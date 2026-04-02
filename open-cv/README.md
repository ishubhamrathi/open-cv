# Smart Portfolio Chat - AI-Driven Interactive CV

A modern, monolithic portfolio website with an AI-driven chatbot that serves as your interactive CV. Built with Kotlin and Spring Boot, this application uses a pattern-based knowledge base approach for easy deployment and maintenance without external AI dependencies.

## 🚀 Features

### For Visitors
- **Interactive Chat Interface**: Ask questions about skills, experience, projects, education, and more
- **Smart Suggestions**: Get relevant follow-up question suggestions based on context
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Real-time Responses**: Instant answers powered by pattern matching and keyword recognition
- **Chat History**: Conversations are persisted and can be retrieved by session

### For You (Portfolio Owner)
- **Admin Dashboard**: Easy-to-use interface to manage your knowledge base
- **Add/Edit Knowledge**: Update your information through the UI or code
- **Search Functionality**: Quickly find and edit existing knowledge items
- **Statistics**: Track knowledge base metrics by category
- **No External AI Dependencies**: Runs entirely on your server with no API costs
- **Soft Delete**: Knowledge items can be deactivated without permanent deletion

## 🛠️ Technology Stack

- **Backend**: Kotlin, Spring Boot 3.2.4
- **Database**: H2 (file-based, easily switchable to PostgreSQL/MySQL)
- **ORM**: Spring Data JPA
- **Frontend**: Vanilla JavaScript, CSS3, HTML5
- **Security**: Spring Security with CORS support
- **Build Tool**: Maven
- **Template Engine**: Thymeleaf

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🚀 Quick Start

### 1. Clone and Build

```bash
cd open-cv
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
open-cv/
├── src/main/kotlin/com/portfolio/
│   ├── PortfolioApplication.kt       # Main application entry point
│   ├── chatbot/
│   │   └── ChatbotEngine.kt          # Pattern-based chatbot engine
│   ├── config/
│   │   ├── SecurityConfig.kt         # Security configuration
│   │   └── DataInitializer.kt        # Initial data seeding
│   ├── controller/
│   │   ├── WebController.kt          # Web page routing
│   │   ├── ChatController.kt         # Chat REST API endpoints
│   │   └── KnowledgeController.kt    # Knowledge management REST API
│   ├── model/
│   │   ├── ChatMessage.kt            # Chat message entity
│   │   ├── KnowledgeItem.kt          # Knowledge item entity
│   │   └── DTOs.kt                   # Data transfer objects
│   ├── repository/
│   │   ├── ChatMessageRepository.kt  # Chat message data access
│   │   └── KnowledgeItemRepository.kt # Knowledge item data access
│   └── service/
│       ├── ChatService.kt            # Chat business logic
│       └── KnowledgeService.kt       # Knowledge management logic
├── src/main/resources/
│   ├── static/
│   │   ├── css/
│   │   │   └── styles.css            # Application styles
│   │   └── js/
│   │       └── app.js                # Frontend JavaScript
│   ├── templates/
│   │   └── index.html                # Main HTML template
│   └── application.properties        # Application configuration
└── pom.xml                           # Maven build configuration
```

## 💡 How It Works

### Chatbot Engine

The chatbot uses a **pattern-based matching system** with three levels of intelligence:

1. **Exact Pattern Match**: Matches user input against predefined question patterns (100% confidence)
2. **Keyword Matching**: Scores responses based on keyword overlap with questions, answers, and tags (30%-100% confidence)
3. **Category Detection**: Identifies topic categories (skills, experience, projects, education, contact) when no direct match is found (60% confidence)

The engine processes queries in order of specificity, returning the highest-confidence match found.

### Knowledge Base

Information is stored as `KnowledgeItem` entities with:
- **Category**: Groups related information (skills, experience, projects, education, contact, general)
- **Question**: The canonical form of the question
- **Answer**: Detailed response (supports markdown-like formatting with `**bold**` and line breaks)
- **Patterns**: Alternative ways users might ask the question (stored as a collection)
- **Tags**: Comma-separated keywords for better searchability
- **isActive**: Soft-delete flag to deactivate items without permanent deletion
- **metadata**: Optional JSON field for additional data
- **createdAt/updatedAt**: Automatic timestamp tracking

### Data Flow

1. User sends a message via the chat interface
2. Message is saved to `ChatMessageRepository`
3. `ChatService` processes the message through `ChatbotEngine`
4. Engine matches query against active knowledge items
5. Response with answer, confidence score, and suggestions is returned
6. Bot response is saved and displayed to the user

## 🔧 Customization

### Update Your Information

#### Option 1: Using the Admin Dashboard (Recommended)

1. Click the "Admin" tab in the navigation
2. **View All**: Browse existing knowledge items with search functionality
3. **Add New**: Create new knowledge items with category, question, answer, patterns, and tags
4. **Edit**: Click "Edit" on any item to modify its content
5. **Delete**: Soft-delete items to hide them from chatbot responses
6. **Statistics**: View knowledge base metrics by category

