package com.govos.security.resolver;

import com.govos.security.annotation.CurrentUser;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link JwtPrincipal} parameters annotated with {@link CurrentUser}.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && JwtPrincipal.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            return jwtAuthentication.getPrincipal();
        }
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Authenticated user principal is not available");
    }
}
