package cl.duoc.bffusuariosroles.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UsuarioClientService {

    private final RestClient restClient;

    public UsuarioClientService(
            @Value("${usuarios.service.url}") String usuariosServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(usuariosServiceUrl)
                .build();
    }

    public String listarUsuarios() {
        return restClient.get()
                .uri("/usuarios")
                .retrieve()
                .body(String.class);
    }
    public String buscarUsuarioPorId(Long id) {
    return restClient.get()
            .uri("/usuarios/" + id)
            .retrieve()
            .body(String.class);
    }

    public String crearUsuario(String usuarioJson) {
        return restClient.post()
                .uri("/usuarios")
                .header("Content-Type", "application/json")
                .body(usuarioJson)
                .retrieve()
                .body(String.class);
    }

    public String actualizarUsuario(Long id, String usuarioJson) {
        return restClient.put()
                .uri("/usuarios/" + id)
                .header("Content-Type", "application/json")
                .body(usuarioJson)
                .retrieve()
                .body(String.class);
    }

    public void eliminarUsuario(Long id) {
        restClient.delete()
                .uri("/usuarios/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}