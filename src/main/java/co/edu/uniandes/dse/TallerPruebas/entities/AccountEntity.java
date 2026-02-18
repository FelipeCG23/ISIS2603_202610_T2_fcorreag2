package co.edu.uniandes.dse.TallerPruebas.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

/**
 * Clase que representa una cuenta en la persistencia
 */
@Data
@Entity
public class AccountEntity extends BaseEntity {

    private String numeroCuenta;
    private Double saldo;
    private String estado; // ACTIVA, BLOQUEADA

    @PodamExclude
    @ManyToOne
    private UserEntity user;

    @PodamExclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PocketEntity> pockets = new ArrayList<>();

    @PodamExclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions = new ArrayList<>();
}
