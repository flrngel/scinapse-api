package io.scinapse.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScinapseConstant {

    public static String SCINAPSE_MEDIA_URL;

    @Value("${pluto.server.web.url.scinapse-media}")
    public void setScinapseMediaUrl(String scinapseMediaUrl) {
        SCINAPSE_MEDIA_URL = scinapseMediaUrl;
    }

}
