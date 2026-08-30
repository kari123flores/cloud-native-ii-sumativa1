package cl.duoc.function_roles.controller;

import cl.duoc.function_roles.model.Rol;
import cl.duoc.function_roles.service.RolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> buscarPorId(@PathVariable Long id) {
        return rolService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rol crear(@RequestBody Rol rol) {
        return rolService.guardar(rol);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizar(@PathVariable Long id, @RequestBody Rol rol) {
        if (rolService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        rol.setIdRol(id);
        return ResponseEntity.ok(rolService.guardar(rol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (rolService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}