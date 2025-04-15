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
    public Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider) {
        return userRepository.findByEmailAndProvider(email, provider);
    }

    public Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public BaseUser registerOAuthUser(BaseUser baseUser) {
        if (userRepository.existsByEmailAndProvider(baseUser.getEmail(), baseUser.getProvider())) {
            throw new IllegalStateException("Użytkownik z tym adresem e-mail i providerem OA2 już istnieje.");
        }

        return userRepository.findByEmailAndProvider(baseUser.getEmail(), baseUser.getProvider())
                                          .orElseGet(() -> {
                                              BaseUser newBaseUser = new BaseUser();
                                              newBaseUser.setEmail(baseUser.getEmail());
                                              newBaseUser.setName(baseUser.getName());
                                              newBaseUser.setImageUrl(baseUser.getImageUrl());
                                              newBaseUser.setProvider(baseUser.getProvider());
                                              newBaseUser.setProviderId(baseUser.getProviderId());
                                              newBaseUser.setLogin(baseUser.getLogin());
                                              return userRepository.save(newBaseUser);
                                          });
    }
}
