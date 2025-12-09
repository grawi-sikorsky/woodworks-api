package uk.jsikora.woodworksapi.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.UUID;

@Data
@Builder
@Entity
@AllArgsConstructor
public class BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;

    private String email;
    private String name;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;
    private String login;
    private String locale;
    
    @Builder.Default
    private Long generationRequestCount = 0L;
    
    @Builder.Default
    private Integer projectCount = 0;
    
    @Builder.Default
    private Integer maxProjects = 5;
    
    @Builder.Default
    private Integer maxCabinetsPerProject = 10;

    public BaseUser() {
    }
}