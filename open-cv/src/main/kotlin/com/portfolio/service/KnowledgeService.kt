package com.portfolio.service

import com.portfolio.model.KnowledgeItem
import com.portfolio.model.KnowledgeItemDTO
import com.portfolio.repository.KnowledgeItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class KnowledgeService(
    private val knowledgeItemRepository: KnowledgeItemRepository
) {

    /**
     * Get all active knowledge items
     */
    fun getAllKnowledgeItems(): List<KnowledgeItem> {
        return knowledgeItemRepository.findByIsActiveTrue()
    }

    /**
     * Get knowledge items by category
     */
    fun getKnowledgeByCategory(category: String): List<KnowledgeItem> {
        return knowledgeItemRepository.findByCategoryAndIsActiveTrue(category)
    }

    /**
     * Get a single knowledge item by ID
     */
    fun getKnowledgeItem(id: Long): KnowledgeItem? {
        return knowledgeItemRepository.findById(id).orElse(null)
    }

    /**
     * Create a new knowledge item
     */
    @Transactional
    fun createKnowledgeItem(dto: KnowledgeItemDTO): KnowledgeItem {
        val item = KnowledgeItem(
            category = dto.category,
            question = dto.question,
            answer = dto.answer,
            patterns = dto.patterns.toMutableList(),
            confidence = dto.confidence,
            isActive = dto.isActive,
            tags = dto.tags,
            metadata = dto.metadata,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return knowledgeItemRepository.save(item)
    }

    /**
     * Update an existing knowledge item
     */
    @Transactional
    fun updateKnowledgeItem(id: Long, dto: KnowledgeItemDTO): KnowledgeItem? {
        val existingItem = knowledgeItemRepository.findById(id).orElse(null) ?: return null
        
        val updatedItem = existingItem.copy(
            category = dto.category,
            question = dto.question,
            answer = dto.answer,
            // Update patterns
            patterns = applyPatternUpdates(existingItem.patterns, dto.patterns),
            confidence = dto.confidence,
            isActive = dto.isActive,
            tags = dto.tags,
            metadata = dto.metadata,
            updatedAt = LocalDateTime.now()
        )
        
        return knowledgeItemRepository.save(updatedItem)
    }

    /**
     * Delete a knowledge item (soft delete)
     */
    @Transactional
    fun deleteKnowledgeItem(id: Long): Boolean {
        val item = knowledgeItemRepository.findById(id).orElse(null) ?: return false
        val deactivatedItem = item.copy(isActive = false, updatedAt = LocalDateTime.now())
        knowledgeItemRepository.save(deactivatedItem)
        return true
    }

    /**
     * Hard delete a knowledge item
     */
    @Transactional
    fun hardDeleteKnowledgeItem(id: Long): Boolean {
        if (!knowledgeItemRepository.existsById(id)) return false
        knowledgeItemRepository.deleteById(id)
        return true
    }

    /**
     * Search knowledge items
     */
    fun searchKnowledge(query: String): List<KnowledgeItem> {
        return knowledgeItemRepository.searchByQuery(query)
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): List<String> {
        return knowledgeItemRepository.findAllCategories()
    }

    /**
     * Get statistics
     */
    fun getStatistics(): Map<String, Any> {
        val totalItems = knowledgeItemRepository.countByIsActiveTrue()
        val categories = knowledgeItemRepository.findAllCategories()
        val categoryCounts = categories.associateWith { category ->
            knowledgeItemRepository.countByCategoryAndIsActiveTrue(category)
        }
        
        return mapOf(
            "totalItems" to totalItems,
            "categories" to categories,
            "categoryCounts" to categoryCounts
        )
    }

    /**
     * Import knowledge items from JSON or other format
     */
    @Transactional
    fun importKnowledgeItems(items: List<KnowledgeItemDTO>): Int {
        var count = 0
        items.forEach { dto ->
            try {
                createKnowledgeItem(dto)
                count++
            } catch (e: Exception) {
                // Log error but continue with other items
                println("Failed to import item: ${dto.question} - ${e.message}")
            }
        }
        return count
    }

    private fun applyPatternUpdates(existingPatterns: MutableList<String>, newPatterns: List<String>): MutableList<String> {
        // Simple strategy: replace all patterns
        return newPatterns.toMutableList()
    }
}
