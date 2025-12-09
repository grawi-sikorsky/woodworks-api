package uk.jsikora.woodworksapi.user;

import org.springframework.stereotype.Service;
import uk.jsikora.woodworksapi.auth.AuthProvider;

import java.util.Optional;

/**
 * Implementation of UserService for managing user operations.
 * Handles user lookup, OAuth registration, and usage counter management.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BaseUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BaseUser> findByEmailAndProvider(String email, AuthProvider provider) {
        return userRepository.findByEmailAndProvider(email, provider);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<BaseUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * {@inheritDoc}
     * Creates a new user if one doesn't exist for the given email and provider combination.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BaseUser> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementGenerationCount(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setGenerationRequestCount(user.getGenerationRequestCount() + 1);
            userRepository.save(user);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementProjectCount(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setProjectCount(user.getProjectCount() + 1);
            userRepository.save(user);
        });
    }

    /**
     * {@inheritDoc}
     * Only decrements if the current count is greater than zero.
     */
    @Override
    public void decrementProjectCount(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getProjectCount() > 0) {
                user.setProjectCount(user.getProjectCount() - 1);
                userRepository.save(user);
            }
        });
    }
}
