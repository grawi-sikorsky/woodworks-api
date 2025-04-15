package uk.jsikora.woodworksapi.auth;

import lombok.Getter;

@Getter
public enum AuthProvider {
    GOOGLE("google"),
    GITHUB("github"),
    LOCAL("local");

    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

    public static AuthProvider fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Provider value cannot be null");
        }
        for (AuthProvider provider : AuthProvider.values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown provider: " + value);
    }
}