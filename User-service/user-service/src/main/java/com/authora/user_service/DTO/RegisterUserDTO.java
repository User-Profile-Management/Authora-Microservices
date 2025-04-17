package com.authora.user_service.DTO;

import java.time.LocalDate;

public class RegisterUserDTO {

        private String googleId;
        private String fullName;
        private String email;
        private String password;
        private String contactNo;
        private String emergencyContact;
        private LocalDate dateOfBirth;
        private String address;
        private String profilePictureBase64;
        private String roleName;


        public String getGoogleId() { return googleId; }
        public void setGoogleId(String googleId) { this.googleId = googleId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getContactNo() { return contactNo; }
        public void setContactNo(String contactNo) { this.contactNo = contactNo; }

        public String getEmergencyContact() { return emergencyContact; }
        public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getProfilePictureBase64() {
            return profilePictureBase64;
        }

        public void setProfilePictureBase64(String profilePictureBase64) {
            this.profilePictureBase64 = profilePictureBase64;
        }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public static class UpdatePasswordRequest {
            private String email;
            private String oldPassword;
            private String newPassword;


            public UpdatePasswordRequest() {}

            public UpdatePasswordRequest(String email, String oldPassword, String newPassword) {
                this.email = email;
                this.oldPassword = oldPassword;
                this.newPassword = newPassword;
            }


            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getOldPassword() { return oldPassword; }
            public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

            public String getNewPassword() { return newPassword; }
            public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        }

    }

