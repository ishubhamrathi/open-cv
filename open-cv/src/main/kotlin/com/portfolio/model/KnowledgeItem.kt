package com.portfolio.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a knowledge item in the portfolio chatbot's knowledge base.
 * Each item has a category, question patterns, and answer content.
 */
@Entity
@Table(name = "knowledge_items")
data class KnowledgeItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    val category: String,
    
    @Column(nullable = false, length = 500)
    val question: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val answer: String,
    
    @ElementCollection
    @CollectionTable(name = "question_patterns", joinColumns = [JoinColumn(name = "knowledge_item_id")])
    @Column(name = "pattern", length = 200)
    val patterns: MutableList<String> = mutableListOf(),
    
    @Column(nullable = false)
    val confidence: Double = 1.0,
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var isActive: Boolean = true,
    
    @Column(length = 500)
    val tags: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val metadata: String? = null
) {
    @PreUpdate
    fun onUpdate() {
        // Update timestamp before saving
    }
}
