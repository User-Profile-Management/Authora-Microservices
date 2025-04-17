package com.authora.user_service.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")



    public class User {
        @Id
        @Column(name = "user_id")
        private String userId;



        @ManyToOne(fetch = FetchType.LAZY) // Change to EAGER if needed
        @JoinColumn(name = "role_id", nullable = false)
        @Fetch(FetchMode.JOIN)  // Ensures role is fetched in a single query
        private Role role;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @Column(name = "emergency_contact")
        private String emergencyContact;

        @Column(name = "date_of_birth")
        private LocalDate dateOfBirth;

        @Column(name = "contact_no", unique = true)
        private String contactNo;

        @Column(name = "address")
        private String address;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private Status status = Status.ACTIVE;

        @Column(name = "password", nullable = false)
        @JsonIgnore
        private String password;

        @Column(name = "profile_picture")
        @Lob// Change storage type
        private byte[] profilePicture;




        @Column(name = "email", nullable = false, unique = true)
        private String email;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "deleted_at")
        private LocalDateTime deletedAt;


        @Column(name = "updated_at")
        private LocalDateTime updatedAt;
        private String profilePictureUrl;

        public String getProfilePictureUrl() {
            return this.profilePictureUrl; // Ensure this matches your class variable
        }



        public enum Status {
            ACTIVE,
            INACTIVE,
            PENDING,
            REJECTED
        }

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.deletedAt = null;
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }



        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(String emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getContactNo() {
            return contactNo;
        }

        public void setContactNo(String contactNo) {
            this.contactNo = contactNo;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public byte[] getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(byte[] profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public void softDelete() {
            this.deletedAt = LocalDateTime.now();
        }


        public void setDeletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

