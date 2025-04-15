package uk.jsikora.woodworksapi.user;

import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

public interface UserService {
    Optional<BaseUser> findByEmail(String email);
    Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider);
    Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId);
    BaseUser registerOAuthUser(BaseUser baseUser);
}