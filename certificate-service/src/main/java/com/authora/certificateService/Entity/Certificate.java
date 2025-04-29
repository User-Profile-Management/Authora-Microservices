package com.authora.certificateService.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Integer certificateId;

    @Column(name = "certificate_name", nullable = false)
    private String certificateName;

    @Column(name = "issued_by", nullable = false)
    private String issuedBy;

    @Lob
    @Column(name = "certificate_pdf")
    private byte[] certificatePdf;

    @Column(name = "user_id", nullable = false)
    private String userId; // now a String or UUID

    public Integer getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Integer certificateId) {
        this.certificateId = certificateId;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public byte[] getCertificatePdf() {
        return certificatePdf;
    }

    public void setCertificatePdf(byte[] certificatePdf) {
        this.certificatePdf = certificatePdf;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
