package com.rnclasses.dto;

import java.time.LocalDateTime;

public class RegisterRequest {

    // Common fields for all users
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String role; // STUDENT, TRAINER, ADMIN
    
    // Student specific fields
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private LocalDateTime dateOfBirth;
    private String gender;
    
    // Trainer specific fields
    private String expertise;
    private String qualification;
    private Integer yearsOfExperience;
    private String bio;
    private String linkedinUrl;
    private String githubUrl;
    
    // Admin specific fields
    private String employeeId;
    private String department;
    private String designation;
    private LocalDateTime joiningDate;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "STUDENT"; // Default role
    }

    // Student constructor
    public RegisterRequest(String name, String email, String password, String phoneNumber, 
                          String address, String city, String state, String country, 
                          String pincode, LocalDateTime dateOfBirth, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = "STUDENT";
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Trainer constructor
    public RegisterRequest(String name, String email, String password, String phoneNumber,
                          String expertise, String qualification, Integer yearsOfExperience,
                          String bio, String linkedinUrl, String githubUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = "TRAINER";
        this.expertise = expertise;
        this.qualification = qualification;
        this.yearsOfExperience = yearsOfExperience;
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
    }

    // Admin constructor
    public RegisterRequest(String name, String email, String password, String phoneNumber,
                          String employeeId, String department, String designation, 
                          LocalDateTime joiningDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = "ADMIN";
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
    }

    // Getters and Setters for all fields

    // Basic fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Student fields
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // Trainer fields
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    // Admin fields
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public LocalDateTime getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDateTime joiningDate) { this.joiningDate = joiningDate; }

    // Helper method to validate based on role
    public boolean isValidForRole() {
        if (role == null) return false;
        
        switch (role) {
            case "STUDENT":
                return name != null && email != null && password != null && 
                       password.length() >= 6;
            
            case "TRAINER":
                return name != null && email != null && password != null && 
                       password.length() >= 6 && expertise != null;
            
            case "ADMIN":
                return name != null && email != null && password != null && 
                       password.length() >= 6 && employeeId != null;
            
            default:
                return false;
        }
    }

    // Get required fields for role (useful for error messages)
    public String getRequiredFields() {
        switch (role) {
            case "STUDENT":
                return "name, email, password (min 6 chars)";
            case "TRAINER":
                return "name, email, password (min 6 chars), expertise";
            case "ADMIN":
                return "name, email, password (min 6 chars), employeeId";
            default:
                return "name, email, password";
        }
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}