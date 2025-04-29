package com.authora.certificateService.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Integer certificateId;
    private String certificateName;
    private String issuedBy;
    private String additionalField; // If applicable

    // Modify constructor accordingly
    public CertificateDTO(Integer certificateId, String certificateName, String issuedBy) {
        this.certificateId = certificateId;
        this.certificateName = certificateName;
        this.issuedBy = issuedBy;
    }
}
