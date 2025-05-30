# MediConnect - Telemedicine Platform

## 🏥 Overview
MediConnect is a comprehensive telemedicine platform that enables video consultations between patients and doctors. The platform consists of a Spring Boot backend API and a React Native mobile application, providing a complete solution for remote healthcare services.

## 🚀 One-Click Deployment
[![Deploy to DigitalOcean](https://www.deploytodo.com/do-btn-blue.svg)](https://cloud.digitalocean.com/apps/new?repo=https://github.com/latifox/mediconnect_app/tree/main)

Deploy MediConnect with a single click using DigitalOcean App Platform. This will automatically set up:
- Backend Spring Boot API
- Frontend Expo application
- PostgreSQL database
- All necessary environment variables

*Note: You'll need a DigitalOcean account. Sign up [here](https://www.digitalocean.com/try/free-trial-offer) for a $200 free credit.*

## 🚀 Features

### For Patients
- **Doctor Search**: Find doctors by specialty, location, and availability
- **Appointment Booking**: Schedule consultations with preferred doctors
- **Video Consultations**: High-quality video calls with WebRTC technology
- **Secure Messaging**: Chat with healthcare providers
- **Prescription Management**: View and download digital prescriptions
- **Medical History**: Access past consultations and medical records

### For Doctors
- **Patient Management**: View patient profiles and medical history
- **Schedule Management**: Manage availability and appointments
- **Video Consultations**: Conduct remote consultations
- **Prescription Writing**: Create and send digital prescriptions
- **Patient Communication**: Secure messaging with patients
- **Consultation Notes**: Document patient interactions

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│  React Native   │◄──►│  Spring Boot    │◄──►│   PostgreSQL    │
│  Mobile App     │    │   Backend API   │    │    Database     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                        │
        │                        │
        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │
│   WebRTC for    │    │   WebSocket     │
│  Video Calls    │    │  for Real-time  │
│                 │    │   Messaging     │
└─────────────────┘    └─────────────────┘
```

## 📁 Project Structure

```
mediconnect/
├── mediconnect-backend/     # Spring Boot API
│   ├── src/main/java/
│   │   └── com/mediconnect/backend/
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Business Logic
│   │       ├── repository/  # Data Access Layer
│   │       ├── model/       # Entity Models
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── security/    # JWT & Security Config
│   │       └── config/      # Application Configuration
│   ├── src/main/resources/
│   └── pom.xml
├── mediconnect-frontend/    # React Native App
│   ├── src/
│   │   ├── screens/         # Screen Components
│   │   ├── navigation/      # Navigation Setup
│   │   ├── services/        # API Services
│   │   ├── context/         # State Management
│   │   ├── types/           # TypeScript Types
│   │   └── components/      # Reusable Components
│   ├── App.tsx
│   └── package.json
└── README.md
```

## 🛠️ Technology Stack

### Backend (Spring Boot)
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Primary database
- **JWT** - Token-based authentication
- **WebSocket** - Real-time communication
- **Maven** - Dependency management
- **Swagger/OpenAPI** - API documentation

### Frontend (React Native)
- **React Native** - Mobile app framework
- **Expo** - Development platform
- **TypeScript** - Type-safe JavaScript
- **React Navigation** - Navigation library
- **Axios** - HTTP client
- **AsyncStorage** - Local storage
- **React Context** - State management
- **WebRTC** - Video calling

### Database Schema
- **Users** (patients & doctors)
- **Appointments** (consultation bookings)
- **Messages** (chat communications)
- **Prescriptions** (medical prescriptions)

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Node.js 16+**
- **PostgreSQL 12+**
- **Maven 3.6+**
- **Expo CLI**

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mediconnect
```

### 2. Setup Backend
```bash
cd mediconnect-backend

# Configure database in application.properties
# Create PostgreSQL database: mediconnect_db

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

### 3. Setup Frontend
```bash
cd mediconnect-frontend

# Install dependencies
npm install

# Start development server
npm start

# Run on device/emulator
npm run android  # or npm run ios
```

### 4. Database Setup
```sql
CREATE DATABASE mediconnect_db;
CREATE USER mediconnect_user WITH PASSWORD 'mediconnect_password';
GRANT ALL PRIVILEGES ON DATABASE mediconnect_db TO mediconnect_user;
```

## 📚 API Documentation

Once the backend is running, access the interactive API documentation:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

### Key Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration
- `GET /api/users/doctors` - List all doctors
- `POST /api/appointments` - Create appointment
- `GET /api/appointments/patient/{id}` - Get patient appointments
- `POST /api/messages` - Send message
- `POST /api/prescriptions` - Create prescription

## 🔐 Security Features

- **JWT Authentication** - Secure token-based auth
- **Password Encryption** - BCrypt hashing
- **Role-based Access** - Patient/Doctor permissions
- **CORS Configuration** - Cross-origin request handling
- **Input Validation** - Request data validation
- **Secure Storage** - Encrypted local storage

## 🧪 Testing

### Backend Testing
```bash
cd mediconnect-backend
mvn test
```

### Frontend Testing
```bash
cd mediconnect-frontend
npm test
```

## 🚀 Deployment

### One-Click Cloud Deployment
[![Deploy to DigitalOcean](https://www.deploytodo.com/do-btn-blue.svg)](https://cloud.digitalocean.com/apps/new?repo=https://github.com/latifox/mediconnect_app/tree/main)

For detailed DigitalOcean deployment instructions, see [DIGITAL_OCEAN_DEPLOYMENT.md](DIGITAL_OCEAN_DEPLOYMENT.md).

### Docker Deployment
```bash
# Build and start containers
docker-compose up -d
```

### Backend Deployment
```bash
# Build JAR file
mvn clean package

# Run with Docker
docker build -t mediconnect-backend ./mediconnect-backend
docker run -p 8080:8080 mediconnect-backend
```

### Frontend Deployment
```bash
# Build for production
cd mediconnect-frontend
expo build:android
expo build:ios

# Publish to Expo
expo publish
```

## 🔧 Configuration

### Backend Configuration
Update `mediconnect-backend/src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Frontend Configuration
Update `mediconnect-frontend/src/services/api.ts`:
```typescript
const API_BASE_URL = 'http://your-backend-url:8080/api';
```

## 📱 Mobile App Features

### Authentication Flow
1. Login/Register screen
2. Role selection (Patient/Doctor)
3. Profile setup
4. Main dashboard

### Core Functionality
- **Dashboard**: Role-specific home screen
- **Search**: Find doctors by specialty
- **Booking**: Schedule appointments
- **Video Calls**: WebRTC consultations
- **Messaging**: Real-time chat
- **Prescriptions**: Digital prescriptions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow coding standards for both Java and TypeScript
- Write unit tests for new features
- Update documentation for API changes
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation in each module

## 🗺️ Roadmap

### Phase 1 (Current)
- [x] Basic authentication system
- [x] User management (patients/doctors)
- [x] Appointment booking system
- [x] Basic messaging functionality

### Phase 2 (In Progress)
- [x] Video consultation implementation
- [x] Prescription management
- [ ] File upload/sharing
- [ ] Push notifications

### Phase 3 (Planned)
- [ ] Payment integration
- [x] Advanced search filters
- [ ] Multi-language support
- [ ] Offline capabilities
- [ ] Analytics dashboard

## 🏆 Acknowledgments

- Spring Boot community for excellent documentation
- React Native and Expo teams for mobile development tools
- WebRTC community for video calling capabilities
- All contributors and testers

---

**MediConnect** - Connecting Health, Connecting Lives 🏥💙 
