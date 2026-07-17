package com.govos.security.resolver;

import com.govos.security.annotation.CurrentUser;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserArgumentResolverTest {

    private static final UUID USER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserArgumentResolver();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSupportCurrentUserJwtPrincipalParameter() throws Exception {
        MethodParameter parameter = methodParameter(JwtPrincipal.class);

        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void shouldRejectParametersWithoutCurrentUserAnnotation() throws Exception {
        MethodParameter parameter = new MethodParameter(
                UnsupportedController.class.getDeclaredMethod("withoutAnnotation", JwtPrincipal.class),
                0);

        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void shouldResolveJwtPrincipalFromSecurityContext() throws Exception {
        JwtPrincipal principal = new JwtPrincipal(
                USER_ID,
                "jdoe",
                "session-1",
                "jti-1",
                List.of(),
                "token");
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthentication(principal, "token", List.of()));

        Object resolved = resolver.resolveArgument(
                methodParameter(JwtPrincipal.class),
                new ModelAndViewContainer(),
                new ServletWebRequest(mock(jakarta.servlet.http.HttpServletRequest.class)),
                null);

        assertThat(resolved).isEqualTo(principal);
    }

    @Test
    void shouldThrowWhenAuthenticationIsMissing() throws Exception {
        assertThatThrownBy(() -> resolver.resolveArgument(
                        methodParameter(JwtPrincipal.class),
                        new ModelAndViewContainer(),
                        new ServletWebRequest(mock(jakarta.servlet.http.HttpServletRequest.class)),
                        null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authenticated user principal is not available");
    }

    private MethodParameter methodParameter(Class<?> parameterType) throws NoSuchMethodException {
        return new MethodParameter(
                SupportedController.class.getDeclaredMethod("withCurrentUser", parameterType),
                0);
    }

    static class SupportedController {
        void withCurrentUser(@CurrentUser JwtPrincipal user) {
        }
    }

    static class UnsupportedController {
        void withoutAnnotation(JwtPrincipal user) {
        }
    }
}
