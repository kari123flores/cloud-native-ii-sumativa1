package cl.duoc.function_roles.repository;

import cl.duoc.function_roles.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
}