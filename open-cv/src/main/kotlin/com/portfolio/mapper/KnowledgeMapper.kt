package com.portfolio.mapper

import com.portfolio.model.KnowledgeItem
import com.portfolio.model.KnowledgeItemDTO
import org.springframework.stereotype.Component

/**
 * Mapper component for converting between KnowledgeItem entities and DTOs.
 * Eliminates duplicate mapping code across controllers and services.
 */
@Component
class KnowledgeMapper {

    /**
     * Convert a KnowledgeItem entity to DTO
     */
    fun toDto(entity: KnowledgeItem): KnowledgeItemDTO {
        return KnowledgeItemDTO(
            id = entity.id,
            category = entity.category,
            question = entity.question,
            answer = entity.answer,
            patterns = entity.patterns,
            confidence = entity.confidence,
            isActive = entity.isActive,
            tags = entity.tags,
            metadata = entity.metadata
        )
    }

    /**
     * Convert a list of KnowledgeItem entities to DTOs
     */
    fun toDtoList(entities: List<KnowledgeItem>): List<KnowledgeItemDTO> {
        return entities.map { toDto(it) }
    }

    /**
     * Convert a KnowledgeItemDTO to entity for creation
     */
    fun toEntity(dto: KnowledgeItemDTO): KnowledgeItem {
        return KnowledgeItem(
            category = dto.category,
            question = dto.question,
            answer = dto.answer,
            patterns = dto.patterns.toMutableList(),
            confidence = dto.confidence,
            isActive = dto.isActive,
            tags = dto.tags,
            metadata = dto.metadata
        )
    }

    /**
     * Update an existing entity with DTO data
     */
    fun updateEntity(entity: KnowledgeItem, dto: KnowledgeItemDTO): KnowledgeItem {
        return entity.copy(
            category = dto.category,
            question = dto.question,
            answer = dto.answer,
            patterns = dto.patterns.toMutableList(),
            confidence = dto.confidence,
            isActive = dto.isActive,
            tags = dto.tags,
            metadata = dto.metadata
        )
    }
}
