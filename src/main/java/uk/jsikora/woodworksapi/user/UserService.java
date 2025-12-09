package uk.jsikora.woodworksapi.user;

import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

/**
 * Service interface for user management operations.
 * Defines methods for user lookup, registration, and counter management.
 */
public interface UserService {
    /**
     * Finds a user by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<BaseUser> findByEmail(String email);
    
    /**
     * Finds a user by email and authentication provider.
     * 
     * @param email the email address
     * @param provider the authentication provider
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider);
    
    /**
     * Finds a user by authentication provider and provider-specific user ID.
     * 
     * @param provider the authentication provider
     * @param providerId the provider-specific user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    /**
     * Registers a new OAuth user or retrieves existing user.
     * 
     * @param baseUser the user data from OAuth provider
     * @return the registered or existing user
     */
    BaseUser registerOAuthUser(BaseUser baseUser);
    
    /**
     * Finds a user by their ID.
     * 
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<BaseUser> findById(Long id);
    
    /**
     * Increments the generation request count for a user.
     * 
     * @param userId the ID of the user
     */
    void incrementGenerationCount(Long userId);
    
    /**
     * Increments the project count for a user.
     * 
     * @param userId the ID of the user
     */
    void incrementProjectCount(Long userId);
    
    /**
     * Decrements the project count for a user.
     * 
     * @param userId the ID of the user
     */
    void decrementProjectCount(Long userId);
}