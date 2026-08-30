package cl.duoc.bffusuariosroles.controller;

import cl.duoc.bffusuariosroles.service.UsuarioClientService;
import cl.duoc.bffusuariosroles.service.RolClientService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BffController {

    private final UsuarioClientService usuarioClientService;
    private final RolClientService rolClientService;

    public BffController(
            UsuarioClientService usuarioClientService,
            RolClientService rolClientService) {
        this.usuarioClientService = usuarioClientService;
        this.rolClientService = rolClientService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios() {
        return usuarioClientService.listarUsuarios();
    }

    @GetMapping("/roles")
    public String listarRoles() {
        return rolClientService.listarRoles();
    }
}