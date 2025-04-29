package com.authora.certificateService.Repository;

import com.authora.certificateService.Entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Integer> {

    // Check if a certificate exists for a specific userId and certificateName
    boolean existsByUserIdAndCertificateName(String userId, String certificateName);

    // Get a list of certificates by userId
    List<Certificate> findByUserId(String userId);
}
