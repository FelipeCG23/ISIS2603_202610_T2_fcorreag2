package co.edu.uniandes.dse.TallerPruebas.entities;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

/**
 * Clase que representa una transacción en la persistencia
 */
@Data
@Entity
public class TransactionEntity extends BaseEntity {

    private Double monto;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    private String tipo; // ENTRADA, SALIDA

    @PodamExclude
    @ManyToOne
    private AccountEntity account;
}
