# Employee Management Service

A Spring Boot REST API for managing employees, departments, and user authentication with role-based access control. The system supports three roles (EMPLOYEE, HR, ADMIN) with JWT authentication, PostgreSQL persistence, and Flyway-managed database migrations.

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Security | 6.x |
| Spring Data JPA | 3.2.x |
| PostgreSQL | 16 |
| Flyway | 9.x |
| JWT (jjwt) | 0.11.5 |
| Springdoc OpenAPI | 2.5.0 |
| Lombok | latest |
| JaCoCo | 0.8.12 |
| JUnit 5 + Mockito | latest |
| Docker | multi-stage build |

## Quick Start with Docker

```bash
cp .env.example .env        # edit with your credentials
docker-compose up --build    # starts PostgreSQL + app
# API available at http://localhost:8080
```

## Running Locally (without Docker)

```bash
# Ensure PostgreSQL is running and set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/employee_management
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your_base64_encoded_secret_key

mvn clean install
java -jar target/employee-management-service-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT |

**Register:**
```json
POST /api/auth/register
{
  "email": "admin@company.com",
  "password": "password123",
  "role": "ADMIN"
}
```

**Login:**
```json
POST /api/auth/login
{
  "email": "admin@company.com",
  "password": "password123"
}
```

**Response (both endpoints):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@company.com",
  "role": "ADMIN"
}
```

### Departments

| Method | Endpoint | Roles | Description |
|--------|----------|-------|-------------|
| GET | `/api/departments` | EMPLOYEE, HR, ADMIN | List all departments |
| GET | `/api/departments/{id}` | EMPLOYEE, HR, ADMIN | Get department by ID |
| GET | `/api/departments/search?keyword=` | HR, ADMIN | Search departments |
| POST | `/api/departments` | ADMIN | Create department |
| PUT | `/api/departments/{id}` | ADMIN | Update department |

**Create Department:**
```json
POST /api/departments
{
  "name": "Marketing",
  "description": "Marketing and communications"
}
```

### Employees

| Method | Endpoint | Roles | Description |
|--------|----------|-------|-------------|
| GET | `/api/employees` | HR, ADMIN | List all employees |
| GET | `/api/employees/{id}` | HR, ADMIN | Get employee by ID |
| GET | `/api/employees/search?keyword=` | HR, ADMIN | Search by name |
| GET | `/api/employees/department/{id}` | HR, ADMIN | Filter by department |
| GET | `/api/employees/status/{status}` | HR, ADMIN | Filter by status |
| POST | `/api/employees` | HR, ADMIN | Create employee |
| PUT | `/api/employees/{id}` | HR, ADMIN | Update employee |
| PATCH | `/api/employees/{id}/status` | ADMIN | Change status |
| DELETE | `/api/employees/{id}` | ADMIN | Delete employee |

**Create Employee:**
```json
POST /api/employees
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "phone": "1234567890",
  "jobTitle": "Software Developer",
  "salary": 75000.00,
  "departmentId": 1
}
```

**Update Employee Status:**
```json
PATCH /api/employees/1/status
{
  "employmentStatus": "SUSPENDED"
}
```

## Swagger UI

Once the application is running, open your browser to:

```
http://localhost:8080/swagger-ui.html
```

To authenticate in Swagger:
1. Call `POST /api/auth/register` or `POST /api/auth/login` to get a token.
2. Click the **Authorize** button at the top of the page.
3. Enter the token (without the "Bearer " prefix).
4. All subsequent requests will include the JWT automatically.

## Role-Based Access Control

The system enforces three access levels:

**EMPLOYEE** — Read-only access to departments. Cannot view or manage employees. Intended for regular staff who only need to look up department information.

**HR** — Can view, search, create, and update employees. Can search departments. Cannot delete employees or change employment status. Intended for human resources staff managing day-to-day employee records.

**ADMIN** — Full access to all operations. Can create and update departments, delete employees, and change employment status (ACTIVE, INACTIVE, SUSPENDED). Intended for system administrators.

## Search Functionality

The search feature performs case-insensitive partial matching:

- **Employee search** (`/api/employees/search?keyword=john`) searches both first name and last name. A keyword of "john" matches "John Smith", "johnny Appleseed", and "Mary Johnson".
- **Department search** (`/api/departments/search?keyword=eng`) searches department names. A keyword of "eng" matches "Engineering".

## Testing

```bash
mvn clean test          # run all tests
mvn clean verify        # run tests + enforce 80% coverage
```

The project includes 72 tests:
- **Unit tests** for `DepartmentService` (12 tests) and `EmployeeService` (22 tests) covering happy paths and all exception scenarios.
- **Integration tests** for `AuthController` (5 tests), `DepartmentController` (12 tests), and `EmployeeController` (20 tests) verifying HTTP status codes, role-based access control, and error handling.

## Project Structure

```
src/main/java/com/employeemanagement/
├── config/              SecurityConfig, OpenApiConfig
├── controller/          AuthController, DepartmentController, EmployeeController
├── dto/                 Request/response DTOs
├── entity/              JPA entities (User, Department, Employee)
├── enums/               Role, EmploymentStatus
├── exception/           Custom exceptions, GlobalExceptionHandler
├── repository/          Spring Data JPA repositories
├── security/            JwtUtil, JwtAuthFilter
└── service/             AuthService, DepartmentService, EmployeeService

src/main/resources/
├── application.yml
└── db/migration/        Flyway SQL migrations (V1-V4)

src/test/java/com/employeemanagement/
├── controller/          Integration tests (MockMvc)
└── service/             Unit tests (Mockito)
```
