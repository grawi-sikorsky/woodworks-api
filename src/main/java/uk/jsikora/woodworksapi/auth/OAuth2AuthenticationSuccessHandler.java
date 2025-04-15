package uk.jsikora.woodworksapi.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.jsikora.woodworksapi.user.BaseUser;
import uk.jsikora.woodworksapi.user.UserRepository;
import uk.jsikora.woodworksapi.user.UserServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.jsikora.woodworksapi.auth.AuthProvider.GITHUB;
import static uk.jsikora.woodworksapi.auth.AuthProvider.GOOGLE;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserServiceImpl userServiceImpl;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

//        String providerString = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
//        AuthProvider provider = AuthProvider.fromString(providerString);
//        String providerId = extractProviderId(provider, oAuth2User);

        String providerStr = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        AuthProvider provider = AuthProvider.fromString(providerStr);

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


        // Znajdź po provider i providerId
        BaseUser baseUser = userServiceImpl.findByEmailAndProvider(email, provider)
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

        // 4. Extra claims do JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("provider", provider);
        extraClaims.put("providerId", providerId);

        String token = jwtService.generateToken(baseUser, extraClaims);

        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/oauth2/redirect")
                                                 .queryParam("token", token)
                                                 .build()
                                                 .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractProviderId(AuthProvider provider, OAuth2User oAuth2User) {
        if (GITHUB.equals(provider)) {
            Object id = oAuth2User.getAttribute("id");
            return Objects.requireNonNull(id)
                          .toString();
        }
        else if (GOOGLE.equals(provider)) {
            return oAuth2User.getAttribute("sub");
        }
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
    }
}