package com.portfolio.controller

import com.portfolio.mapper.KnowledgeMapper
import com.portfolio.model.KnowledgeItemDTO
import com.portfolio.service.KnowledgeService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = ["*"])
class KnowledgeController(
    private val knowledgeService: KnowledgeService,
    private val knowledgeMapper: KnowledgeMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(KnowledgeController::class.java)
    }

    /**
     * Get all knowledge items
     */
    @GetMapping
    fun getAllKnowledge(): ResponseEntity<List<KnowledgeItemDTO>> {
        log.debug("Fetching all knowledge items")
        val items = knowledgeService.getAllKnowledgeItems()
        return ResponseEntity.ok(knowledgeMapper.toDtoList(items))
    }

    /**
     * Get knowledge by category
     */
    @GetMapping("/category/{category}")
    fun getByCategory(@PathVariable category: String): ResponseEntity<List<KnowledgeItemDTO>> {
        log.debug("Fetching knowledge items for category: {}", category)
        val items = knowledgeService.getKnowledgeByCategory(category)
        return ResponseEntity.ok(knowledgeMapper.toDtoList(items))
    }

    /**
     * Get a single knowledge item
     */
    @GetMapping("/{id}")
    fun getKnowledgeItem(@PathVariable id: Long): ResponseEntity<KnowledgeItemDTO?> {
        log.debug("Fetching knowledge item with id: {}", id)
        val item = knowledgeService.getKnowledgeItem(id)
        return if (item != null) {
            ResponseEntity.ok(knowledgeMapper.toDto(item))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Create a new knowledge item
     */
    @PostMapping
    fun createKnowledge(@RequestBody dto: KnowledgeItemDTO): ResponseEntity<KnowledgeItemDTO> {
        log.info("Creating new knowledge item via API")
        val created = knowledgeService.createKnowledgeItem(dto)
        return ResponseEntity.ok(knowledgeMapper.toDto(created))
    }

    /**
     * Update an existing knowledge item
     */
    @PutMapping("/{id}")
    fun updateKnowledge(@PathVariable id: Long, @RequestBody dto: KnowledgeItemDTO): ResponseEntity<KnowledgeItemDTO?> {
        log.info("Updating knowledge item with id: {} via API", id)
        val updated = knowledgeService.updateKnowledgeItem(id, dto)
        return if (updated != null) {
            ResponseEntity.ok(knowledgeMapper.toDto(updated))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Delete a knowledge item (soft delete)
     */
    @DeleteMapping("/{id}")
    fun deleteKnowledge(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        log.info("Deleting knowledge item with id: {} via API", id)
        val deleted = knowledgeService.deleteKnowledgeItem(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Knowledge item deleted"))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Search knowledge items
     */
    @GetMapping("/search")
    fun searchKnowledge(@RequestParam query: String): ResponseEntity<List<KnowledgeItemDTO>> {
        log.debug("Searching knowledge items with query: {}", query)
        val items = knowledgeService.searchKnowledge(query)
        return ResponseEntity.ok(knowledgeMapper.toDtoList(items))
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<String>> {
        log.debug("Fetching all categories")
        return ResponseEntity.ok(knowledgeService.getAllCategories())
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Any>> {
        log.debug("Fetching knowledge base statistics")
        return ResponseEntity.ok(knowledgeService.getStatistics())
    }

    /**
     * Import knowledge items from JSON
     */
    @PostMapping("/import")
    fun importKnowledge(@RequestBody items: List<KnowledgeItemDTO>): ResponseEntity<Map<String, Int>> {
        log.info("Importing {} knowledge items via API", items.size)
        val count = knowledgeService.importKnowledgeItems(items)
        return ResponseEntity.ok(mapOf("imported" to count))
    }
}
