Patient Management System — Developer Documentation

1. Project Overview
Patient Management System is a microservices-based application designed to manage patients and user authentication. It consists of three main components:

1. API Gateway – Routes requests to services and validates JWT tokens.

2. Auth Service – Handles user signup, login, and token validation.

3. Patient Service – Handles patient CRUD operations.

4. Database – PostgreSQL databases for Auth and Patient services.

Tech Stack:
Java 17, Spring Boot, Spring Cloud Gateway
PostgreSQL
Docker & Docker Compose
JWT for authentication
OpenAPI / Swagger for API documentation

2. Architecture Diagram
                +------------------+
                |   API Gateway    |  (Port: 4004)
                |  JWT Validation  |
                +------------------+
                 |             |
                 v             v
      +----------------+   +----------------+
      |   Auth Service |   | Patient Service|
      |   (Port 4005)  |   |   (Port 4000) |
      +----------------+   +----------------+
         | DB: authdb           | DB: patientdb
         v                      v
      PostgreSQL             PostgreSQL

3. Microservices Overview
   
3.1 API Gateway
Port: 4004
Routes requests to Auth and Patient services.
Applies JWT validation to all protected routes via JwtValidationGatewayFilter.
Public endpoints (signup/login) bypass JWT validation.

Key Features:
Route /auth/signup and /auth/login → public
Route /auth/** (except public) → JWT protected
Route /api/patients/** → JWT protected


3.2 Auth Service
Port: 4005
Handles user authentication and token management.
Controllers & Endpoints:

Endpoint	Method	Description	Request Body	Response
/auth/signup	POST	Register a new user and generate access + refresh tokens	{ "email": "...", "password": "..." }	{ "accessToken": "...", "refreshToken": "..." }
/auth/login	POST	Login user and generate tokens	{ "email": "...", "password": "..." }	{ "accessToken": "...", "refreshToken": "..." }
/auth/validate/access	GET	Validate access token	Header: Authorization: Bearer <token>	200 OK / 401 Unauthorized
/auth/validate/refresh	GET	Validate refresh token	Header: Authorization: Bearer <token>	200 OK / 401 Unauthorized

Notes:
Signup and login are public — no token needed.
All other endpoints require a valid JWT access token.

3.3 Patient Service
Port: 4000
Handles patient management operations.
Controllers & Endpoints:
Endpoint	Method	Description	Request Body	Response
/api/patients	GET	Get all patients	None	List of patients
/api/patients	POST	Create a new patient	{ "name": "...", "age": 30, "diagnosis": "..." }	Created patient object
/api/patients/{id}	GET	Get patient by ID	None	Patient object
/api/patients/{id}	PUT	Update patient	Patient object	Updated patient
/api/patients/{id}	DELETE	Delete patient	None	Success message

Notes:
All endpoints require a valid JWT token in Authorization header.


4. Database
Auth Service DB: authdb (PostgreSQL)
Patient Service DB: patientdb (PostgreSQL)
Tables Example:
users (Auth Service): id, email, passwordHash, createdAt
patients (Patient Service): id, name, age, diagnosis, createdAt

5. How to Run Locally
Clone the repository:
git clone <your-repo-url>
cd patient-management-system

Start all services with Docker Compose:
docker-compose up --build


Access endpoints:
API Gateway: http://localhost:4004
Auth Service Swagger: http://localhost:4005/swagger-ui.html
Patient Service Swagger: http://localhost:4000/swagger-ui.html

6. How to Test

Signup:
POST http://localhost:4004/auth/signup
Body: { "email": "test@example.com", "password": "password123" }

Login:
POST http://localhost:4004/auth/login
Body: { "email": "test@example.com", "password": "password123" }

Access Patient Endpoint:
Copy the accessToken from login response

Call a protected endpoint with:
GET http://localhost:4004/api/patients
Header: Authorization: Bearer <accessToken>

7. API Documentation
Auth API Docs: http://localhost:4004/api-docs/auth
Patient API Docs: http://localhost:4004/api-docs/patients
Swagger UI is enabled for both services.

8. Additional Notes
JWT token expiry: Access tokens expire in 30 minutes, refresh tokens in 10 minutes.

Future improvements:

Add more microservices (Appointments, Billing).

CI/CD deployment with Docker Compose → ECS/EKS.

Detailed Postman collection with test cases.
