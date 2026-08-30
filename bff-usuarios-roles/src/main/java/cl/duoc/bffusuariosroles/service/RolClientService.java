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
                .uri("/roles")
                .retrieve()
                .body(String.class);
    }
}