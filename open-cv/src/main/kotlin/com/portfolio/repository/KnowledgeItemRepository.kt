package com.portfolio.repository

import com.portfolio.model.KnowledgeItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface KnowledgeItemRepository : JpaRepository<KnowledgeItem, Long> {
    
    fun findByCategory(category: String): List<KnowledgeItem>
    
    fun findByIsActiveTrue(): List<KnowledgeItem>
    
    fun findByCategoryAndIsActiveTrue(category: String): List<KnowledgeItem>
    
    @Query("SELECT k FROM KnowledgeItem k WHERE k.isActive = true AND " +
           "(LOWER(k.question) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.answer) LIKE LOWER(CONCAT('%', :query, '%')))")
    fun searchByQuery(query: String): List<KnowledgeItem>
    
    @Query("SELECT DISTINCT k.category FROM KnowledgeItem k WHERE k.isActive = true")
    fun findAllCategories(): List<String>
    
    fun countByIsActiveTrue(): Int
    
    fun countByCategoryAndIsActiveTrue(category: String): Int
}
