package cl.duoc.function_usuarios;

import cl.duoc.function_usuarios.controller.UsuarioController;
import cl.duoc.function_usuarios.model.Usuario;
import cl.duoc.function_usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FunctionUsuariosApplicationTests {

    @Test
    void listarUsuariosDebeRetornarUsuarios() {

        UsuarioService usuarioService = mock(UsuarioService.class);

        when(usuarioService.listar()).thenReturn(List.of(
                new Usuario()
        ));

        UsuarioController controller = new UsuarioController(usuarioService);

        List<Usuario> resultado = controller.listar();

        assertEquals(1, resultado.size());
    }
}
