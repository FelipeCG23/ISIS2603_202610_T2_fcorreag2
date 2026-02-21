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
 * Pruebas de lógica de TransactionService
 */
@DataJpaTest
@Transactional
@Import(TransactionService.class)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

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
        entityManager.getEntityManager().createQuery("delete from TransactionEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from AccountEntity").executeUpdate();
    }

    /**
     * Inserta datos iniciales para el correcto funcionamiento de las pruebas.
     */
    private void insertData() {
        for (int i = 0; i < 3; i++) {
            AccountEntity accountEntity = factory.manufacturePojo(AccountEntity.class);
            accountEntity.setEstado("ACTIVA");
            accountEntity.setSaldo(1000.0);
            entityManager.persist(accountEntity);
            accountList.add(accountEntity);
        }

        for (int i = 0; i < 3; i++) {
            PocketEntity pocketEntity = factory.manufacturePojo(PocketEntity.class);
            pocketEntity.setAccount(accountList.get(0));
            pocketEntity.setNombre("Bolsillo " + i);
            pocketEntity.setSaldo(0.0);
            entityManager.persist(pocketEntity);
            pocketList.add(pocketEntity);
        }
        // Actualizar la lista de bolsillos en la cuenta para las validaciones
        accountList.get(0).setPockets(pocketList);
    }

    /**
     * Prueba para transferir dinero de una cuenta a un bolsillo.
     */
    @Test
    void testTransferirABolsillo() throws EntityNotFoundException, BusinessLogicException {
        AccountEntity account = accountList.get(0);
        Double montoInicial = account.getSaldo();
        Double montoTransferencia = 100.0;
        
        String result = transactionService.transferirABolsillo(account.getId(), "Bolsillo 0", montoTransferencia);
        
        assertNotNull(result);
        AccountEntity updatedAccount = entityManager.find(AccountEntity.class, account.getId());
        assertEquals(montoInicial - montoTransferencia, updatedAccount.getSaldo());
        
        PocketEntity updatedPocket = entityManager.find(PocketEntity.class, pocketList.get(0).getId());
        assertEquals(montoTransferencia, updatedPocket.getSaldo());
    }

    /**
     * Prueba para transferir dinero a un bolsillo con una cuenta que no existe.
     */
    @Test
    void testTransferirABolsilloWithInvalidAccount() {
        assertThrows(EntityNotFoundException.class, () -> {
            transactionService.transferirABolsillo(0L, "Bolsillo 0", 100.0);
        });
    }

    /**
     * Prueba para transferir dinero desde una cuenta bloqueada a un bolsillo.
     */
    @Test
    void testTransferirABolsilloWithBlockedAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(1);
            account.setEstado("BLOQUEADA");
            entityManager.merge(account);
            
            transactionService.transferirABolsillo(account.getId(), "Bolsillo 0", 100.0);
        });
    }

    /**
     * Prueba para transferir dinero a un bolsillo que no existe.
     */
    @Test
    void testTransferirABolsilloWithInvalidPocket() {
        assertThrows(EntityNotFoundException.class, () -> {
            AccountEntity account = accountList.get(0);
            transactionService.transferirABolsillo(account.getId(), "BolsilloNoExiste", 100.0);
        });
    }

    /**
     * Prueba para transferir un monto negativo.
     */
    @Test
    void testTransferirABolsilloWithNegativeAmount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(0);
            transactionService.transferirABolsillo(account.getId(), "Bolsillo 0", -100.0);
        });
    }

    /**
     * Prueba para transferir un monto mayor al saldo disponible.
     */
    @Test
    void testTransferirABolsilloWithInsufficientBalance() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(0);
            transactionService.transferirABolsillo(account.getId(), "Bolsillo 0", 2000.0);
        });
    }

    /**
     * Prueba para transferir dinero entre cuentas.
     */
    @Test
    void testTransferirACuenta() throws EntityNotFoundException, BusinessLogicException {
        AccountEntity accountOrigen = accountList.get(0);
        AccountEntity accountDestino = accountList.get(1);
        Double montoInicialOrigen = accountOrigen.getSaldo();
        Double montoInicialDestino = accountDestino.getSaldo();
        Double montoTransferencia = 100.0;
        
        String result = transactionService.transferirACuenta(accountOrigen.getId(), accountDestino.getId(), montoTransferencia);
        
        assertNotNull(result);
        AccountEntity updatedOrigen = entityManager.find(AccountEntity.class, accountOrigen.getId());
        assertEquals(montoInicialOrigen - montoTransferencia, updatedOrigen.getSaldo());
        
        AccountEntity updatedDestino = entityManager.find(AccountEntity.class, accountDestino.getId());
        assertEquals(montoInicialDestino + montoTransferencia, updatedDestino.getSaldo());
    }

    /**
     * Prueba para transferir dinero cuando la cuenta origen no existe.
     */
    @Test
    void testTransferirACuentaWithInvalidOriginAccount() {
        assertThrows(EntityNotFoundException.class, () -> {
            AccountEntity accountDestino = accountList.get(1);
            transactionService.transferirACuenta(0L, accountDestino.getId(), 100.0);
        });
    }

    /**
     * Prueba para transferir dinero cuando la cuenta destino no existe.
     */
    @Test
    void testTransferirACuentaWithInvalidDestinationAccount() {
        assertThrows(EntityNotFoundException.class, () -> {
            AccountEntity accountOrigen = accountList.get(0);
            transactionService.transferirACuenta(accountOrigen.getId(), 0L, 100.0);
        });
    }

    /**
     * Prueba para transferir dinero entre la misma cuenta.
     */
    @Test
    void testTransferirACuentaWithSameAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity account = accountList.get(0);
            transactionService.transferirACuenta(account.getId(), account.getId(), 100.0);
        });
    }

    /**
     * Prueba para transferir dinero desde una cuenta bloqueada.
     */
    @Test
    void testTransferirACuentaWithBlockedOriginAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity accountOrigen = accountList.get(0);
            AccountEntity accountDestino = accountList.get(1);
            accountOrigen.setEstado("BLOQUEADA");
            entityManager.merge(accountOrigen);
            
            transactionService.transferirACuenta(accountOrigen.getId(), accountDestino.getId(), 100.0);
        });
    }

    /**
     * Prueba para transferir dinero a una cuenta bloqueada.
     */
    @Test
    void testTransferirACuentaWithBlockedDestinationAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity accountOrigen = accountList.get(0);
            AccountEntity accountDestino = accountList.get(1);
            accountDestino.setEstado("BLOQUEADA");
            entityManager.merge(accountDestino);
            
            transactionService.transferirACuenta(accountOrigen.getId(), accountDestino.getId(), 100.0);
        });
    }

    /**
     * Prueba para transferir un monto negativo entre cuentas.
     */
    @Test
    void testTransferirACuentaWithNegativeAmount() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity accountOrigen = accountList.get(0);
            AccountEntity accountDestino = accountList.get(1);
            transactionService.transferirACuenta(accountOrigen.getId(), accountDestino.getId(), -100.0);
        });
    }

    /**
     * Prueba para transferir un monto mayor al saldo disponible entre cuentas.
     */
    @Test
    void testTransferirACuentaWithInsufficientBalance() {
        assertThrows(BusinessLogicException.class, () -> {
            AccountEntity accountOrigen = accountList.get(0);
            AccountEntity accountDestino = accountList.get(1);
            transactionService.transferirACuenta(accountOrigen.getId(), accountDestino.getId(), 2000.0);
        });
    }

}