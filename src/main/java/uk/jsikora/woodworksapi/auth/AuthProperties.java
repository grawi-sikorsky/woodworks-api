package uk.jsikora.woodworksapi.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
class AuthProperties {

    @Value("${auth.enabled}")
    private boolean enabled;

}
