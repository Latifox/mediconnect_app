# MediConnect Setup Guide

## 🚀 Complete Setup Instructions

This guide will help you set up the complete MediConnect telemedicine platform on your local machine.

## 📋 Prerequisites

Before starting, ensure you have the following installed:

### Required Software
- **Java 17+** - [Download here](https://adoptium.net/)
- **Node.js 16+** - [Download here](https://nodejs.org/)
- **PostgreSQL 12+** - [Download here](https://www.postgresql.org/download/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **Git** - [Download here](https://git-scm.com/)

### For Mobile Development
- **Expo CLI** - Install with `npm install -g @expo/cli`
- **Android Studio** (for Android development)
- **Xcode** (for iOS development - macOS only)

## 🗂️ Project Structure Overview

```
mediconnect/
├── mediconnect-backend/          # Spring Boot API
│   ├── src/main/java/com/mediconnect/backend/
│   │   ├── controller/           # REST API Controllers
│   │   ├── service/              # Business Logic Services
│   │   ├── repository/           # Data Access Layer
│   │   ├── model/                # JPA Entity Models
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── security/             # JWT Security Configuration
│   │   └── config/               # Application Configuration
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── mediconnect-frontend/         # React Native Mobile App
│   ├── src/
│   │   ├── screens/              # Screen Components
│   │   ├── navigation/           # Navigation Configuration
│   │   ├── services/             # API Services
│   │   ├── context/              # React Context (State Management)
│   │   ├── types/                # TypeScript Type Definitions
│   │   └── components/           # Reusable UI Components
│   ├── App.tsx                   # Main App Component
│   └── package.json
├── README.md                     # Main Project Documentation
└── SETUP_GUIDE.md               # This setup guide
```

## 🛠️ Step-by-Step Setup

### Step 1: Clone the Repository

```bash
git clone <your-repository-url>
cd mediconnect
```

### Step 2: Database Setup

1. **Start PostgreSQL service**
2. **Create database and user:**

```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create database
CREATE DATABASE mediconnect_db;

-- Create user
CREATE USER mediconnect_user WITH PASSWORD 'mediconnect_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mediconnect_db TO mediconnect_user;

-- Exit psql
\q
```

### Step 3: Backend Setup (Spring Boot)

```bash
# Navigate to backend directory
cd mediconnect-backend

# Verify Java version
java -version

# Verify Maven installation
mvn -version

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

**Backend will be available at:** `http://localhost:8080`

**API Documentation:** `http://localhost:8080/api/swagger-ui.html`

### Step 4: Frontend Setup (React Native)

```bash
# Navigate to frontend directory (from project root)
cd mediconnect-frontend

# Verify Node.js version
node -version

# Install dependencies
npm install

# Start Expo development server
npm start
```

### Step 5: Running the Mobile App

After starting the Expo server, you have several options:

#### Option 1: Expo Go App (Recommended for testing)
1. Install Expo Go on your mobile device
2. Scan the QR code displayed in the terminal
3. The app will load on your device

#### Option 2: Android Emulator
```bash
npm run android
```

#### Option 3: iOS Simulator (macOS only)
```bash
npm run ios
```

#### Option 4: Web Browser
```bash
npm run web
```

## 🔧 Configuration

### Backend Configuration

Edit `mediconnect-backend/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration (Update if needed)
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_db
spring.datasource.username=mediconnect_user
spring.datasource.password=mediconnect_password

# JWT Configuration (Change secret in production)
jwt.secret=mediconnect-secret-key-for-jwt-token-generation-2024
jwt.expiration=86400000

# CORS Configuration (Update for production)
cors.allowed-origins=http://localhost:3000,http://localhost:19006
```

### Frontend Configuration

Edit `mediconnect-frontend/src/services/api.ts`:

```typescript
// Update this URL to match your backend
const API_BASE_URL = 'http://localhost:8080/api';
```

## ✅ Verification Steps

### 1. Backend Verification

Test the backend API:

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# API documentation
curl http://localhost:8080/api/api-docs
```

### 2. Frontend Verification

1. Open the mobile app
2. You should see the MediConnect login screen
3. Navigation should work between screens

### 3. Database Verification

```sql
-- Connect to the database
psql -U mediconnect_user -d mediconnect_db

-- Check if tables are created
\dt

-- Exit
\q
```

## 🧪 Testing the Application

### Test User Registration

1. Open the mobile app
2. Navigate to "Create New Account"
3. Fill in the registration form
4. Choose role (Patient or Doctor)
5. Submit the form

### Test User Login

1. Use the credentials you just created
2. Login to the application
3. Verify you reach the appropriate dashboard

## 🚨 Troubleshooting

### Common Backend Issues

**Issue:** Database connection error
```
Solution: 
- Verify PostgreSQL is running
- Check database credentials in application.properties
- Ensure database and user exist
```

**Issue:** Port 8080 already in use
```
Solution:
- Change port in application.properties: server.port=8081
- Or stop the process using port 8080
```

**Issue:** Maven build fails
```
Solution:
- Verify Java 17+ is installed
- Check JAVA_HOME environment variable
- Run: mvn clean install -U
```

### Common Frontend Issues

**Issue:** Metro bundler not starting
```
Solution:
- Clear cache: expo start -c
- Delete node_modules and reinstall: rm -rf node_modules && npm install
```

**Issue:** Cannot connect to backend
```
Solution:
- Verify backend is running on http://localhost:8080
- Check API_BASE_URL in src/services/api.ts
- Ensure CORS is properly configured
```

**Issue:** Expo CLI not found
```
Solution:
- Install globally: npm install -g @expo/cli
- Or use npx: npx expo start
```

## 📱 Mobile Development Tips

### For Android Development
1. Install Android Studio
2. Set up Android SDK
3. Create an Android Virtual Device (AVD)
4. Run: `npm run android`

### For iOS Development (macOS only)
1. Install Xcode from App Store
2. Install iOS Simulator
3. Run: `npm run ios`

### For Physical Device Testing
1. Install Expo Go app
2. Ensure device and computer are on same network
3. Scan QR code from Expo development server

## 🔐 Security Notes

### Development Environment
- Default JWT secret is for development only
- Database credentials are default values
- CORS is configured for local development

### Production Deployment
- Change JWT secret to a strong, random value
- Use environment variables for sensitive data
- Configure CORS for your production domains
- Use HTTPS for all communications
- Set up proper database security

## 📚 Next Steps

After successful setup:

1. **Explore the API** - Visit Swagger UI at `http://localhost:8080/api/swagger-ui.html`
2. **Test Authentication** - Register and login with different user roles
3. **Customize the App** - Modify screens and add new features
4. **Add Real Features** - Implement video calling, messaging, etc.
5. **Deploy** - Follow deployment guides for production

## 🆘 Getting Help

If you encounter issues:

1. Check this troubleshooting section
2. Review the logs in terminal/console
3. Check the individual README files in backend and frontend directories
4. Create an issue in the repository
5. Contact the development team

## 📝 Development Workflow

### Making Changes

1. **Backend Changes:**
   ```bash
   cd mediconnect-backend
   # Make your changes
   mvn spring-boot:run  # Restart if needed
   ```

2. **Frontend Changes:**
   ```bash
   cd mediconnect-frontend
   # Make your changes
   # Expo will auto-reload
   ```

### Testing Changes

1. Test backend endpoints with Swagger UI
2. Test frontend changes on device/emulator
3. Verify integration between frontend and backend

---

**Congratulations! 🎉** 

You now have a fully functional MediConnect telemedicine platform running locally. Start exploring and building amazing healthcare solutions!

For more detailed information, check the README files in each module:
- `mediconnect-backend/README.md` - Backend documentation
- `mediconnect-frontend/README.md` - Frontend documentation 