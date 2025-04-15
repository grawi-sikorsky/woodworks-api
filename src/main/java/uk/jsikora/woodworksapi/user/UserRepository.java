package uk.jsikora.woodworksapi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByEmail(String email);
    Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider);
    Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndProvider(String email, AuthProvider provider);
    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}