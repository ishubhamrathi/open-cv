package com.portfolio.controller

import com.portfolio.model.KnowledgeItemDTO
import com.portfolio.service.KnowledgeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = ["*"])
class KnowledgeController(
    private val knowledgeService: KnowledgeService
) {

    /**
     * Get all knowledge items
     */
    @GetMapping
    fun getAllKnowledge(): ResponseEntity<List<KnowledgeItemDTO>> {
        val items = knowledgeService.getAllKnowledgeItems().map { item ->
            KnowledgeItemDTO(
                id = item.id,
                category = item.category,
                question = item.question,
                answer = item.answer,
                patterns = item.patterns,
                confidence = item.confidence,
                isActive = item.isActive,
                tags = item.tags,
                metadata = item.metadata
            )
        }
        return ResponseEntity.ok(items)
    }

    /**
     * Get knowledge by category
     */
    @GetMapping("/category/{category}")
    fun getByCategory(@PathVariable category: String): ResponseEntity<List<KnowledgeItemDTO>> {
        val items = knowledgeService.getKnowledgeByCategory(category).map { item ->
            KnowledgeItemDTO(
                id = item.id,
                category = item.category,
                question = item.question,
                answer = item.answer,
                patterns = item.patterns,
                confidence = item.confidence,
                isActive = item.isActive,
                tags = item.tags,
                metadata = item.metadata
            )
        }
        return ResponseEntity.ok(items)
    }

    /**
     * Get a single knowledge item
     */
    @GetMapping("/{id}")
    fun getKnowledgeItem(@PathVariable id: Long): ResponseEntity<KnowledgeItemDTO?> {
        val item = knowledgeService.getKnowledgeItem(id)?.let {
            KnowledgeItemDTO(
                id = it.id,
                category = it.category,
                question = it.question,
                answer = it.answer,
                patterns = it.patterns,
                confidence = it.confidence,
                isActive = it.isActive,
                tags = it.tags,
                metadata = it.metadata
            )
        }
        return if (item != null) ResponseEntity.ok(item) else ResponseEntity.notFound().build()
    }

    /**
     * Create a new knowledge item
     */
    @PostMapping
    fun createKnowledge(@RequestBody dto: KnowledgeItemDTO): ResponseEntity<KnowledgeItemDTO> {
        val created = knowledgeService.createKnowledgeItem(dto)
        val response = KnowledgeItemDTO(
            id = created.id,
            category = created.category,
            question = created.question,
            answer = created.answer,
            patterns = created.patterns,
            confidence = created.confidence,
            isActive = created.isActive,
            tags = created.tags,
            metadata = created.metadata
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Update an existing knowledge item
     */
    @PutMapping("/{id}")
    fun updateKnowledge(@PathVariable id: Long, @RequestBody dto: KnowledgeItemDTO): ResponseEntity<KnowledgeItemDTO?> {
        val updated = knowledgeService.updateKnowledgeItem(id, dto)
        return if (updated != null) {
            ResponseEntity.ok(KnowledgeItemDTO(
                id = updated.id,
                category = updated.category,
                question = updated.question,
                answer = updated.answer,
                patterns = updated.patterns,
                confidence = updated.confidence,
                isActive = updated.isActive,
                tags = updated.tags,
                metadata = updated.metadata
            ))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Delete a knowledge item (soft delete)
     */
    @DeleteMapping("/{id}")
    fun deleteKnowledge(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
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
        val items = knowledgeService.searchKnowledge(query).map { item ->
            KnowledgeItemDTO(
                id = item.id,
                category = item.category,
                question = item.question,
                answer = item.answer,
                patterns = item.patterns,
                confidence = item.confidence,
                isActive = item.isActive,
                tags = item.tags,
                metadata = item.metadata
            )
        }
        return ResponseEntity.ok(items)
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(knowledgeService.getAllCategories())
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(knowledgeService.getStatistics())
    }

    /**
     * Import knowledge items from JSON
     */
    @PostMapping("/import")
    fun importKnowledge(@RequestBody items: List<KnowledgeItemDTO>): ResponseEntity<Map<String, Int>> {
        val count = knowledgeService.importKnowledgeItems(items)
        return ResponseEntity.ok(mapOf("imported" to count))
    }
}
