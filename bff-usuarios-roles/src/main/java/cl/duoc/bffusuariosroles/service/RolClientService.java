package cl.duoc.bffusuariosroles.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RolClientService {

    private final RestClient restClient;

    public RolClientService(
            @Value("${roles.service.url}") String rolesServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(rolesServiceUrl)
                .build();
    }

    public String listarRoles() {

        return restClient.get()
                .uri("/api/roles")
                .retrieve()
                .body(String.class);
    }

    public String buscarRolPorId(Long id) {

        return restClient.get()
                .uri("/api/roles/" + id)
                .retrieve()
                .body(String.class);
    }

    public String crearRol(String rolJson) {

        return restClient.post()
                .uri("/api/roles")
                .header("Content-Type", "application/json")
                .body(rolJson)
                .retrieve()
                .body(String.class);
    }

    public String actualizarRol(Long id, String rolJson) {

        return restClient.put()
                .uri("/api/roles/" + id)
                .header("Content-Type", "application/json")
                .body(rolJson)
                .retrieve()
                .body(String.class);
    }

    public void eliminarRol(Long id) {

        restClient.delete()
                .uri("/api/roles/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}