package cl.duoc;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.*;
import java.util.Optional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

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

            // GENERAR EVENTO
            enviarEvento(
                    "RolCreado",
                    "{\"nombre\":\"" + nombre +
                    "\",\"descripcion\":\"" + descripcion + "\"}",
                    context
            );

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

            // GENERAR EVENTO
            enviarEvento(
                    "RolActualizado",
                    "{\"id_rol\":" + id +
                    ",\"nombre\":\"" + nombre +
                    "\",\"descripcion\":\"" + descripcion + "\"}",
                    context
            );

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

            // GENERAR EVENTO
            enviarEvento(
                    "RolEliminado",
                    "{\"id_rol\":" + id + "}",
                    context
            );

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


    // ENVÍA EVENTOS A AZURE EVENT GRID
    private void enviarEvento(
            String tipoEvento,
            String datos,
            ExecutionContext context) {

        try {

            String endpoint = System.getenv("EVENT_GRID_ENDPOINT");
            String key = System.getenv("EVENT_GRID_KEY");

            if (endpoint == null || endpoint.isBlank()) {
                context.getLogger().warning(
                        "EVENT_GRID_ENDPOINT no está configurado"
                );
                return;
            }

            if (key == null || key.isBlank()) {
                context.getLogger().warning(
                        "EVENT_GRID_KEY no está configurado"
                );
                return;
            }

            String evento = String.format(
                    "[{" +
                    "\"id\":\"%s\"," +
                    "\"eventType\":\"%s\"," +
                    "\"subject\":\"roles\"," +
                    "\"eventTime\":\"%s\"," +
                    "\"dataVersion\":\"1.0\"," +
                    "\"data\":%s" +
                    "}]",
                    UUID.randomUUID().toString(),
                    tipoEvento,
                    Instant.now().toString(),
                    datos
            );

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("aeg-sas-key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(evento))
                    .build();

            HttpClient cliente = HttpClient.newHttpClient();

            HttpResponse<String> respuesta = cliente.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString()
            );

            context.getLogger().info(
                    "Evento " +
                    tipoEvento +
                    " enviado a Event Grid. HTTP " +
                    respuesta.statusCode()
            );

            if (respuesta.statusCode() >= 400) {
                context.getLogger().warning(
                        "Respuesta Event Grid: " + respuesta.body()
                );
            }

        } catch (Exception e) {

            context.getLogger().severe(
                    "Error enviando evento a Event Grid: " +
                    e.getMessage()
            );
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

        while (
                inicio < json.length() &&
                Character.isWhitespace(json.charAt(inicio))
        ) {
            inicio++;
        }

        if (json.charAt(inicio) == '"') {

            inicio++;

            int fin = json.indexOf("\"", inicio);

            return json.substring(inicio, fin);
        }

        int fin = inicio;

        while (
                fin < json.length() &&
                json.charAt(fin) != ',' &&
                json.charAt(fin) != '}'
        ) {
            fin++;
        }

        return json.substring(inicio, fin).trim();
    }
}