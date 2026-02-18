package co.edu.uniandes.dse.TallerPruebas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniandes.dse.TallerPruebas.entities.UserEntity;

/**
 * Interface that persists a user
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
