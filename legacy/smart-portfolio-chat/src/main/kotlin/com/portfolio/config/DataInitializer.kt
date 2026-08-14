package com.portfolio.config

import com.portfolio.model.KnowledgeItem
import com.portfolio.model.MessageSender
import com.portfolio.repository.ChatMessageRepository
import com.portfolio.repository.KnowledgeItemRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class DataInitializer {

    @Bean
    fun initializeData(
        knowledgeItemRepository: KnowledgeItemRepository,
        chatMessageRepository: ChatMessageRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            // Only initialize if database is empty
            if (knowledgeItemRepository.count() == 0L) {
                println("Initializing portfolio knowledge base...")
                
                val initialKnowledge = listOf(
                    // Skills Category
                    KnowledgeItem(
                        category = "skills",
                        question = "What are your main programming skills?",
                        answer = """I'm proficient in several programming languages and technologies:
                            |
                            |**Backend:** Kotlin, Java, Spring Boot, Node.js
                            |**Frontend:** JavaScript, TypeScript, React, Vue.js
                            |**Databases:** PostgreSQL, MySQL, MongoDB, H2
                            |**Tools:** Git, Docker, Kubernetes, CI/CD
                            |**Cloud:** AWS, Azure, Google Cloud
                            |
                            |I always keep learning new technologies to stay current with industry trends.""".trimMargin(),
                        patterns = mutableListOf(
                            "what are your skills",
                            "programming languages",
                            "technologies you know",
                            "what can you code",
                            "tech stack"
                        ),
                        tags = "skills,programming,technology,languages"
                    ),
                    
                    KnowledgeItem(
                        category = "skills",
                        question = "What frameworks do you specialize in?",
                        answer = """I specialize in several modern frameworks:
                            |
                            |**Backend Frameworks:**
                            |- Spring Boot (Kotlin/Java)
                            |- Ktor
                            |- Express.js
                            |
                            |**Frontend Frameworks:**
                            |- React with TypeScript
                            |- Vue.js
                            |- Next.js
                            |
                            |I choose the right framework based on project requirements.""".trimMargin(),
                        patterns = mutableListOf(
                            "what frameworks",
                            "framework specialization",
                            "which frameworks do you use"
                        ),
                        tags = "frameworks,spring,react,vue"
                    ),
                    
                    // Experience Category
                    KnowledgeItem(
                        category = "experience",
                        question = "Tell me about your work experience",
                        answer = """I have extensive experience in software development:
                            |
                            |**Senior Software Engineer** | Tech Company | 2021-Present
                            |- Led development of microservices architecture
                            |- Mentored junior developers
                            |- Implemented CI/CD pipelines
                            |
                            |**Software Developer** | Startup Inc | 2018-2021
                            |- Built scalable web applications
                            |- Worked with cross-functional teams
                            |- Delivered projects on tight deadlines
                            |
                            |**Junior Developer** | Dev Corp | 2016-2018
                            |- Developed REST APIs
                            |- Maintained legacy systems
                            |- Learned best practices""".trimMargin(),
                        patterns = mutableListOf(
                            "work experience",
                            "your job history",
                            "where have you worked",
                            "tell me about your career"
                        ),
                        tags = "experience,work,career,jobs"
                    ),
                    
                    KnowledgeItem(
                        category = "experience",
                        question = "What industries have you worked in?",
                        answer = """I've had the opportunity to work across various industries:
                            |
                            |1. **FinTech** - Payment processing, banking systems
                            |2. **E-commerce** - Online retail platforms, inventory management
                            |3. **Healthcare** - Patient management systems, telemedicine
                            |4. **Education** - E-learning platforms, student portals
                            |5. **SaaS** - B2B software solutions, enterprise tools
                            |
                            |This diverse experience helps me understand different business domains.""".trimMargin(),
                        patterns = mutableListOf(
                            "industries",
                            "domains",
                            "sectors",
                            "types of companies"
                        ),
                        tags = "industries,domains,business"
                    ),
                    
                    // Projects Category
                    KnowledgeItem(
                        category = "projects",
                        question = "What projects have you worked on?",
                        answer = """Here are some notable projects I've worked on:
                            |
                            |**1. E-Commerce Platform**
                            |- Full-stack application with React and Spring Boot
                            |- Handles 10k+ daily transactions
                            |- [GitHub Link]
                            |
                            |**2. Real-time Analytics Dashboard**
                            |- WebSocket-based real-time data visualization
                            |- Built with Vue.js and Kotlin
                            |- Processes millions of events daily
                            |
                            |**3. Microservices Architecture**
                            |- Designed and implemented cloud-native microservices
                            |- Used Docker, Kubernetes, and service mesh
                            |- Improved system scalability by 300%
                            |
                            |**4. Mobile App Backend**
                            |- RESTful API for iOS and Android apps
                            |- Integrated payment gateways and push notifications
                            |- Serves 50k+ active users""".trimMargin(),
                        patterns = mutableListOf(
                            "your projects",
                            "what have you built",
                            "portfolio projects",
                            "show me your work"
                        ),
                        tags = "projects,portfolio,github,applications"
                    ),
                    
                    KnowledgeItem(
                        category = "projects",
                        question = "Do you have a GitHub profile?",
                        answer = """Yes! You can find my code and projects on GitHub:
                            |
                            |**GitHub:** github.com/yourusername
                            |
                            |My repositories include:
                            |- Open source contributions
                            |- Personal projects
                            |- Code samples and demos
                            |- Technical documentation
                            |
                            |Feel free to explore my code and reach out if you have questions!""".trimMargin(),
                        patterns = mutableListOf(
                            "github",
                            "code repository",
                            "open source",
                            "your code"
                        ),
                        tags = "github,opensource,code,repositories"
                    ),
                    
                    // Education Category
                    KnowledgeItem(
                        category = "education",
                        question = "What is your educational background?",
                        answer = """**Bachelor of Science in Computer Science**
                            |University Name | 2012-2016
                            |
                            |**Relevant Coursework:**
                            |- Data Structures & Algorithms
                            |- Software Engineering
                            |- Database Systems
                            |- Computer Networks
                            |- Operating Systems
                            |
                            |**Certifications:**
                            |- AWS Certified Solutions Architect
                            |- Oracle Certified Professional, Java SE
                            |- Spring Professional Certification
                            |- Kubernetes Administrator (CKA)""".trimMargin(),
                        patterns = mutableListOf(
                            "education",
                            "degree",
                            "university",
                            "certifications",
                            "qualifications"
                        ),
                        tags = "education,degree,university,certifications"
                    ),
                    
                    // Contact Category
                    KnowledgeItem(
                        category = "contact",
                        question = "How can I contact you?",
                        answer = """You can reach me through various channels:
                            |
                            |📧 **Email:** your.email@example.com
                            |💼 **LinkedIn:** linkedin.com/in/yourprofile
                            |🐦 **Twitter:** @yourhandle
                            |📱 **Phone:** +1-234-567-8900
                            |
                            |I'm open to discussing:
                            |- Job opportunities
                            |- Freelance projects
                            |- Speaking engagements
                            |- Technical collaborations
                            |
                            |Response time: Usually within 24 hours""".trimMargin(),
                        patterns = mutableListOf(
                            "contact",
                            "reach you",
                            "get in touch",
                            "email",
                            "phone number"
                        ),
                        tags = "contact,email,linkedin,social"
                    ),
                    
                    KnowledgeItem(
                        category = "contact",
                        question = "Are you available for freelance work?",
                        answer = """Yes, I'm available for select freelance projects!
                            |
                            |**What I offer:**
                            |- Full-stack web development
                            |- API design and implementation
                            |- System architecture consulting
                            |- Code review and mentoring
                            |
                            |**Preferred project types:**
                            |- Short-term contracts (1-3 months)
                            |- Technical consulting
                            |- MVP development
                            |- Performance optimization
                            |
                            |Please reach out via email to discuss your project requirements.""".trimMargin(),
                        patterns = mutableListOf(
                            "freelance",
                            "available for hire",
                            "contract work",
                            "consulting"
                        ),
                        tags = "freelance,consulting,available,hire"
                    ),
                    
                    // General/Greeting
                    KnowledgeItem(
                        category = "general",
                        question = "Hello / Hi",
                        answer = """👋 Hello! Welcome to my interactive portfolio!
                            |
                            |I'm an AI assistant that can tell you about:
                            |- 💻 My technical skills
                            |- 🏢 Work experience
                            |- 🚀 Projects I've built
                            |- 🎓 Education and certifications
                            |- 📬 How to contact me
                            |
                            |Feel free to ask me anything! Try questions like:
                            |- "What are your main skills?"
                            |- "Tell me about your experience"
                            |- "What projects have you worked on?"""".trimMargin(),
                        patterns = mutableListOf(
                            "hello",
                            "hi",
                            "hey",
                            "greetings",
                            "welcome"
                        ),
                        tags = "greeting,hello,hi,welcome"
                    )
                )
                
                knowledgeItemRepository.saveAll(initialKnowledge)
                println("✅ Initialized ${initialKnowledge.size} knowledge items")
                
                // Add a welcome message to chat history
                val welcomeMessage = com.portfolio.model.ChatMessage(
                    content = "Welcome to my portfolio! Ask me anything about my skills, experience, or projects.",
                    sender = MessageSender.BOT,
                    sessionId = "system",
                    intent = "greeting",
                    confidence = 1.0,
                    timestamp = LocalDateTime.now()
                )
                chatMessageRepository.save(welcomeMessage)
            } else {
                println("Database already initialized. Skipping data initialization.")
            }
        }
    }
}
