package com.authora.certificateService.Service;

import com.authora.certificateService.DTO.CertificateDTO;
import com.authora.certificateService.Entity.Certificate;
import com.authora.certificateService.Repository.CertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    public List<CertificateDTO> getCertificatesByUserId(String userId) {
        List<Certificate> certificates = certificateRepository.findByUserId(userId);
        return certificates.stream()
                .map(c -> new CertificateDTO(c.getCertificateId(), c.getCertificateName(), c.getIssuedBy()))
                .collect(Collectors.toList());
    }

    public byte[] getCertificatePdfById(Integer certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return certificate.getCertificatePdf();
    }

    public CertificateDTO addCertificate(String userId, String certificateName, String issuedBy, MultipartFile pdfFile) throws IOException {
        boolean exists = certificateRepository.existsByUserIdAndCertificateName(userId, certificateName);
        if (exists) {
            throw new RuntimeException("Certificate already exists");
        }

        Certificate certificate = new Certificate();
        certificate.setCertificateName(certificateName);
        certificate.setIssuedBy(issuedBy);
        certificate.setUserId(userId);
        certificate.setCertificatePdf(pdfFile.getBytes());

        Certificate saved = certificateRepository.save(certificate);
        return new CertificateDTO(saved.getCertificateId(), saved.getCertificateName(), saved.getIssuedBy());
    }

    public void deleteCertificateById(String userId, Integer certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (!certificate.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own certificates.");
        }

        certificateRepository.deleteById(certificateId);
    }
}
