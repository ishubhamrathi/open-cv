package com.portfolio.controller

import com.portfolio.model.ChatRequest
import com.portfolio.model.ChatResponse
import com.portfolio.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"])
class ChatController(
    private val chatService: ChatService
) {

    /**
     * Send a message and get a response
     */
    @PostMapping
    fun sendMessage(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        if (request.message.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val response = chatService.processMessage(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Get chat history for a session
     */
    @GetMapping("/history/{sessionId}")
    fun getHistory(@PathVariable sessionId: String): ResponseEntity<List<Map<String, Any>>> {
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
        chatService.clearChatHistory(sessionId)
        return ResponseEntity.ok(mapOf("message" to "Chat history cleared"))
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Any>> {
        val stats = chatService.getConversationStats()
        return ResponseEntity.ok(stats)
    }
}
