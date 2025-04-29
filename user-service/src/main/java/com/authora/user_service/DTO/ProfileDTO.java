package com.authora.user_service.DTO;

import com.authora.user_service.modal.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProfileDTO {
    private String userId;
    private String fullName;
    private String email;
    private String contactNo;
    private String emergencyContact;
    private String address;
    private LocalDate dateOfBirth;
    private String status;
    private byte[] profilePicture;
    private String role;
    private LocalDateTime deletedAt;

    // Default constructor
    public ProfileDTO() {
    }

    // Full constructor
    public ProfileDTO(String userId, String fullName, String email, String contactNo,
                      String emergencyContact, String address, LocalDate dateOfBirth,
                      String status, byte[] profilePicture, String role, LocalDateTime deletedAt) {
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

    // Minimal constructor for basic user info
    public ProfileDTO(String userId, String role, String status, LocalDateTime deletedAt) {
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.deletedAt = deletedAt;
    }

    public ProfileDTO(String userId, String roleName, User.Status status, LocalDateTime deletedAt) {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "ProfileDTO{" +
                "userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}