package com.portfolio.repository

import com.portfolio.model.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    
    fun findBySessionIdOrderByTimestampDesc(sessionId: String): List<ChatMessage>
    
    fun findTop10BySessionIdOrderByTimestampDesc(sessionId: String): List<ChatMessage>
    
    @Query("SELECT COUNT(DISTINCT c.sessionId) FROM ChatMessage c")
    fun countDistinctSessions(): Long
    
    @Query("SELECT c FROM ChatMessage c WHERE c.timestamp > :since ORDER BY c.timestamp DESC")
    fun findRecentMessages(since: LocalDateTime): List<ChatMessage>
    
    fun deleteBySessionId(sessionId: String)
}
