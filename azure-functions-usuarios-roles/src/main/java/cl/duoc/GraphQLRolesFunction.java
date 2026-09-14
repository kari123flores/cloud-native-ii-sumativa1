package cl.duoc;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GraphQLRolesFunction {

    private final GraphQL graphQL;

    public GraphQLRolesFunction() {

        GraphQLObjectType rolType = GraphQLObjectType.newObject()
                .name("Rol")
                .field(field -> field
                        .name("id_rol")
                        .type(Scalars.GraphQLInt))
                .field(field -> field
                        .name("nombre")
                        .type(Scalars.GraphQLString))
                .field(field -> field
                        .name("descripcion")
                        .type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType queryType = GraphQLObjectType.newObject()
                .name("Query")

                .field(field -> field
                        .name("roles")
                        .type(graphql.schema.GraphQLList.list(rolType))
                        .dataFetcher(environment -> obtenerRoles()))

                .field(field -> field
                        .name("rol")
                        .type(rolType)
                        .argument(argument -> argument
                                .name("id")
                                .type(Scalars.GraphQLInt))
                        .dataFetcher(environment -> {
                            Integer id = environment.getArgument("id");
                            return obtenerRolPorId(id);
                        }))

                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .build();

        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    @FunctionName("GraphQLRoles")
    public HttpResponseMessage graphqlRoles(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "graphql")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {

            String body = request.getBody().orElse("");

            String query = extraerQuery(body);

            if (query.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"Debe enviar una consulta GraphQL\"}")
                        .build();
            }

            ExecutionInput executionInput = ExecutionInput
                    .newExecutionInput()
                    .query(query)
                    .build();

            Map<String, Object> resultado =
                    graphQL.execute(executionInput).toSpecification();

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(resultado)
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al ejecutar GraphQL\"}")
                    .build();
        }
    }

    private List<Map<String, Object>> obtenerRoles() throws Exception {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        List<Map<String, Object>> roles = new ArrayList<>();

        try (
                Connection conn =
                        DriverManager.getConnection(url, user, password);

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_rol, nombre, descripcion " +
                        "FROM roles ORDER BY id_rol");

                ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                Map<String, Object> rol = new HashMap<>();

                rol.put("id_rol", rs.getInt("id_rol"));
                rol.put("nombre", rs.getString("nombre"));
                rol.put("descripcion", rs.getString("descripcion"));

                roles.add(rol);
            }
        }

        return roles;
    }

    private Map<String, Object> obtenerRolPorId(Integer id)
            throws Exception {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn =
                        DriverManager.getConnection(url, user, password);

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_rol, nombre, descripcion " +
                        "FROM roles WHERE id_rol = ?")
        ) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Map<String, Object> rol = new HashMap<>();

                rol.put("id_rol", rs.getInt("id_rol"));
                rol.put("nombre", rs.getString("nombre"));
                rol.put("descripcion", rs.getString("descripcion"));

                return rol;
            }
        }

        return null;
    }

    private String extraerQuery(String json) {

        if (json == null || json.isEmpty()) {
            return "";
        }

        String buscar = "\"query\"";
        int inicio = json.indexOf(buscar);

        if (inicio == -1) {
            return "";
        }

        inicio = json.indexOf(":", inicio);

        if (inicio == -1) {
            return "";
        }

        inicio = json.indexOf("\"", inicio);

        if (inicio == -1) {
            return "";
        }

        inicio++;

        StringBuilder query = new StringBuilder();
        boolean escape = false;

        for (int i = inicio; i < json.length(); i++) {

            char c = json.charAt(i);

            if (escape) {

                if (c == 'n') {
                    query.append('\n');
                } else if (c == 't') {
                    query.append('\t');
                } else {
                    query.append(c);
                }

                escape = false;

            } else if (c == '\\') {

                escape = true;

            } else if (c == '"') {

                break;

            } else {

                query.append(c);
            }
        }

        return query.toString();
    }
}