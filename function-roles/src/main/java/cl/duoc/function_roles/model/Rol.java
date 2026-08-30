package cl.duoc.function_roles.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ROLES")
@Data
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROL")
    private Long idRol;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String descripcion;
}