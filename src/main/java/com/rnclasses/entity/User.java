package com.rnclasses.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "STUDENT";  // STUDENT, TRAINER, ADMIN

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "profile_image")
    private String profileImage;

    // ========== TRAINER SPECIFIC FIELDS ==========
    @Column(name = "expertise", length = 255)
    private String expertise;

    @Column(name = "qualification", length = 255)
    private String qualification;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    // ========== ADMIN SPECIFIC FIELDS ==========
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "joining_date")
    private LocalDateTime joiningDate;

    // ========== COMMON FIELDS ==========
    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    // ========== RELATIONSHIPS ==========
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Course> coursesCreated = new ArrayList<>();

    // ========== CONSTRUCTORS ==========
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Full constructor for Student
    public User(String name, String email, String password, String role, 
                String phoneNumber, String address, String city, String state, 
                String country, String pincode, LocalDateTime dateOfBirth, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Constructor for Trainer
    public User(String name, String email, String password, String role,
                String phoneNumber, String expertise, String qualification,
                Integer yearsOfExperience, String bio, String linkedinUrl, String githubUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.expertise = expertise;
        this.qualification = qualification;
        this.yearsOfExperience = yearsOfExperience;
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Constructor for Admin
    public User(String name, String email, String password, String role,
                String phoneNumber, String employeeId, String department,
                String designation, LocalDateTime joiningDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // ========== LIFECYCLE CALLBACKS ==========
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ========== CUSTOM METHODS ==========
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setUser(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setUser(null);
    }

    public void addCourse(Course course) {
        coursesCreated.add(course);
        course.setTrainer(this);
    }

    public void removeCourse(Course course) {
        coursesCreated.remove(course);
        course.setTrainer(null);
    }

    // Helper method to check user type
    public boolean isStudent() {
        return "STUDENT".equals(this.role);
    }

    public boolean isTrainer() {
        return "TRAINER".equals(this.role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    // ========== GETTERS AND SETTERS ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonIgnore
    public String getPassword() { return password; }
    @JsonProperty
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    // Trainer Getters and Setters
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

    // Admin Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public LocalDateTime getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDateTime joiningDate) { this.joiningDate = joiningDate; }

    // Common Getters and Setters
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

    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }

    public List<Course> getCoursesCreated() { return coursesCreated; }
    public void setCoursesCreated(List<Course> coursesCreated) { this.coursesCreated = coursesCreated; }

    // ========== TO STRING ==========
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", phoneNumber='" + phoneNumber + '\'' +
                (expertise != null ? ", expertise='" + expertise + '\'' : "") +
                (employeeId != null ? ", employeeId='" + employeeId + '\'' : "") +
                '}';
    }
}