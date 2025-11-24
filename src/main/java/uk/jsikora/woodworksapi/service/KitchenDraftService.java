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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KitchenDraftService {

    private final KitchenDraftRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public KitchenDraftDto saveDraft(Long userId, SaveKitchenDraftRequest request) {
        KitchenDraft draft = new KitchenDraft();
        draft.setUserId(userId);
        draft.setName(request.getName());
        
        try {
            draft.setCabinetsJson(objectMapper.writeValueAsString(request.getCabinets()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cabinets", e);
        }

        draft = repository.save(draft);
        return toDto(draft);
    }

    @Transactional
    public KitchenDraftDto updateDraft(Long userId, UUID uuid, SaveKitchenDraftRequest request) {
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

    @Transactional
    public void renameDraft(Long userId, UUID uuid, String newName) {
        KitchenDraft draft = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        draft.setName(newName);
        repository.save(draft);
    }

    @Transactional(readOnly = true)
    public List<KitchenDraftSummaryDto> getUserDraftSummaries(Long userId) {
        return repository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KitchenDraftDto getDraft(Long userId, UUID uuid) {
        KitchenDraft draft = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        return toDto(draft);
    }

    @Transactional
    public void deleteDraft(Long userId, UUID uuid) {
        repository.deleteByUuidAndUserId(uuid, userId);
    }

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

    private KitchenDraftSummaryDto toSummaryDto(KitchenDraft draft) {
        return new KitchenDraftSummaryDto(
            draft.getUuid(),
            draft.getName(),
            draft.getCreatedAt(),
            draft.getUpdatedAt()
        );
    }
}
