package uk.jsikora.woodworksapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.jsikora.woodworksapi.dto.KitchenDraftDto;
import uk.jsikora.woodworksapi.dto.KitchenDraftSummaryDto;
import uk.jsikora.woodworksapi.dto.SaveKitchenDraftRequest;
import uk.jsikora.woodworksapi.entity.KitchenDraft;
import uk.jsikora.woodworksapi.repository.KitchenDraftRepository;
import uk.jsikora.woodworksapi.user.BaseUser;
import uk.jsikora.woodworksapi.user.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing kitchen design drafts.
 * Handles CRUD operations, validation of user limits, and JSON serialization of cabinet data.
 */
@Service
@RequiredArgsConstructor
public class KitchenDraftService {

    private final KitchenDraftRepository repository;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    /**
     * Saves a new kitchen draft for the user.
     * Validates project count and cabinet limits before saving.
     * 
     * @param userId the ID of the user creating the draft
     * @param request the draft data to save
     * @return the created draft DTO
     * @throws RuntimeException if user not found or limits exceeded
     */
    @Transactional
    public KitchenDraftDto saveDraft(Long userId, SaveKitchenDraftRequest request) {
        BaseUser user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProjectCount() >= user.getMaxProjects()) {
            throw new RuntimeException("Przekroczono limit projektów dla Twojego konta (" + user.getMaxProjects() + ").");
        }

        if (request.getCabinets().size() > user.getMaxCabinetsPerProject()) {
            throw new RuntimeException("Przekroczono limit szafek na projekt (" + user.getMaxCabinetsPerProject() + ").");
        }

        KitchenDraft draft = new KitchenDraft();
        draft.setUserId(userId);
        draft.setName(request.getName());
        
        try {
            draft.setCabinetsJson(objectMapper.writeValueAsString(request.getCabinets()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cabinets", e);
        }

        draft = repository.save(draft);
        userService.incrementProjectCount(userId);
        
        return toDto(draft);
    }

    /**
     * Updates an existing kitchen draft.
     * Validates cabinet limits before updating.
     * 
     * @param userId the ID of the user owning the draft
     * @param uuid the UUID of the draft to update
     * @param request the updated draft data
     * @return the updated draft DTO
     * @throws RuntimeException if user or draft not found, or limits exceeded
     */
    @Transactional
    public KitchenDraftDto updateDraft(Long userId, UUID uuid, SaveKitchenDraftRequest request) {
        BaseUser user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getCabinets().size() > user.getMaxCabinetsPerProject()) {
            throw new RuntimeException("Przekroczono limit szafek na projekt (" + user.getMaxCabinetsPerProject() + ").");
        }

        KitchenDraft draft = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        draft.setName(request.getName());
        
        try {
            draft.setCabinetsJson(objectMapper.writeValueAsString(request.getCabinets()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cabinets", e);
        }

        draft = repository.save(draft);
        return toDto(draft);
    }

    /**
     * Renames an existing kitchen draft.
     * 
     * @param userId the ID of the user owning the draft
     * @param uuid the UUID of the draft to rename
     * @param newName the new name for the draft
     * @throws RuntimeException if draft not found
     */
    @Transactional
    public void renameDraft(Long userId, UUID uuid, String newName) {
        KitchenDraft draft = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        draft.setName(newName);
        repository.save(draft);
    }

    /**
     * Retrieves summary information for all drafts owned by the user.
     * Returns drafts ordered by last update time (most recent first).
     * 
     * @param userId the ID of the user
     * @return list of draft summaries
     */
    @Transactional(readOnly = true)
    public List<KitchenDraftSummaryDto> getUserDraftSummaries(Long userId) {
        return repository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific kitchen draft by UUID.
     * 
     * @param userId the ID of the user owning the draft
     * @param uuid the UUID of the draft to retrieve
     * @return the full draft DTO with cabinet data
     * @throws RuntimeException if draft not found
     */
    @Transactional(readOnly = true)
    public KitchenDraftDto getDraft(Long userId, UUID uuid) {
        KitchenDraft draft = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        return toDto(draft);
    }

    /**
     * Deletes a kitchen draft and decrements the user's project count.
     * 
     * @param userId the ID of the user owning the draft
     * @param uuid the UUID of the draft to delete
     */
    @Transactional
    public void deleteDraft(Long userId, UUID uuid) {
        repository.deleteByUuidAndUserId(uuid, userId);
        userService.decrementProjectCount(userId);
    }

    /**
     * Converts a KitchenDraft entity to a DTO.
     * Deserializes the JSON cabinet data.
     * 
     * @param draft the entity to convert
     * @return the DTO representation
     * @throws RuntimeException if JSON deserialization fails
     */
    private KitchenDraftDto toDto(KitchenDraft draft) {
        KitchenDraftDto dto = new KitchenDraftDto();
        dto.setUuid(draft.getUuid());
        dto.setName(draft.getName());
        dto.setCreatedAt(draft.getCreatedAt());
        dto.setUpdatedAt(draft.getUpdatedAt());
        
        try {
            dto.setCabinets(objectMapper.readTree(draft.getCabinetsJson()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize cabinets", e);
        }
        
        return dto;
    }

    /**
     * Converts a KitchenDraft entity to a summary DTO.
     * 
     * @param draft the entity to convert
     * @return the summary DTO representation
     */
    private KitchenDraftSummaryDto toSummaryDto(KitchenDraft draft) {
        return new KitchenDraftSummaryDto(
            draft.getUuid(),
            draft.getName(),
            draft.getCreatedAt(),
            draft.getUpdatedAt()
        );
    }
}
