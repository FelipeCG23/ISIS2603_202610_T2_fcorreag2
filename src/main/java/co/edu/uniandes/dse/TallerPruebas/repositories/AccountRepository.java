package co.edu.uniandes.dse.TallerPruebas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;

/**
 * Interface that persists an account
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

}
