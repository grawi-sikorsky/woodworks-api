package uk.jsikora.woodworksapi.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenDraftDto {
    
    private UUID uuid;
    private String name;
    private JsonNode cabinets; // Will hold the cabinet array as JSON
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
