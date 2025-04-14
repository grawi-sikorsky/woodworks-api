package uk.jsikora.woodworksapi.user;

import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

public interface UserService {
    Optional<BaseUser> findByEmail(String email);
    BaseUser registerOAuthUser(String email, String name, AuthProvider provider);
}