#### Option 2: Edit the DataInitializer

Modify `src/main/kotlin/com/portfolio/config/DataInitializer.kt` to change the default seed data:

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

Default categories available:
- `skills` - Technical abilities and technologies
- `experience` - Work history and roles
- `projects` - Portfolio projects and GitHub
- `education` - Degrees and certifications
- `contact` - Contact information and availability
- `general` - Greetings and general info

You can add custom categories by simply using a new category name when creating knowledge items.

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

Build and run:
```bash
docker build -t smart-portfolio-chat .
docker run -p 8080:8080 smart-portfolio-chat
```

## 📊 API Endpoints

### Chat API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | Send a message and get a response |
| `GET` | `/api/chat/history/{sessionId}` | Get chat history for a session |
| `DELETE` | `/api/chat/history/{sessionId}` | Clear chat history for a session |
| `GET` | `/api/chat/stats` | Get conversation statistics |

**Request Body for POST /api/chat:**
```json
{
  "message": "What are your skills?",
  "sessionId": "session_abc123"
}
```

**Response:**
```json
{
  "message": "I'm proficient in several programming languages...",
  "confidence": 0.95,
  "matchedCategory": "skills",
  "suggestions": ["What frameworks do you use?", "Tell me about your experience"],
  "timestamp": "2024-01-15T10:30:00"
}
```

### Knowledge API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/knowledge` | Get all active knowledge items |
| `GET` | `/api/knowledge/category/{category}` | Get items by category |
| `GET` | `/api/knowledge/{id}` | Get a specific item |
| `POST` | `/api/knowledge` | Create a new item |
| `PUT` | `/api/knowledge/{id}` | Update an item |
| `DELETE` | `/api/knowledge/{id}` | Soft-delete an item |
| `GET` | `/api/knowledge/search?query=...` | Search items by keyword |
| `GET` | `/api/knowledge/categories` | Get all categories |
| `GET` | `/api/knowledge/stats` | Get knowledge base statistics |
| `POST` | `/api/knowledge/import` | Import multiple items from JSON |

**Knowledge Item DTO:**
```json
{
  "id": 1,
  "category": "skills",
  "question": "What are your main skills?",
  "answer": "Detailed answer here...",
  "patterns": ["what are your skills", "tech stack"],
  "confidence": 1.0,
  "isActive": true,
  "tags": "skills,programming",
  "metadata": null
}
```

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
   - "what can you code"
   ```

2. **Detailed Answers**: Provide comprehensive, well-formatted answers
   - Use bold text for emphasis: `**important**`
   - Use line breaks for readability (each new line starts with `|`)
   - Include relevant links (GitHub, LinkedIn, etc.)
   - Use emoji icons for visual appeal

3. **Categorize Properly**: Group related information for better suggestions and organization

4. **Use Tags**: Add searchable keywords as comma-separated values for better discovery

5. **Test Your Patterns**: Try different phrasings in the chat to ensure patterns match correctly

### Regular Updates

- Review chat logs to identify unanswered questions (check H2 console or database)
- Add new knowledge items based on common queries
- Keep contact information current
- Update project descriptions as you complete new work
- Monitor statistics to see which categories are most popular

## 🔒 Security Notes

- The admin panel is currently open for ease of use (no authentication required)
- For production, consider adding authentication to the `/admin/**` endpoints
- Change the default security configuration in `SecurityConfig.kt`
- Enable HTTPS for secure communication
- Consider rate limiting for the chat endpoint to prevent abuse
- Update CORS settings if deploying to a specific domain
- Use environment variables for sensitive configuration (database credentials, etc.)

## 🤝 Contributing

This is your personal portfolio, but feel free to extend it with:
- Additional categories and knowledge items
- Enhanced chatbot algorithms (fuzzy matching, ML-based approaches)
- Analytics and usage tracking
- Multi-language support (i18n)
- Resume PDF generation
- Email notifications for new inquiries
- User authentication for the admin panel
- Integration with external AI services (optional)
- Dark mode toggle
- Export/import functionality for knowledge base backup

## 📝 License

This project is open source and available for personal use. Feel free to modify and customize it for your own portfolio.

## 💬 Support

For issues or questions:
1. Check the application logs in the console
2. Review the H2 database via the console at `/h2-console`
3. Test API endpoints directly using tools like Postman or curl
4. Verify that all dependencies are correctly configured in `pom.xml`

### Common Issues

**Issue**: Chatbot returns low confidence responses  
**Solution**: Add more patterns to your knowledge items or improve keyword tags

**Issue**: Database not persisting data  
**Solution**: Check that the `data/` directory has write permissions

**Issue**: CORS errors when calling API  
**Solution**: Update CORS configuration in `SecurityConfig.kt`

---

**Built with ❤️ using Kotlin & Spring Boot**

Happy coding! 🚀
