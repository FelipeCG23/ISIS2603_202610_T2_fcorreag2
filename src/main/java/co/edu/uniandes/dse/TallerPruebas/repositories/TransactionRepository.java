package co.edu.uniandes.dse.TallerPruebas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniandes.dse.TallerPruebas.entities.TransactionEntity;

/**
 * Interface that persists a transaction
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

}
