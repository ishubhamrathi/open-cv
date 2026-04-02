package com.portfolio.controller

import com.portfolio.exception.InvalidChatInputException
import com.portfolio.mapper.KnowledgeMapper
import com.portfolio.model.ChatRequest
import com.portfolio.model.ChatResponse
import com.portfolio.service.ChatService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"])
class ChatController(
    private val chatService: ChatService,
    private val knowledgeMapper: KnowledgeMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(ChatController::class.java)
    }

    /**
     * Send a message and get a response
     */
    @PostMapping
    fun sendMessage(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        try {
            if (request.message.isBlank()) {
                throw InvalidChatInputException("Message cannot be blank")
            }
            
            log.info("Received chat message from session: {}", request.sessionId)
            val response = chatService.processMessage(request)
            return ResponseEntity.ok(response)
        } catch (e: InvalidChatInputException) {
            log.warn("Invalid chat input: {}", e.message)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            log.error("Error processing chat message", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get chat history for a session
     */
    @GetMapping("/history/{sessionId}")
    fun getHistory(@PathVariable sessionId: String): ResponseEntity<List<Map<String, Any>>> {
        log.debug("Fetching chat history for session: {}", sessionId)
        
        val messages = chatService.getChatHistory(sessionId)
        val response = messages.map { msg ->
            mapOf(
                "id" to msg.id,
                "content" to msg.content,
                "sender" to msg.sender.name,
                "timestamp" to msg.timestamp.toString(),
                "confidence" to msg.confidence
            )
        }
        return ResponseEntity.ok(response)
    }

    /**
     * Clear chat history
     */
    @DeleteMapping("/history/{sessionId}")
    fun clearHistory(@PathVariable sessionId: String): ResponseEntity<Map<String, String>> {
        log.info("Clearing chat history for session: {}", sessionId)
        chatService.clearChatHistory(sessionId)
        return ResponseEntity.ok(mapOf("message" to "Chat history cleared"))
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Any>> {
        log.debug("Fetching conversation statistics")
        val stats = chatService.getConversationStats()
        return ResponseEntity.ok(stats)
    }
}
