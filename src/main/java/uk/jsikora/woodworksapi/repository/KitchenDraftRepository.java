package uk.jsikora.woodworksapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.jsikora.woodworksapi.entity.KitchenDraft;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KitchenDraftRepository extends JpaRepository<KitchenDraft, Long> {
    
    List<KitchenDraft> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    Optional<KitchenDraft> findByUuidAndUserId(UUID uuid, Long userId);
    
    void deleteByUuidAndUserId(UUID uuid, Long userId);
}
