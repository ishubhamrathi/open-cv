package com.portfolio.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a chat message in the conversation history.
 */
@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val sender: MessageSender,
    
    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @Column(length = 100)
    val intent: String? = null,
    
    @Column(nullable = false)
    val confidence: Double = 0.0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_item_id")
    val matchedKnowledgeItem: KnowledgeItem? = null,
    
    @Column(nullable = false)
    val sessionId: String = "default"
)

enum class MessageSender {
    USER,
    BOT
}
