# MediConnect Backend

## Description
MediConnect is a telemedicine application backend built with Spring Boot that enables video consultations between patients and doctors. The application provides REST APIs for user management, appointment scheduling, messaging, prescriptions, and WebRTC signaling for video calls.

## Features
- **User Authentication & Authorization** (JWT-based)
- **Role-based Access Control** (Patient/Doctor)
- **Appointment Management**
- **Real-time Messaging**
- **Prescription Management**
- **WebRTC Signaling for Video Calls**
- **File Upload Support**
- **API Documentation with Swagger**

## Technology Stack
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL** Database
- **WebSocket** for real-time communication
- **Maven** for dependency management
- **Swagger/OpenAPI** for API documentation

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mediconnect-backend
```

### 2. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE mediconnect_db;
CREATE USER mediconnect_user WITH PASSWORD 'mediconnect_password';
GRANT ALL PRIVILEGES ON DATABASE mediconnect_db TO mediconnect_user;
```

### 3. Configuration
Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build and Run
```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation
Once the application is running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/validate` - Validate JWT token

### Users
- `GET /api/users/doctors` - Get all doctors
- `GET /api/users/doctors/speciality/{speciality}` - Get doctors by speciality
- `GET /api/users/specialities` - Get all specialities
- `GET /api/users/search?q={searchTerm}` - Search doctors

### Appointments
- `POST /api/appointments` - Create appointment
- `GET /api/appointments/doctor/{doctorId}` - Get doctor's appointments
- `GET /api/appointments/patient/{patientId}` - Get patient's appointments
- `PUT /api/appointments/{id}/status` - Update appointment status

### Messages
- `POST /api/messages` - Send message
- `GET /api/messages/conversation/{userId}` - Get conversation with user
- `GET /api/messages/unread` - Get unread messages

### Prescriptions
- `POST /api/prescriptions` - Create prescription
- `GET /api/prescriptions/patient/{patientId}` - Get patient's prescriptions
- `GET /api/prescriptions/doctor/{doctorId}` - Get doctor's prescriptions

### WebSocket Endpoints
- `/ws/signal` - WebRTC signaling for video calls

## Environment Variables
```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mediconnect_db
SPRING_DATASOURCE_USERNAME=mediconnect_user
SPRING_DATASOURCE_PASSWORD=mediconnect_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:19006
```

## Database Schema
The application uses the following main entities:
- **Users** (patients and doctors)
- **Appointments**
- **Messages**
- **Prescriptions**

## Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Deployment
### Using Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/mediconnect-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build and run:
```bash
mvn clean package
docker build -t mediconnect-backend .
docker run -p 8080:8080 mediconnect-backend
```

## Security
- JWT-based authentication
- Password encryption using BCrypt
- CORS configuration for cross-origin requests
- Input validation and sanitization
- Role-based access control

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License.

## Support
For support and questions, please contact the development team. 