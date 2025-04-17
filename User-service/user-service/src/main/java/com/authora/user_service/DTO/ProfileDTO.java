package com.authora.user_service.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

public class ProfileDTO {
    private String userId;
    private String fullName;
    private String email;
    private String contactNo;
    private String address;
    private String emergencyContact;

    private LocalDate dateOfBirth;
    private String status;
    private byte[] profilePicture;
    private String role;
    private LocalDateTime deletedAt;

    public ProfileDTO(String userId, String fullName, String email, String contactNo, String emergencyContact, String address,
                      LocalDate dateOfBirth, String status, byte[] profilePicture, String role, LocalDateTime deletedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.contactNo = contactNo;
        this.emergencyContact = emergencyContact;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
        this.profilePicture = profilePicture;
        this.role = role;
        this.deletedAt = deletedAt;

    }


    // Corrected Getters
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getContactNo() { return contactNo; }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public String getAddress() { return address; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getStatus() { return status; }
    public byte[] getProfilePicture() { return profilePicture; }
    public String getRole() { return role; }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Setters
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    // Convert Base64 to byte[] before setting
    public void setProfilePictureBase64(String profilePictureBase64) {
        if (profilePictureBase64 != null) {
            this.profilePicture = Base64.getDecoder().decode(profilePictureBase64);
        }
    }

    // Get profile picture as Base64 string
    public String getProfilePictureBase64() {
        return (profilePicture != null) ? Base64.getEncoder().encodeToString(profilePicture) : null;
    }
}
