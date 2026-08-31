package cl.duoc.function_roles;

import cl.duoc.function_roles.controller.RolController;
import cl.duoc.function_roles.model.Rol;
import cl.duoc.function_roles.service.RolService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FunctionRolesApplicationTests {

    @Test
    void listarRolesDebeRetornarRoles() {

        RolService rolService = mock(RolService.class);

        when(rolService.listar()).thenReturn(List.of(
                new Rol()
        ));

        RolController controller = new RolController(rolService);

        List<Rol> resultado = controller.listar();

        assertEquals(1, resultado.size());
    }
}
