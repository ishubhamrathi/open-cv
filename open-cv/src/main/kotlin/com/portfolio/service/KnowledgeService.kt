package com.portfolio.service

import com.portfolio.exception.InvalidCategoryException
import com.portfolio.exception.ResourceNotFoundException
import com.portfolio.mapper.KnowledgeMapper
import com.portfolio.model.KnowledgeItem
import com.portfolio.model.KnowledgeItemDTO
import com.portfolio.repository.KnowledgeItemRepository
import com.portfolio.util.AppConstants
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class KnowledgeService(
    private val knowledgeItemRepository: KnowledgeItemRepository,
    private val knowledgeMapper: KnowledgeMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(KnowledgeService::class.java)
    }

    /**
     * Get all active knowledge items
     */
    fun getAllKnowledgeItems(): List<KnowledgeItem> {
        log.debug("Fetching all active knowledge items")
        return knowledgeItemRepository.findByIsActiveTrue()
    }

    /**
     * Get knowledge items by category
     */
    fun getKnowledgeByCategory(category: String): List<KnowledgeItem> {
        // Validate category
        if (category !in AppConstants.VALID_CATEGORIES) {
            log.warn("Invalid category requested: {}", category)
            throw InvalidCategoryException(category, AppConstants.VALID_CATEGORIES)
        }
        
        log.debug("Fetching knowledge items for category: {}", category)
        return knowledgeItemRepository.findByCategoryAndIsActiveTrue(category)
    }

    /**
     * Get a single knowledge item by ID
     */
    fun getKnowledgeItem(id: Long): KnowledgeItem? {
        log.debug("Fetching knowledge item with id: {}", id)
        return knowledgeItemRepository.findById(id).orElse(null)
    }

    /**
     * Create a new knowledge item
     */
    @Transactional
    fun createKnowledgeItem(dto: KnowledgeItemDTO): KnowledgeItem {
        log.info("Creating new knowledge item for category: {}", dto.category)
        
        val item = knowledgeMapper.toEntity(dto).copy(
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
        log.info("Updating knowledge item with id: {}", id)
        
        val existingItem = knowledgeItemRepository.findById(id)
            .orElseThrow { 
                log.warn("Knowledge item not found with id: {}", id)
                ResourceNotFoundException("KnowledgeItem", id) 
            }
        
        val updatedItem = knowledgeMapper.updateEntity(existingItem, dto)
            .copy(updatedAt = LocalDateTime.now())
        
        return knowledgeItemRepository.save(updatedItem)
    }

    /**
     * Delete a knowledge item (soft delete)
     */
    @Transactional
    fun deleteKnowledgeItem(id: Long): Boolean {
        log.info("Soft deleting knowledge item with id: {}", id)
        
        val item = knowledgeItemRepository.findById(id)
            .orElseThrow { 
                log.warn("Knowledge item not found with id: {}", id)
                ResourceNotFoundException("KnowledgeItem", id) 
            }
        
        val deactivatedItem = item.copy(isActive = false, updatedAt = LocalDateTime.now())
        knowledgeItemRepository.save(deactivatedItem)
        return true
    }

    /**
     * Hard delete a knowledge item
     */
    @Transactional
    fun hardDeleteKnowledgeItem(id: Long): Boolean {
        log.info("Hard deleting knowledge item with id: {}", id)
        
        if (!knowledgeItemRepository.existsById(id)) {
            log.warn("Knowledge item not found with id: {}", id)
            return false
        }
        
        knowledgeItemRepository.deleteById(id)
        return true
    }

    /**
     * Search knowledge items
     */
    fun searchKnowledge(query: String): List<KnowledgeItem> {
        log.debug("Searching knowledge items with query: {}", query)
        return knowledgeItemRepository.searchByQuery(query)
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): List<String> {
        log.debug("Fetching all categories")
        return knowledgeItemRepository.findAllCategories()
    }

    /**
     * Get statistics
     */
    fun getStatistics(): Map<String, Any> {
        log.debug("Calculating knowledge base statistics")
        
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
        log.info("Importing {} knowledge items", items.size)
        
        var count = 0
        items.forEach { dto ->
            try {
                createKnowledgeItem(dto)
                count++
            } catch (e: Exception) {
                log.error("Failed to import item: ${dto.question}", e)
            }
        }
        
        log.info("Successfully imported {} out of {} knowledge items", count, items.size)
        return count
    }
}
