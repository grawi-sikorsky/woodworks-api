package uk.jsikora.woodworksapi.user;

import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

/** Service for user management operations. */
public interface UserService {
    Optional<BaseUser> findByEmail(String email);
    
    Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider);
    
    Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    /** Registers new OAuth user or retrieves existing. */
    BaseUser registerOAuthUser(BaseUser baseUser);
    
    Optional<BaseUser> findById(Long id);
    
    void incrementGenerationCount(Long userId);
    
    void incrementProjectCount(Long userId);
    
    void decrementProjectCount(Long userId);
}