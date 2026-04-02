package com.portfolio.service

import com.portfolio.exception.InvalidChatInputException
import com.portfolio.exception.ResourceNotFoundException
import com.portfolio.mapper.KnowledgeMapper
import com.portfolio.model.*
import com.portfolio.repository.ChatMessageRepository
import com.portfolio.repository.KnowledgeItemRepository
import com.portfolio.util.AppConstants
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val chatbotEngine: ChatbotEngine,
    private val knowledgeItemRepository: KnowledgeItemRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val knowledgeMapper: KnowledgeMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(ChatService::class.java)
    }

    /**
     * Process a chat message and return a response
     */
    @Transactional
    fun processMessage(request: ChatRequest): ChatResponse {
        // Validate input
        if (request.message.isBlank()) {
            throw InvalidChatInputException("Message cannot be empty")
        }

        val sanitizedMessage = request.message.trim().take(1000)
        
        log.info("Processing chat message for session: {}", request.sessionId)

        val userMessage = ChatMessage(
            content = sanitizedMessage,
            sender = MessageSender.USER,
            sessionId = request.sessionId
        )
        chatMessageRepository.save(userMessage)
        
        // Get all active knowledge items
        val knowledgeItems = knowledgeItemRepository.findByIsActiveTrue()
        
        // Process query through chatbot engine
        val botResponse = chatbotEngine.processQuery(sanitizedMessage, knowledgeItems)
        
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
        
        log.info("Chat response generated with confidence: {}", botResponse.confidence)
        
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
    fun getChatHistory(sessionId: String, limit: Int = AppConstants.CHAT_HISTORY_LIMIT): List<ChatMessage> {
        log.debug("Fetching chat history for session: {} with limit: {}", sessionId, limit)
        return chatMessageRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId)
            .reversed()
            .take(limit)
    }
    
    /**
     * Clear chat history for a session
     */
    @Transactional
    fun clearChatHistory(sessionId: String) {
        log.info("Clearing chat history for session: {}", sessionId)
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
