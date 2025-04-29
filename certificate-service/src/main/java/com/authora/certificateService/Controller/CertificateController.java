package com.authora.certificateService.Controller;

import com.authora.certificateService.DTO.ApiResponse;
import com.authora.certificateService.DTO.CertificateDTO;
import com.authora.certificateService.Service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users/certificates")
public class CertificateController {

    @Autowired
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CertificateDTO>> addCertificate(
            @RequestParam("certificateName") String certificateName,
            @RequestParam("issuedBy") String issuedBy,
            @RequestParam("file") MultipartFile pdfFile) {

        try {
            // Validate file type and size
            if (!pdfFile.getContentType().equalsIgnoreCase("application/pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Invalid file type", null, "Only PDF files are allowed"));
            }

            if (pdfFile.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiResponse<>(413, "File too large", null, "Maximum file size is 5MB"));
            }

            // Get the userId from JWT token
            String userId = getCurrentUserId();

            // Add certificate using the service method
            CertificateDTO response = certificateService.addCertificate(userId, certificateName, issuedBy, pdfFile);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "Certificate uploaded successfully", response, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error processing file", null, e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificates() {
        String userId = getCurrentUserId();
        List<CertificateDTO> certificates = certificateService.getCertificatesByUserId(userId);

        if (certificates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(204, "No certificates found", Collections.emptyList(), null));
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "Certificates retrieved successfully", certificates, null));
    }

    @GetMapping("/{certificateId}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Integer certificateId) {
        byte[] pdfData = certificateService.getCertificatePdfById(certificateId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=certificate_" + certificateId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<ApiResponse<String>> deleteCertificate(@PathVariable Integer certificateId) {
        try {
            String userId = getCurrentUserId();

            certificateService.deleteCertificateById(userId, certificateId);

            return ResponseEntity.ok(new ApiResponse<>(200, "Certificate deleted successfully", null, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null, null));
        }
    }

    // Helper method to extract userId from JWT Authentication
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // assuming username or userId is stored as name in token
    }
}
