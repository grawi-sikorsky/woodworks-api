package uk.jsikora.woodworksapi.auth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import uk.jsikora.woodworksapi.user.BaseUser;
import uk.jsikora.woodworksapi.user.UserRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.jsikora.woodworksapi.auth.AuthProvider.GITHUB;

@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomOauth2UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);

        String provider = request.getClientRegistration()
                                 .getRegistrationId(); // google/github
        String providerId = oAuth2User.getAttribute("sub"); // google: "sub", github: "id"
        if (GITHUB.toString().equals(provider.toUpperCase())) {
            Object id = oAuth2User.getAttribute("id");
            if (id == null) {
                throw new OAuth2AuthenticationException("GitHub ID attribute is missing");
            }
            providerId = id.toString(); // Safely convert to String
        }

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String imageUrl = oAuth2User.getAttribute("picture"); // google
        if (GITHUB.toString().equals(provider.toUpperCase())) {
            imageUrl = oAuth2User.getAttribute("avatar_url");
        }

        // Rejestracja/logowanie
        String finalProviderId = providerId;
        String finalImageUrl = imageUrl;
        BaseUser baseUser = userRepository.findByEmail(email)
                                          .orElseGet(() -> {
                                      BaseUser newBaseUser = new BaseUser();
                                      newBaseUser.setEmail(email);
                                      newBaseUser.setName(name);
                                      newBaseUser.setImageUrl(finalImageUrl);
                                      newBaseUser.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
                                      newBaseUser.setProviderId(finalProviderId);
                                      return userRepository.save(newBaseUser);
                                  });

        // Token JWT
        String token = jwtService.generateToken(baseUser);

        // Token jako atrybut przekazany do handlera success
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("jwt", token);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "name"
        );
    }
}