package com.portfolio.service

import com.portfolio.chatbot.ChatbotEngine
import com.portfolio.model.*
import com.portfolio.repository.ChatMessageRepository
import com.portfolio.repository.KnowledgeItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val chatbotEngine: ChatbotEngine,
    private val knowledgeItemRepository: KnowledgeItemRepository,
    private val chatMessageRepository: ChatMessageRepository
) {

    /**
     * Process a chat message and return a response
     */
    @Transactional
    fun processMessage(request: ChatRequest): ChatResponse {
        val userMessage = ChatMessage(
            content = request.message,
            sender = MessageSender.USER,
            sessionId = request.sessionId
        )
        chatMessageRepository.save(userMessage)
        
        // Get all active knowledge items
        val knowledgeItems = knowledgeItemRepository.findByIsActiveTrue()
        
        // Process query through chatbot engine
        val botResponse = chatbotEngine.processQuery(request.message, knowledgeItems)
        
        // Generate suggestions
        val suggestions = chatbotEngine.generateSuggestions(botResponse.category, knowledgeItems)
        
        // Save bot response
        val botMessage = ChatMessage(
            content = botResponse.answer,
            sender = MessageSender.BOT,
            sessionId = request.sessionId,
            intent = botResponse.category,
            confidence = botResponse.confidence,
            matchedKnowledgeItem = botResponse.matchedItem
        )
        chatMessageRepository.save(botMessage)
        
        return ChatResponse(
            message = botResponse.answer,
            confidence = botResponse.confidence,
            matchedCategory = botResponse.category,
            suggestions = suggestions,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
    
    /**
     * Get chat history for a session
     */
    fun getChatHistory(sessionId: String, limit: Int = 20): List<ChatMessage> {
        return chatMessageRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId)
            .reversed()
            .take(limit)
    }
    
    /**
     * Clear chat history for a session
     */
    @Transactional
    fun clearChatHistory(sessionId: String) {
        chatMessageRepository.deleteBySessionId(sessionId)
    }
    
    /**
     * Get conversation statistics
     */
    fun getConversationStats(): Map<String, Any> {
        val totalSessions = chatMessageRepository.countDistinctSessions()
        val recentMessages = chatMessageRepository.findRecentMessages(
            LocalDateTime.now().minusDays(7)
        )
        
        return mapOf(
            "totalSessions" to totalSessions,
            "recentMessagesCount" to recentMessages.size,
            "lastActivity" to recentMessages.firstOrNull()?.timestamp
        )
    }
}
