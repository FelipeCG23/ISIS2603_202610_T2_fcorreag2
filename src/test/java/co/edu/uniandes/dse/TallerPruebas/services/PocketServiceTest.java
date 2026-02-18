package co.edu.uniandes.dse.TallerPruebas.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * Pruebas de lógica de PocketService
 */
@DataJpaTest
@Transactional
@Import(PocketService.class)
public class PocketServiceTest {

    @Autowired
    private PocketService pocketService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<AccountEntity> accountList = new ArrayList<>();
    private List<PocketEntity> pocketList = new ArrayList<>();

    /**
     * Configuración inicial de la prueba.
     */
    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    /**
     * Limpia las tablas que están implicadas en la prueba.
     */
    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from PocketEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from AccountEntity").executeUpdate();
    }

    /**
     * Inserta datos iniciales para el correcto funcionamiento de las pruebas.
     */
    private void insertData() {
        for (int i = 0; i < 3; i++) {
            AccountEntity accountEntity = factory.manufacturePojo(AccountEntity.class);
            accountEntity.setEstado("ACTIVA");
            entityManager.persist(accountEntity);
            accountList.add(accountEntity);
        }

        for (int i = 0; i < 3; i++) {
            PocketEntity pocketEntity = factory.manufacturePojo(PocketEntity.class);
            pocketEntity.setAccount(accountList.get(0));
            entityManager.persist(pocketEntity);
            pocketList.add(pocketEntity);
        }
        // Actualizar la lista de bolsillos en la cuenta para las validaciones
        accountList.get(0).setPockets(pocketList);
    }

    /**
     * Prueba para crear un Pocket.
     */
    @Test
    void testCreatePocket() throws EntityNotFoundException, BusinessLogicException {
        PocketEntity newEntity = factory.manufacturePojo(PocketEntity.class);
        newEntity.setNombre("Nuevo Bolsillo");
        AccountEntity account = accountList.get(1); // Una cuenta sin bolsillos
        
        PocketEntity result = pocketService.createPocket(account.getId(), newEntity);
        
        assertNotNull(result);
        PocketEntity entity = entityManager.find(PocketEntity.class, result.getId());
        assertEquals(newEntity.getId(), entity.getId());
        assertEquals(newEntity.getNombre(), entity.getNombre());
    }

    /**
     * Prueba para crear un Pocket con una cuenta que no existe.
     */
    @Test
    void testCreatePocketWithInvalidAccount() {
        assertThrows(EntityNotFoundException.class, () -> {
            PocketEntity newEntity = factory.manufacturePojo(PocketEntity.class);
            pocketService.createPocket(0L, newEntity);
        });
    }

    /**
     * Prueba para crear un Pocket con una cuenta bloqueada.
     */
    @Test
    void testCreatePocketWithBlockedAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(2);
            account.setEstado("BLOQUEADA");
            entityManager.merge(account);
            
            PocketEntity newEntity = factory.manufacturePojo(PocketEntity.class);
            pocketService.createPocket(account.getId(), newEntity);
        });
    }

    /**
     * Prueba para crear un Pocket con un nombre ya existente en la cuenta.
     */
    @Test
    void testCreatePocketWithDuplicateName() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(0);
            PocketEntity newEntity = factory.manufacturePojo(PocketEntity.class);
            newEntity.setNombre(pocketList.get(0).getNombre());
            
            pocketService.createPocket(account.getId(), newEntity);
        });
    }
}
