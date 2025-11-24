package uk.jsikora.woodworksapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenDraftSummaryDto {
    
    private UUID uuid;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
