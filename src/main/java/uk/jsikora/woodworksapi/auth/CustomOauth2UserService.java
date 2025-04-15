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

        String providerStr = request.getClientRegistration().getRegistrationId(); // google/github
        AuthProvider provider = AuthProvider.fromString(providerStr); // Convert to enum

        String providerId;
        String login = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String imageUrl;

        switch (provider) {
        case GITHUB:
            Object id = oAuth2User.getAttribute("id");
            if (id == null) {
                throw new OAuth2AuthenticationException("GitHub ID attribute is missing");
            }
            providerId = id.toString();
            imageUrl = oAuth2User.getAttribute("avatar_url");
            break;
        case GOOGLE:
            providerId = oAuth2User.getAttribute("sub"); // Google uses "sub" for ID
            imageUrl = oAuth2User.getAttribute("picture");
            break;
        case LOCAL:
            throw new UnsupportedOperationException("Local provider not supported for OAuth");
        default:
            throw new IllegalStateException("Unexpected provider: " + provider);
        }

        BaseUser baseUser = userRepository.findByEmailAndProvider(email, provider)
                                          .orElseGet(() -> {
                                              BaseUser newBaseUser = new BaseUser();
                                              newBaseUser.setEmail(email);
                                              newBaseUser.setName(name);
                                              newBaseUser.setImageUrl(imageUrl);
                                              newBaseUser.setProvider(provider);
                                              newBaseUser.setProviderId(providerId);
                                              newBaseUser.setLogin(login);
                                              return userRepository.save(newBaseUser);
                                          });

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("baseUserId", baseUser.getId()); // opcjonalnie: przyda się później

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "name"
        );

//        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);
//
//        String providerStr = request.getClientRegistration().getRegistrationId(); // google/github
//        AuthProvider provider = AuthProvider.fromString(providerStr); // Convert to enum
//
//        String providerId;
//        String login = oAuth2User.getAttribute("login");
//        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");
//        String imageUrl;
//
//        switch (provider) {
//        case GITHUB:
//            Object id = oAuth2User.getAttribute("id");
//            if (id == null) {
//                throw new OAuth2AuthenticationException("GitHub ID attribute is missing");
//            }
//            providerId = id.toString();
//            imageUrl = oAuth2User.getAttribute("avatar_url");
//            break;
//        case GOOGLE:
//            providerId = oAuth2User.getAttribute("sub"); // Google uses "sub" for ID
//            imageUrl = oAuth2User.getAttribute("picture");
//            break;
//        case LOCAL:
//            throw new UnsupportedOperationException("Local provider not supported for OAuth");
//        default:
//            throw new IllegalStateException("Unexpected provider: " + provider);
//        }
//
//        BaseUser baseUser = userRepository.findByEmailAndProvider(email, provider)
//                                          .orElseGet(() -> {
//                                              BaseUser newBaseUser = new BaseUser();
//                                              newBaseUser.setEmail(email);
//                                              newBaseUser.setName(name);
//                                              newBaseUser.setImageUrl(imageUrl);
//                                              newBaseUser.setProvider(provider);
//                                              newBaseUser.setProviderId(providerId);
//                                              return userRepository.save(newBaseUser);
//                                          });
//
//        // Token JWT
//        String token = jwtService.generateToken(baseUser);
//
//        // Token jako atrybut przekazany do handlera success
//        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
//        attributes.put("jwt", token);
//
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority("USER")),
//                attributes,
//                "name"
//        );
    }
}