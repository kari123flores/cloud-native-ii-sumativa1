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
}