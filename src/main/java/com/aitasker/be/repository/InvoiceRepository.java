package com.aitasker.be.repository;
import com.aitasker.be.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    boolean existsByTransactionId(Long transactionId);
    Optional<InvoiceEntity> findByTransactionId(Long transactionId);
}
