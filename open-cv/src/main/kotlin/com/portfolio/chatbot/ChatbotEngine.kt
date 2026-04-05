package com.portfolio.chatbot

import com.portfolio.model.KnowledgeItem
import org.springframework.stereotype.Component

/**
 * Simple pattern-based chatbot engine that matches user queries to knowledge items.
 * Uses keyword matching and pattern recognition without requiring external AI services.
 */
@Component
class ChatbotEngine {

    /**
     * Process a user query and find the best matching knowledge item
     */
    fun processQuery(query: String, knowledgeItems: List<KnowledgeItem>): ChatbotResponse {
        val normalizedQuery = normalizeText(query)
        
        // Try exact pattern match first
        val exactMatch = findExactPatternMatch(normalizedQuery, knowledgeItems)
        if (exactMatch != null) {
            return ChatbotResponse(
                answer = exactMatch.answer,
                confidence = 1.0,
                matchedItem = exactMatch,
                category = exactMatch.category
            )
        }
        
        // Try keyword matching
        val keywordMatches = findKeywordMatches(normalizedQuery, knowledgeItems)
        if (keywordMatches.isNotEmpty()) {
            val bestMatch = keywordMatches.maxByOrNull { it.score }!!
            if (bestMatch.score >= 0.3) { // Minimum confidence threshold
                return ChatbotResponse(
                    answer = bestMatch.item.answer,
                    confidence = bestMatch.score,
                    matchedItem = bestMatch.item,
                    category = bestMatch.item.category
                )
            }
        }
        
        // Try category-based matching
        val categoryMatch = findCategoryMatch(normalizedQuery, knowledgeItems)
        if (categoryMatch != null) {
            return ChatbotResponse(
                answer = categoryMatch.answer,
                confidence = 0.6,
                matchedItem = categoryMatch,
                category = categoryMatch.category
            )
        }
        
        // No match found
        return ChatbotResponse(
            answer = "I'm not sure about that. Could you ask something about my skills, experience, projects, or education?",
            confidence = 0.0,
            matchedItem = null,
            category = null
        )
    }
    
    /**
     * Generate follow-up suggestions based on the matched category
     * Excludes the current question and provides diverse suggestions from other categories
     */
    fun generateSuggestions(category: String?, knowledgeItems: List<KnowledgeItem>, currentQuestion: String? = null): List<String> {
        if (category == null) {
            return listOf("What are your main skills?", "Tell me about your experience", "What projects have you worked on?")
        }
        
        // Get items from the same category but exclude the current question
        val relatedItems = knowledgeItems.filter { 
            it.category == category && it.isActive && 
            !it.question.equals(currentQuestion, ignoreCase = true)
        }
        
        // Get items from other categories for diversity
        val otherCategoryItems = knowledgeItems.filter { 
            it.category != category && it.isActive 
        }
        
        // If we have related items (excluding current), suggest 1-2 from same category
        val suggestions = mutableListOf<String>()
        
        if (relatedItems.isNotEmpty()) {
            suggestions.addAll(relatedItems.take(2).map { it.question })
        }
        
        // Fill remaining slots with items from other categories
        val remainingSlots = 4 - suggestions.size
        if (otherCategoryItems.isNotEmpty() && remainingSlots > 0) {
            val shuffledOthers = otherCategoryItems.shuffled().take(remainingSlots)
            suggestions.addAll(shuffledOthers.map { it.question })
        }
        
        // If we still don't have enough suggestions, add defaults
        val defaultSuggestions = listOf(
            "What are your main skills?",
            "Tell me about your experience",
            "What projects have you worked on?",
            "How can I contact you?"
        )
        
        while (suggestions.size < 4) {
            val defaultToAdd = defaultSuggestions.firstOrNull { it !in suggestions }
            if (defaultToAdd != null) {
                suggestions.add(defaultToAdd)
            } else {
                break
            }
        }
        
        return suggestions.take(4)
    }
    
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()
    }
    
    private fun findExactPatternMatch(query: String, knowledgeItems: List<KnowledgeItem>): KnowledgeItem? {
        return knowledgeItems.firstOrNull { item ->
            item.isActive && item.patterns.any { pattern ->
                normalizeText(pattern) == query || query.contains(normalizeText(pattern))
            }
        }
    }
    
    private fun findKeywordMatches(query: String, knowledgeItems: List<KnowledgeItem>): List<ScoredItem> {
        val queryWords = query.split("\\s+".toRegex()).filter { it.length > 2 }.toSet()
        
        return knowledgeItems
            .filter { it.isActive }
            .mapNotNull { item ->
                val score = calculateKeywordScore(queryWords, item)
                if (score > 0) ScoredItem(item, score) else null
            }
            .sortedByDescending { it.score }
    }
    
    private fun calculateKeywordScore(queryWords: Set<String>, item: KnowledgeItem): Double {
        val textToSearch = "${item.question} ${item.answer} ${item.tags ?: ""}".lowercase()
        val matchedWords = queryWords.count { textToSearch.contains(it) }
        return matchedWords.toDouble() / queryWords.size
    }
    
    private fun findCategoryMatch(query: String, knowledgeItems: List<KnowledgeItem>): KnowledgeItem? {
        val categoryKeywords = mapOf(
            "skills" to listOf("skill", "technology", "language", "framework", "tool"),
            "experience" to listOf("experience", "work", "job", "company", "role"),
            "projects" to listOf("project", "portfolio", "github", "application"),
            "education" to listOf("education", "degree", "university", "school", "certification"),
            "contact" to listOf("contact", "email", "phone", "linkedin", "social")
        )
        
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { query.contains(it) }) {
                return knowledgeItems.firstOrNull { 
                    it.category.equals(category, ignoreCase = true) && it.isActive 
                }
            }
        }
        
        return null
    }
}

data class ChatbotResponse(
    val answer: String,
    val confidence: Double,
    val matchedItem: KnowledgeItem?,
    val category: String?
)

data class ScoredItem(
    val item: KnowledgeItem,
    val score: Double
)
