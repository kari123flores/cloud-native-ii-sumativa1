package cl.duoc;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.*;
import java.util.Optional;

public class RolesFunction {

    // GET TODOS + POST
    @FunctionName("RolesTodos")
    public HttpResponseMessage rolesTodos(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "roles")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.POST) {
            return crearRol(request, context);
        }

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_rol, nombre, descripcion FROM roles ORDER BY id_rol");
                ResultSet rs = stmt.executeQuery()
        ) {

            StringBuilder json = new StringBuilder("[");
            boolean primero = true;

            while (rs.next()) {

                if (!primero) {
                    json.append(",");
                }

                json.append(String.format(
                        "{\"id_rol\":%d,\"nombre\":\"%s\",\"descripcion\":\"%s\"}",
                        rs.getInt("id_rol"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));

                primero = false;
            }

            json.append("]");

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json.toString())
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al consultar roles\"}")
                    .build();
        }
    }

    // GET POR ID + PUT + DELETE
    @FunctionName("Roles")
    public HttpResponseMessage roles(
            @HttpTrigger(
                    name = "req",
                    methods = {
                            HttpMethod.GET,
                            HttpMethod.PUT,
                            HttpMethod.DELETE
                    },
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "roles/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.PUT) {
            return actualizarRol(request, id, context);
        }

        if (request.getHttpMethod() == HttpMethod.DELETE) {
            return eliminarRol(request, id, context);
        }

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_rol, nombre, descripcion FROM roles WHERE id_rol = ?")
        ) {

            stmt.setInt(1, Integer.parseInt(id));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String json = String.format(
                        "{\"id_rol\":%d,\"nombre\":\"%s\",\"descripcion\":\"%s\"}",
                        rs.getInt("id_rol"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(json)
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\":\"Rol no encontrado\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al consultar rol\"}")
                    .build();
        }
    }

    // POST
    private HttpResponseMessage crearRol(
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try {

            String body = request.getBody().orElse("");

            String nombre = extraerValor(body, "nombre");
            String descripcion = extraerValor(body, "descripcion");

            try (
                    Connection conn = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO roles (nombre, descripcion) VALUES (?, ?)")
            ) {

                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);

                stmt.executeUpdate();
            }

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Rol creado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al crear rol\"}")
                    .build();
        }
    }

    // PUT
    private HttpResponseMessage actualizarRol(
            HttpRequestMessage<Optional<String>> request,
            String id,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try {

            String body = request.getBody().orElse("");

            String nombre = extraerValor(body, "nombre");
            String descripcion = extraerValor(body, "descripcion");

            try (
                    Connection conn = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE roles SET nombre = ?, descripcion = ? WHERE id_rol = ?")
            ) {

                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);
                stmt.setInt(3, Integer.parseInt(id));

                int filas = stmt.executeUpdate();

                if (filas == 0) {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("{\"mensaje\":\"Rol no encontrado\"}")
                            .build();
                }
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Rol actualizado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al actualizar rol\"}")
                    .build();
        }
    }

    // DELETE
    private HttpResponseMessage eliminarRol(
            HttpRequestMessage<Optional<String>> request,
            String id,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM roles WHERE id_rol = ?")
        ) {

            stmt.setInt(1, Integer.parseInt(id));

            int filas = stmt.executeUpdate();

            if (filas == 0) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"mensaje\":\"Rol no encontrado\"}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Rol eliminado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al eliminar rol\"}")
                    .build();
        }
    }

    // AUXILIAR PARA LEER JSON
    private String extraerValor(String json, String campo) {

        String buscar = "\"" + campo + "\":";
        int inicio = json.indexOf(buscar);

        if (inicio == -1) {
            return "";
        }

        inicio += buscar.length();

        while (inicio < json.length()
                && Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }

        if (json.charAt(inicio) == '"') {

            inicio++;

            int fin = json.indexOf("\"", inicio);

            return json.substring(inicio, fin);
        }

        int fin = inicio;

        while (fin < json.length()
                && json.charAt(fin) != ','
                && json.charAt(fin) != '}') {
            fin++;
        }

        return json.substring(inicio, fin).trim();
    }
}