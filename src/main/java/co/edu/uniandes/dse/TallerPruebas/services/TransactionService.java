package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import co.edu.uniandes.dse.TallerPruebas.repositories.PocketRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class TransactionService {
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PocketRepository pocketRepository;

    // NOTE: Método que transfiere plata de una cuenta a un bolsillo
    // NOTE: El método retorna el saldo nuevo de la cuenta y del bolsillo después de la transferencia
    @Transactional
    public String transferirABolsillo(Long accountId, String nombreBolsillo, Double monto) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de transferir {} de la cuenta con id = {} al bolsillo {}", monto, accountId, nombreBolsillo);
        
        // 1. Verificar que la cuenta existe
        Optional<AccountEntity> accountEntity = accountRepository.findById(accountId);
        if (accountEntity.isEmpty()) {
            throw new EntityNotFoundException("La cuenta no existe");
        }

        // 2. Verificar que la cuenta esté activa
        if (!"ACTIVA".equals(accountEntity.get().getEstado())) {
            throw new BusinessLogicException("La cuenta debe estar en estado ACTIVA para transferir a bolsillos");
        }

        // 3. Verificar que el bolsillo existe en esa cuenta
        List<PocketEntity> bolsillos = accountEntity.get().getPockets();
        int p = 0;
        boolean existe = false;
        PocketEntity bolsillo = null;
        while (p < bolsillos.size() && !existe) {
            if (bolsillos.get(p).getNombre().equals(nombreBolsillo)) {
                existe = true;
                bolsillo = bolsillos.get(p);
            }
            p++;
        }
        if (!existe) {
            throw new EntityNotFoundException("El bolsillo no existe");
        }

        // 4. Verificar que el monto a transferir es positivo
        if (monto <= 0) {
            throw new BusinessLogicException("El monto a transferir debe ser positivo");
        }

        // 5. Verificar que la cuenta tiene saldo suficiente
        if (accountEntity.get().getSaldo() < monto) {
            throw new BusinessLogicException("La cuenta no tiene saldo suficiente para la transferencia");
        }
        // 6. Realizar la transferencia
        // NOTE: Aquí se actualizan los saldos de la cuenta y el bolsillo
        Double saldoCuenta = accountEntity.get().getSaldo() - monto;
        accountEntity.get().setSaldo(saldoCuenta);
        accountRepository.save(accountEntity.get());

        Double saldoBolsillo = bolsillo.getSaldo() + monto;
        bolsillo.setSaldo(saldoBolsillo);
        pocketRepository.save(bolsillo);
        
        log.info("Termina proceso de transferir {} de la cuenta con id = {} al bolsillo {}", monto, accountId, nombreBolsillo);
        return "Transferencia realizada con éxito. El nuevo saldo de la cuenta es: " + saldoCuenta + " y el nuevo saldo del bolsillo es: " + saldoBolsillo;
    }

    @Transactional
    public String transferirACuenta(Long cOrigen, Long cDestino, Double monto) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de transferir {} de la cuenta con id = {} a la cuenta con id = {}", monto, cOrigen, cDestino);
        
        // 1. Verificar que las cuentas existen
        Optional<AccountEntity> cuentaOrigen = accountRepository.findById(cOrigen);
        if (cuentaOrigen.isEmpty()) {
            throw new EntityNotFoundException("La cuenta origen no existe");
        }
        Optional<AccountEntity> cuentaDestino = accountRepository.findById(cDestino);
        if (cuentaDestino.isEmpty()) {
            throw new EntityNotFoundException("La cuenta destino no existe");
        }

        // 2. Verificar que las cuentas de origen y destino no sean la misma
        if (cOrigen.equals(cDestino)) {
            throw new BusinessLogicException("La cuenta origen y destino no pueden ser la misma");
        }

        // 3. Verificar que las cuentas estén activas
        if (!"ACTIVA".equals(cuentaOrigen.get().getEstado())) {
            throw new BusinessLogicException("La cuenta origen debe estar en estado ACTIVA para transferir a otra cuenta");
        }
        if (!"ACTIVA".equals(cuentaDestino.get().getEstado())) {
            throw new BusinessLogicException("La cuenta destino debe estar en estado ACTIVA para recibir transferencias");
        }

        // 4. Verificar que el monto a transferir es positivo
        if (monto <= 0) {
            throw new BusinessLogicException("El monto a transferir debe ser positivo");
        }

        // 5. Verificar que la cuenta origen tiene saldo suficiente
        if (cuentaOrigen.get().getSaldo() < monto) {
            throw new BusinessLogicException("La cuenta origen no tiene saldo suficiente para la transferencia");
        }

        // 6. Realizar la transferencia
        // NOTE: Aquí se actualizan los saldos de las cuentas de origen y destino (solo se muestra el del origen por privacidad)
        Double saldoOrigen = cuentaOrigen.get().getSaldo() - monto;
        cuentaOrigen.get().setSaldo(saldoOrigen);
        accountRepository.save(cuentaOrigen.get());

        Double saldoDestino = cuentaDestino.get().getSaldo() + monto;
        cuentaDestino.get().setSaldo(saldoDestino);
        accountRepository.save(cuentaDestino.get());

        return "Transferencia realizada con éxito. El nuevo saldo de tu cuenta es: " + saldoOrigen;
    }
}
