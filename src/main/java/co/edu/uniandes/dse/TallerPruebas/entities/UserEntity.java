package co.edu.uniandes.dse.TallerPruebas.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

/**
 * Clase que representa un usuario en la persistencia
 */
@Data
@Entity
public class UserEntity extends BaseEntity {

    private String name;
    private String email;
    private String login;
    private String cedula;

    @PodamExclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountEntity> accounts = new ArrayList<>();
}
