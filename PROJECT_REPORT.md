# Student Management System
## Project Report

---

**Course Code:** [TO BE FILLED]  
**Date:** [TO BE FILLED]  
**Team Members:**
- [Member 1 Name] - ID: [ID 1]
- [Member 2 Name] - ID: [ID 2]
- [Member 3 Name] - ID: [ID 3]

---

## 1. Introduction

### Overview
The Student Management System is a comprehensive RESTful API-based application designed to manage student information, course details, and course registrations. The system provides role-based access control, allowing administrators to manage students and courses, while students can view their information and register for courses.

### Objectives
- Develop a secure, scalable student management system
- Implement role-based authentication and authorization (Admin and Student roles)
- Provide RESTful API endpoints for CRUD operations on students and courses
- Enable students to register for courses with proper validation
- Ensure data integrity and security through JWT-based authentication
- Follow best practices in Spring Boot development with clean architecture

### Technology Stack
- **Framework:** Spring Boot 4.0.1
- **Language:** Java 25
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Security:** Spring Security with JWT (JSON Web Tokens)
- **Build Tool:** Maven
- **Validation:** Jakarta Validation API
- **Utilities:** Lombok

---

## 2. System Features & Requirements

### Functional Requirements

#### 2.1 Authentication & Authorization
- User login with email and password
- JWT-based stateless authentication
- Role-based access control (ADMIN and STUDENT roles)
- Password encryption using BCrypt

#### 2.2 Student Management
- Admin can create students with login credentials
- Auto-generation of unique student IDs (format: STU001, STU002, etc.)
- Admin can view, update, and delete students
- Students can view and update their own information
- Email validation and uniqueness enforcement

#### 2.3 Course Management
- Admin can create, read, update, and delete courses
- Course code uniqueness enforcement
- Course information includes: code, name, description, and credits
- Validation for course credits (must be positive)

#### 2.4 Course Registration
- Students can register for courses using student ID and course code
- Students can unregister from courses
- Students can view their registered courses
- Prevention of duplicate registrations
- Automatic registration date tracking
- Students can only manage their own registrations

### Non-Functional Requirements
- RESTful API design
- Secure authentication and authorization
- Input validation and error handling
- Database transaction management
- Clean code architecture (Entity, Repository, Service, Controller layers)
- Separation of concerns between User (authentication) and Student (data)

---

## 3. ERD / Database Schema

### Database Diagram
*[Screenshot of database schema/ERD to be added]*

### Schema Explanation

The database consists of four main tables:

#### 3.1 Users Table
**Purpose:** Stores authentication information for all users (both admins and students)

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| email | VARCHAR(100) | NOT NULL, UNIQUE | User email (used for login) |
| password | VARCHAR | NOT NULL | BCrypt hashed password |
| role | VARCHAR(20) | NOT NULL | User role (ADMIN or STUDENT) |

**Relationships:**
- One-to-One with Students table (via user_id foreign key in students table)

#### 3.2 Students Table
**Purpose:** Stores student-specific information

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| first_name | VARCHAR(50) | NOT NULL | Student's first name |
| last_name | VARCHAR(50) | NOT NULL | Student's last name |
| email | VARCHAR(100) | NOT NULL, UNIQUE | Student's email |
| student_id | VARCHAR(10) | NOT NULL, UNIQUE | Auto-generated student ID (STU001, STU002, etc.) |
| user_id | BIGINT | NULLABLE, FOREIGN KEY | Reference to users.id (nullable) |

**Relationships:**
- Many-to-One with Users table (via user_id)
- One-to-Many with Course_Registrations table

#### 3.3 Courses Table
**Purpose:** Stores course information

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| course_code | VARCHAR(20) | NOT NULL, UNIQUE | Course code (e.g., CS101, MATH201) |
| course_name | VARCHAR(100) | NOT NULL | Course name |
| description | VARCHAR(500) | NULLABLE | Course description |
| credits | INTEGER | NOT NULL | Number of credits |

