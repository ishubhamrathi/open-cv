# Smart Portfolio Chat

An AI-driven chat-based portfolio application with a limited knowledge base, built with Spring Boot and Kotlin.

## Description

This project is a Spring Boot application that provides an intelligent chatbot interface for portfolio interactions. It features a web UI with real-time chat capabilities, security authentication, and persistent data storage.

## Features

- AI-driven chatbot engine
- Real-time chat interface
- Knowledge base management
- Web-based UI with Thymeleaf
- Spring Security integration
- H2 database for data persistence
- REST API controllers
- Spring Boot 3.2.4 with Kotlin

## Prerequisites

- **Java 21 JDK** - [Download](https://adoptium.net/temurin/releases/?version=21)
- **Maven 3.9.x** - [Download](https://maven.apache.org/download.cgi)
- Git

## Installation

```bash
# Clone the repository
git clone <repository-url>
cd open-cv
```

## Building the Project

From the root directory (`open-cv/`):

```bash
# Using Maven wrapper (if available)
./mvn clean install -DskipTests

# Or using Maven directly
mvn clean install -DskipTests
```

## Running the Application

### From root directory:

**PowerShell:**
```powershell
$env:JAVA_HOME = 'C:\Java\jdk-21.0.2+13'
$env:M2_HOME = 'C:\Maven\apache-maven-3.9.6'
$env:Path = $env:JAVA_HOME + '\bin;' + $env:M2_HOME + '\bin;' + $env:Path
cd open-cv
mvn spring-boot:run
```

**Command Prompt:**
```cmd
set JAVA_HOME=C:\Java\jdk-21.0.2+13
set M2_HOME=C:\Maven\apache-maven-3.9.6
set Path=%JAVA_HOME%\bin;%M2_HOME%\bin;%Path%
cd open-cv
mvn spring-boot:run
```

### Or run the packaged JAR:

```bash
java -jar open-cv/target/smart-portfolio-chat-1.0.0.jar
```

Once running, access the application at: **http://localhost:8080**

## API Endpoints

- `GET /` - Home page / UI
- `GET /chat` - Chat messages
- `POST /chat` - Send chat message
- `GET /knowledge` - Knowledge items
- `POST /knowledge` - Add knowledge item
- `GET /h2-console` - H2 database console

## Project Structure

```
open-cv/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
├── src/
│   └── main/
│       ├── kotlin/com/portfolio/
│       │   ├── PortfolioApplication.kt        # Spring Boot entry point
│       │   ├── chatbot/
│       │   │   └── ChatbotEngine.kt           # AI chat engine
│       │   ├── config/
│       │   │   ├── SecurityConfig.kt          # Security configuration
│       │   │   └── DataInitializer.kt         # Initial data setup
│       │   ├── controller/                    # REST controllers
│       │   ├── exception/                     # Exception handling
│       │   ├── model/                         # Data models & DTOs
│       │   ├── repository/                    # Database repositories
│       │   ├── service/                       # Business logic
│       │   └── util/                          # Utilities
│       └── resources/
│           ├── application.properties         # App configuration
│           ├── static/                        # CSS, JS, images
│           └── templates/                     # Thymeleaf templates
└── target/                                    # Build output
```

## Configuration

Edit `open-cv/src/main/resources/application.properties`:

```properties
server.port=8080                              # Application port
spring.application.name=smart-portfolio-chat
spring.datasource.url=jdbc:h2:file:./data/portfolio_db  # Database path
spring.h2.console.enabled=true                # Enable H2 console
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
