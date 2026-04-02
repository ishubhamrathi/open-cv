# Smart Portfolio Chat - AI-Driven Interactive CV

A modern, monolithic portfolio website with an AI-driven chatbot that serves as your interactive CV. Built with Kotlin and Spring Boot, this application uses a limited knowledge base approach for easy deployment and maintenance.

## 🚀 Features

### For Visitors
- **Interactive Chat Interface**: Ask questions about skills, experience, projects, education, and more
- **Smart Suggestions**: Get relevant follow-up question suggestions
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Real-time Responses**: Instant answers powered by pattern matching and keyword recognition

### For You (Portfolio Owner)
- **Admin Dashboard**: Easy-to-use interface to manage your knowledge base
- **Add/Edit Knowledge**: Update your information without touching code
- **Search Functionality**: Quickly find and edit existing knowledge items
- **Statistics**: Track usage and popular topics
- **No External AI Dependencies**: Runs entirely on your server with no API costs

## 🛠️ Technology Stack

- **Backend**: Kotlin, Spring Boot 3.2
- **Database**: H2 (file-based, easily switchable to PostgreSQL/MySQL)
- **Frontend**: Vanilla JavaScript, CSS3
- **Security**: Spring Security with CORS support
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🚀 Quick Start

### 1. Clone and Build

```bash
cd smart-portfolio-chat
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### 3. Access the Application

- **Main Chat Interface**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/portfolio_db`
  - Username: `sa`
  - Password: (empty)

## 📁 Project Structure

```
smart-portfolio-chat/
├── src/main/kotlin/com/portfolio/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── model/            # Entity and DTO classes
│   ├── repository/       # Data access layer
│   ├── service/          # Business logic
│   └── chatbot/          # Chatbot engine
├── src/main/resources/
│   ├── static/
│   │   ├── css/          # Stylesheets
│   │   └── js/           # JavaScript files
│   ├── templates/        # HTML templates
│   └── application.properties
└── pom.xml
```

## 💡 How It Works

### Chatbot Engine

The chatbot uses a **pattern-based matching system** with three levels of intelligence:

1. **Exact Pattern Match**: Matches user input against predefined question patterns
2. **Keyword Matching**: Scores responses based on keyword overlap
3. **Category Detection**: Identifies topic categories (skills, experience, etc.)

### Knowledge Base

Information is stored as `KnowledgeItem` entities with:
- **Category**: Groups related information (skills, experience, projects, etc.)
- **Question**: The canonical form of the question
- **Answer**: Detailed response (supports markdown-like formatting)
- **Patterns**: Alternative ways users might ask the question
- **Tags**: Keywords for better searchability

## 🔧 Customization

### Update Your Information

#### Option 1: Using the Admin Dashboard

1. Navigate to the "Admin" tab
2. Click "Add New" to create new knowledge items
3. Or click "Edit" on existing items to update them

#### Option 2: Edit the DataInitializer

Modify `src/main/kotlin/com/portfolio/config/DataInitializer.kt` to change the default data:

```kotlin
KnowledgeItem(
    category = "skills",
    question = "What are your main programming skills?",
    answer = """Your detailed answer here...""",
    patterns = mutableListOf("what are your skills", "programming languages"),
    tags = "skills,programming,technology"
)
```

### Change Categories

Default categories:
- `skills` - Technical abilities and technologies
- `experience` - Work history and roles
- `projects` - Portfolio projects and GitHub
- `education` - Degrees and certifications
- `contact` - Contact information and availability
- `general` - Greetings and general info

### Styling

Edit `src/main/resources/static/css/styles.css` to customize:
- Color scheme (CSS variables in `:root`)
- Layout and spacing
- Typography
- Responsive breakpoints

## 🌐 Deployment

### Production Configuration

Before deploying to production, update `application.properties`:

```properties
# Use a production database
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Disable H2 console
spring.h2.console.enabled=false

# Set production security
security.jwt.secret=your-production-secret-key-min-32-chars!!
```

### Deploy to Server

1. Build the JAR:
   ```bash
   mvn clean package -DskipTests
   ```

2. Run the JAR:
   ```bash
   java -jar target/smart-portfolio-chat-1.0.0.jar
   ```

3. Or deploy as a service using systemd, Docker, or your preferred method

### Docker Deployment (Optional)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/smart-portfolio-chat-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 📊 API Endpoints

### Chat API
- `POST /api/chat` - Send a message and get a response
- `GET /api/chat/history/{sessionId}` - Get chat history
- `DELETE /api/chat/history/{sessionId}` - Clear chat history
- `GET /api/chat/stats` - Get conversation statistics

### Knowledge API
- `GET /api/knowledge` - Get all knowledge items
- `GET /api/knowledge/{id}` - Get a specific item
- `POST /api/knowledge` - Create a new item
- `PUT /api/knowledge/{id}` - Update an item
- `DELETE /api/knowledge/{id}` - Delete an item
- `GET /api/knowledge/search?query=...` - Search items
- `GET /api/knowledge/categories` - Get all categories
- `GET /api/knowledge/stats` - Get knowledge base statistics

## 🎯 Best Practices

### Adding Effective Knowledge Items

1. **Multiple Patterns**: Add various ways users might ask the same question
   ```
   Question: "What are your main skills?"
   Patterns: 
   - "what are your skills"
   - "programming languages you know"
   - "technologies you use"
   - "tech stack"
   ```

2. **Detailed Answers**: Provide comprehensive, well-formatted answers
   - Use bold text for emphasis: `**important**`
   - Use line breaks for readability
   - Include relevant links (GitHub, LinkedIn, etc.)

3. **Categorize Properly**: Group related information for better suggestions

4. **Use Tags**: Add searchable keywords for better discovery

### Regular Updates

- Review chat logs to identify unanswered questions
- Add new knowledge items based on common queries
- Keep contact information current
- Update project descriptions as you complete new work

## 🔒 Security Notes

- The admin panel is currently open for ease of use
- For production, consider adding authentication
- Change the JWT secret in production
- Enable HTTPS for secure communication
- Consider rate limiting for the chat endpoint

## 🤝 Contributing

This is your personal portfolio, but feel free to extend it with:
- Additional categories
- Enhanced chatbot algorithms
- Analytics and tracking
- Multi-language support
- Resume PDF generation
- Email notifications for new inquiries

## 📝 License

This project is open source and available for personal use. Feel free to modify and customize it for your own portfolio.

## 💬 Support

For issues or questions:
1. Check the application logs
2. Review the H2 database via the console
3. Test API endpoints directly using tools like Postman

---

**Built with ❤️ using Kotlin & Spring Boot**

Happy coding! 🚀
