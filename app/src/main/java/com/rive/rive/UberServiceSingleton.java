package com.rive.rive;

import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;

// Provides reference to a singular
// uber OAuth 2.0 serive (from scribe)
public class UberServiceSingleton {
    static OAuthService uberService;

    public static OAuthService getUberService(String clientId, String secret) {
        if (uberService != null)
            return uberService;
        else {
            uberService = new ServiceBuilder()
                    .provider(UberAPI.class)
                    .apiKey(clientId)
                    .apiSecret(secret)
                    .callback("localhost://callback")
                    .build();

            return uberService;
        }
    }
}
