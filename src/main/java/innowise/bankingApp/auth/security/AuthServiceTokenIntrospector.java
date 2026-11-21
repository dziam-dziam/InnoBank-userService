
package innowise.bankingApp.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class AuthServiceTokenIntrospector implements OpaqueTokenIntrospector {

    private final RestClient client;

    public AuthServiceTokenIntrospector(
            @Value("${services.auth.url}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(httpClient);
        requests.setReadTimeout(Duration.ofSeconds(5));

        client = RestClient.builder().baseUrl(baseUrl).requestFactory(requests).build();
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        TokenStatus result;
        try {
            RestClient.RequestHeadersSpec<?> request = client.get()
                    .uri("/api/auth/validate");
            request.headers(headers -> headers.setBearerAuth(token));

            result = request.retrieve().body(TokenStatus.class);
        } catch (HttpClientErrorException.Unauthorized exception) {
            throw new BadOpaqueTokenException("Invalid access token");
        } catch (RestClientException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("temporarily_unavailable"), exception
            );
        }

        if (result == null || !result.valid() || result.userId() == null
                || result.userId() <= 0
                || !("ADMIN".equals(result.role()) || "USER".equals(result.role()))) {
            throw new BadOpaqueTokenException("Invalid access token");
        }

        String userId = result.userId().toString();
        return new DefaultOAuth2AuthenticatedPrincipal(
                userId,
                Map.of("sub", userId),
                List.of(new SimpleGrantedAuthority("ROLE_" + result.role()))
        );
    }

    private record TokenStatus(boolean valid, Long userId, String role) {
    }
}
