package uk.jsikora.woodworksapi.user;

import org.springframework.stereotype.Service;
import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<BaseUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public BaseUser registerOAuthUser(String email, String name, AuthProvider provider) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Użytkownik z tym adresem e-mail już istnieje.");
        }

        BaseUser baseUser = BaseUser.builder()
                                    .email(email)
                                    .name(name)
                                    .provider(provider)
                                    .build();

        return userRepository.save(baseUser);
    }
}
