package cl.duoc;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.*;
import java.util.Optional;

public class Function {

    // GET TODOS + POST
    @FunctionName("UsuariosTodos")
    public HttpResponseMessage usuariosTodos(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "usuarios")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.POST) {
            return crearUsuario(request, context);
        }

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_usuario, nombre, email, id_rol, estado " +
                        "FROM usuarios ORDER BY id_usuario");
                ResultSet rs = stmt.executeQuery()
        ) {

            StringBuilder json = new StringBuilder("[");
            boolean primero = true;

            while (rs.next()) {

                if (!primero) {
                    json.append(",");
                }

                json.append(String.format(
                        "{\"id_usuario\":%d,\"nombre\":\"%s\",\"email\":\"%s\",\"id_rol\":%d,\"estado\":\"%s\"}",
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getInt("id_rol"),
                        rs.getString("estado")
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
                    .body("{\"error\":\"Error al conectar con Oracle\"}")
                    .build();
        }
    }

    // GET POR ID + PUT + DELETE
    @FunctionName("Usuarios")
    public HttpResponseMessage usuarios(
            @HttpTrigger(
                    name = "req",
                    methods = {
                            HttpMethod.GET,
                            HttpMethod.PUT,
                            HttpMethod.DELETE
                    },
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "usuarios/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.PUT) {
            return actualizarUsuario(request, id, context);
        }

        if (request.getHttpMethod() == HttpMethod.DELETE) {
            return eliminarUsuario(request, id, context);
        }

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_usuario, nombre, email, id_rol, estado " +
                        "FROM usuarios WHERE id_usuario = ?")
        ) {

            stmt.setInt(1, Integer.parseInt(id));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String json = String.format(
                        "{\"id_usuario\":%d,\"nombre\":\"%s\",\"email\":\"%s\",\"id_rol\":%d,\"estado\":\"%s\"}",
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getInt("id_rol"),
                        rs.getString("estado")
                );

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(json)
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\":\"Usuario no encontrado\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al consultar usuario\"}")
                    .build();
        }
    }

    // POST
    private HttpResponseMessage crearUsuario(
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try {

            String body = request.getBody().orElse("");

            String nombre = extraerValor(body, "nombre");
            String email = extraerValor(body, "email");
            String clave = extraerValor(body, "password");
            int idRol = Integer.parseInt(extraerValor(body, "id_rol"));
            String estado = extraerValor(body, "estado");

            try (
                    Connection conn = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO usuarios " +
                            "(nombre, email, password, id_rol, estado) " +
                            "VALUES (?, ?, ?, ?, ?)")
            ) {

                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, clave);
                stmt.setInt(4, idRol);
                stmt.setString(5, estado);

                stmt.executeUpdate();
            }

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Usuario creado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al crear usuario\"}")
                    .build();
        }
    }

    // PUT
    private HttpResponseMessage actualizarUsuario(
            HttpRequestMessage<Optional<String>> request,
            String id,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try {

            String body = request.getBody().orElse("");

            String nombre = extraerValor(body, "nombre");
            String email = extraerValor(body, "email");
            String clave = extraerValor(body, "password");
            int idRol = Integer.parseInt(extraerValor(body, "id_rol"));
            String estado = extraerValor(body, "estado");

            try (
                    Connection conn = DriverManager.getConnection(url, user, password);
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE usuarios SET nombre = ?, email = ?, " +
                            "password = ?, id_rol = ?, estado = ? " +
                            "WHERE id_usuario = ?")
            ) {

                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, clave);
                stmt.setInt(4, idRol);
                stmt.setString(5, estado);
                stmt.setInt(6, Integer.parseInt(id));

                int filas = stmt.executeUpdate();

                if (filas == 0) {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("{\"mensaje\":\"Usuario no encontrado\"}")
                            .build();
                }
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Usuario actualizado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al actualizar usuario\"}")
                    .build();
        }
    }

    // DELETE
    private HttpResponseMessage eliminarUsuario(
            HttpRequestMessage<Optional<String>> request,
            String id,
            ExecutionContext context) {

        String url = System.getenv("ORACLE_URL");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM usuarios WHERE id_usuario = ?")
        ) {

            stmt.setInt(1, Integer.parseInt(id));

            int filas = stmt.executeUpdate();

            if (filas == 0) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"mensaje\":\"Usuario no encontrado\"}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"mensaje\":\"Usuario eliminado correctamente\"}")
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al eliminar usuario\"}")
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