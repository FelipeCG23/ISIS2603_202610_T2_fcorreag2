package co.edu.uniandes.dse.TallerPruebas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;

/**
 * Interface that persists a pocket
 */
@Repository
public interface PocketRepository extends JpaRepository<PocketEntity, Long> {

}
