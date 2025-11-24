package uk.jsikora.woodworksapi.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveKitchenDraftRequest {
    
    private String name;
    private JsonNode cabinets; // Cabinet array
}
