package co.edu.uniandes.dse.TallerPruebas.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

/**
 * Clase que representa un bolsillo en la persistencia
 */
@Data
@Entity
public class PocketEntity extends BaseEntity {

    private String nombre;
    private Double saldo;
    private Double metaAhorro;

    @PodamExclude
    @ManyToOne
    private AccountEntity account;
}
