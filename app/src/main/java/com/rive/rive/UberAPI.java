package com.rive.rive;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;

/**
 * Generic service provider for two-step OAuth10a.
 */
public class UberAPI extends DefaultApi20 {

    @Override
    public String getAccessTokenEndpoint() {
        return "https://login.uber.com/oauth/token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return "https://login.uber.com/oauth/authorize";
    }
}