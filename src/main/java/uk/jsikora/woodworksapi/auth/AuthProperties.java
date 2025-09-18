package uk.jsikora.woodworksapi.auth;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthProperties {

    @Value("${auth.oauth-redirect-uri}")
    private String oauthRedirectUri;

}
