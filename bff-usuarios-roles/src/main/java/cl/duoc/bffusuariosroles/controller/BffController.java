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

    // =========================
    // USUARIOS
    // =========================

    @GetMapping("/usuarios")
    public String listarUsuarios() {
        return usuarioClientService.listarUsuarios();
    }

    @GetMapping("/usuarios/{id}")
    public String buscarUsuarioPorId(@PathVariable Long id) {
        return usuarioClientService.buscarUsuarioPorId(id);
    }

    @PostMapping("/usuarios")
    public String crearUsuario(@RequestBody String usuarioJson) {
        return usuarioClientService.crearUsuario(usuarioJson);
    }

    @PutMapping("/usuarios/{id}")
    public String actualizarUsuario(
            @PathVariable Long id,
            @RequestBody String usuarioJson) {

        return usuarioClientService.actualizarUsuario(id, usuarioJson);
    }

    @DeleteMapping("/usuarios/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioClientService.eliminarUsuario(id);
        return "{\"mensaje\":\"Usuario eliminado correctamente\"}";
    }

    // =========================
    // ROLES
    // =========================

    @GetMapping("/roles")
    public String listarRoles() {
        return rolClientService.listarRoles();
    }

    @GetMapping("/roles/{id}")
    public String buscarRolPorId(@PathVariable Long id) {
        return rolClientService.buscarRolPorId(id);
    }

    @PostMapping("/roles")
    public String crearRol(@RequestBody String rolJson) {
        return rolClientService.crearRol(rolJson);
    }

    @PutMapping("/roles/{id}")
    public String actualizarRol(
            @PathVariable Long id,
            @RequestBody String rolJson) {

        return rolClientService.actualizarRol(id, rolJson);
    }

    @DeleteMapping("/roles/{id}")
    public String eliminarRol(@PathVariable Long id) {
        rolClientService.eliminarRol(id);
        return "{\"mensaje\":\"Rol eliminado correctamente\"}";
    }
}