**Relationships:**
- One-to-Many with Course_Registrations table

#### 3.4 Course_Registrations Table
**Purpose:** Join table for many-to-many relationship between Students and Courses

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| student_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to students.id |
| course_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to courses.id |
| registration_date | TIMESTAMP | NOT NULL | Date and time of registration |

**Relationships:**
- Many-to-One with Students table
- Many-to-One with Courses table
- Unique constraint on (student_id, course_id) to prevent duplicate registrations

### Entity Relationships Summary
```
Users (1) ──────── (0..1) Students
                              │
                              │ (1)
                              │
                              │ (N)
                    Course_Registrations
                              │
                              │ (N)
                              │
                              │ (1)
                            Courses
```

**Key Design Decisions:**
1. **Separation of User and Student:** Authentication (User table) is separated from student data (Students table) for better security and flexibility
2. **Nullable user_id:** Allows for students without login accounts (though current implementation requires user accounts)
3. **Many-to-Many via Join Table:** Course_Registrations table enables students to register for multiple courses
4. **Unique Constraints:** Prevent duplicate emails, student IDs, course codes, and registrations

---

## 4. API Endpoints Table

### 4.1 Authentication Endpoints

| Method | URL | Description | Authorization |
|--------|-----|-------------|---------------|
| POST | `/api/auth/login` | User login (returns JWT token) | Public |

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "user@example.com",
  "role": "STUDENT",
  "studentId": "STU001"
}
```

### 4.2 Student Management Endpoints

| Method | URL | Description | Authorization |
|--------|-----|-------------|---------------|
| POST | `/api/students` | Create a new student with login credentials | ADMIN only |
| GET | `/api/students` | Get all students | ADMIN only |
| GET | `/api/students/{id}` | Get student by ID | ADMIN or own record (STUDENT) |
| PUT | `/api/students/{id}` | Update student information | ADMIN or own record (STUDENT) |
| DELETE | `/api/students/{id}` | Delete a student | ADMIN only |

**Create Student Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

### 4.3 Course Management Endpoints

| Method | URL | Description | Authorization |
|--------|-----|-------------|---------------|
| POST | `/api/courses` | Create a new course | ADMIN only |
| GET | `/api/courses` | Get all courses | ADMIN only |
| GET | `/api/courses/{id}` | Get course by ID | ADMIN only |
| PUT | `/api/courses/{id}` | Update course information | ADMIN only |
| DELETE | `/api/courses/{id}` | Delete a course | ADMIN only |

**Create Course Request Body:**
```json
{
  "courseCode": "CS101",
  "courseName": "Introduction to Computer Science",
  "description": "Fundamentals of computer science",
  "credits": 3
}
```

### 4.4 Course Registration Endpoints

| Method | URL | Description | Authorization |
|--------|-----|-------------|---------------|
| POST | `/api/students/{studentId}/courses/{courseCode}/register` | Register student for a course | ADMIN or own record (STUDENT) |
| DELETE | `/api/students/{studentId}/courses/{courseCode}/unregister` | Unregister student from a course | ADMIN or own record (STUDENT) |
| GET | `/api/students/{studentId}/courses` | Get all courses for a student | ADMIN or own record (STUDENT) |

**Example URLs:**
- Register: `/api/students/STU001/courses/CS101/register`
- Unregister: `/api/students/STU001/courses/CS101/unregister`
- Get courses: `/api/students/STU001/courses`

### Authentication Header
All protected endpoints require the JWT token in the Authorization header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 5. Authentication Explanation

### JWT (JSON Web Token) Authentication Process

#### 5.1 Overview
The system uses JWT for stateless authentication. JWT tokens contain encoded information about the user and are signed to prevent tampering.

#### 5.2 Authentication Flow

1. **Login Request**
   - Client sends POST request to `/api/auth/login` with email and password
   - No authentication required for this endpoint

2. **Authentication & Validation**
   - `AuthService` receives the login request
   - `AuthenticationManager` validates credentials against the `User` table
   - Password is verified using BCrypt password encoder

3. **Token Generation**
   - Upon successful authentication, `JwtUtil` generates a JWT token
   - Token contains:
     - Subject: User's email
     - Claim: User's role (ADMIN or STUDENT)
     - Issued at: Current timestamp
     - Expiration: Current time + expiration period (configured in application.yaml)

4. **Token Response**
   - Server returns the JWT token along with user information
   - Client stores the token (typically in localStorage or sessionStorage)

5. **Subsequent Requests**
   - Client includes the token in the Authorization header: `Bearer <token>`
   - `JwtAuthenticationFilter` intercepts the request
   - Filter validates the token signature and expiration
   - If valid, extracts user information and sets up Spring Security context

6. **Authorization Check**
   - `@PreAuthorize` annotations check user roles and permissions
   - Method-level security ensures only authorized users can access protected endpoints

#### 5.3 Security Components

**JwtUtil:**
- Generates tokens with user email and role
- Validates token signatures
- Extracts claims from tokens
- Checks token expiration

**JwtAuthenticationFilter:**
- Intercepts HTTP requests
- Extracts JWT token from Authorization header
- Validates token and loads user details
- Sets up Spring Security authentication context

**CustomUserDetailsService:**
- Implements Spring Security's `UserDetailsService`
- Loads user details from the `User` table
- Creates Spring Security `User` object with authorities based on role

**SecurityConfig:**
- Configures Spring Security
- Sets up password encoder (BCrypt)
- Configures authentication provider
- Defines security filter chain with JWT filter
- Sets session management to STATELESS

#### 5.4 Password Security
- Passwords are hashed using BCrypt algorithm
- BCrypt automatically generates salt for each password
- Original passwords are never stored in the database
- Password verification is done by comparing hashed values

---

## 6. Business Logic Implementation

### 6.1 Student ID Generation
- Student IDs are auto-generated in the format STU001, STU002, STU003, etc.
- Logic in `StudentService.generateStudentId()`:
  1. Queries database for the highest existing student ID
  2. Extracts the numeric portion
  3. Increments by 1
  4. Formats with leading zeros (e.g., STU001, STU002)

### 6.2 User-Student Separation
- **Design Pattern:** Separate authentication (User) from business data (Student)
- **Benefits:**
  - Cleaner separation of concerns
  - Admins have User records but no Student records
  - Students have both User and Student records, linked via `user_id`
  - Better security isolation

### 6.3 Course Registration Logic
- **Registration Process:**
  1. Validate student exists (by studentId string)
  2. Validate course exists (by courseCode string)
  3. Check for duplicate registration (unique constraint)
  4. Create CourseRegistration record with current timestamp
  5. Save to database

- **Unregistration Process:**
  1. Validate student and course exist
  2. Check if registration exists
  3. Delete CourseRegistration record

- **Authorization:**
  - Students can only register/unregister for their own courses
  - Admins can manage any student's registrations
  - Implemented via `@PreAuthorize` with `isOwnStudentId()` method

### 6.4 Data Validation
- **Input Validation:**
  - Jakarta Validation API annotations on entities and DTOs
  - `@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Positive`
  - Automatic validation at controller level with `@Valid`

- **Business Rule Validation:**
  - Email uniqueness (User and Student tables)
  - Student ID uniqueness
  - Course code uniqueness
  - Duplicate registration prevention
  - Custom exceptions: `DuplicateResourceException`, `ResourceNotFoundException`

### 6.5 Exception Handling
- **GlobalExceptionHandler:**
  - Centralized exception handling
  - Custom error responses for validation errors
  - Consistent error format across the API
  - Returns appropriate HTTP status codes

### 6.6 Transaction Management
- Service layer methods annotated with `@Transactional`
- Ensures data consistency
- Read-only transactions for query operations
- Automatic rollback on exceptions

---

## 7. Screenshots

### 7.1 API Testing (Postman/Swagger)
*[Screenshots to be added:]* 
- Login endpoint test
- Student creation endpoint test
- Course creation endpoint test
- Course registration endpoint test
- Authentication with JWT token
- Error responses

### 7.2 Database Tables
*[Screenshots to be added:]* 
- Users table structure and sample data
- Students table structure and sample data
- Courses table structure and sample data
- Course_Registrations table structure and sample data
- Database relationships/foreign keys

### 7.3 Optional: Frontend Pages
*[Screenshots to be added if frontend exists]*

---

## 8. Team Roles & Contributions

### Member 1: [Name] - [ID]
**Responsibilities:**
- Authentication & Security Implementation
  - JWT token generation and validation
  - Spring Security configuration
  - CustomUserDetailsService implementation
  - JwtAuthenticationFilter development
  - Password encryption setup
- User Entity & Repository
  - User entity design and implementation
  - UserRepository with query methods
  - Admin seeder implementation
- Authentication Controller
  - Login endpoint implementation
  - LoginRequestDTO and LoginResponseDTO design

**Technical Contributions:**
- Implemented JWT-based authentication system
- Configured Spring Security for stateless authentication
- Created user authentication and authorization infrastructure

---

### Member 2: [Name] - [ID]
**Responsibilities:**
- Student Management System
  - Student entity design (separation from User entity)
  - StudentRepository with custom query methods
  - StudentService with CRUD operations
  - Student ID auto-generation logic
  - StudentController implementation
- DTOs Design
  - StudentRequestDTO, StudentResponseDTO
  - StudentCreateWithPasswordDTO
- Exception Handling
  - Custom exception classes (ResourceNotFoundException, DuplicateResourceException)
  - GlobalExceptionHandler implementation
- Business Logic
  - User-Student relationship management
  - Student data validation and business rules

**Technical Contributions:**
- Implemented complete student CRUD functionality
- Designed and implemented DTOs for data transfer
- Created exception handling framework
- Implemented student ID auto-generation algorithm

---

### Member 3: [Name] - [ID]
**Responsibilities:**
- Course Management System
  - Course entity design and implementation
  - CourseRepository with query methods
  - CourseService with CRUD operations
  - CourseController implementation
  - CourseRequestDTO and CourseResponseDTO design
- Course Registration System
  - CourseRegistration entity (join table) design
  - CourseRegistrationRepository implementation
  - CourseRegistrationService with registration logic
  - CourseRegistrationController implementation
  - CourseRegistrationResponseDTO design
- API Design
  - RESTful endpoint design for courses and registrations
  - String-based identifier support (studentId, courseCode)
- Database Schema
  - ERD design and documentation
  - Database relationship implementation
  - Foreign key constraints and unique constraints

**Technical Contributions:**
- Implemented complete course management system
- Designed and implemented course registration functionality
- Created many-to-many relationship between Students and Courses
- Implemented user-friendly API endpoints with string identifiers

---

## 9. Conclusion

### Summary
The Student Management System successfully implements a secure, scalable RESTful API for managing students, courses, and course registrations. The system follows best practices in Spring Boot development with clean architecture, proper separation of concerns, and comprehensive security measures.

### Key Achievements
- ✅ Secure JWT-based authentication and authorization
- ✅ Complete CRUD operations for students and courses
- ✅ Role-based access control (Admin and Student)
- ✅ Course registration system with proper validation
- ✅ Clean architecture with layered design
- ✅ Comprehensive error handling and validation
- ✅ Database design with proper relationships and constraints

### Future Enhancements
- Frontend implementation (web/mobile application)
- Course capacity management
- Grade management system
- Course prerequisites validation
- Email notifications
- Report generation features
- Search and filtering capabilities
- Pagination for large datasets

---

**End of Report**
