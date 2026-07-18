package com.govos.api.srh.security;

import com.govos.api.srh.controller.SearchController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SearchAdminSecurityTest {

    @Test
    void shouldProtectAdminEndpointsWithPermissions() {
        long protectedMethods = Arrays.stream(SearchController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PreAuthorize.class))
                .count();

        assertThat(protectedMethods).isGreaterThanOrEqualTo(23);
    }

    @Test
    void shouldRequireMonitorPermissionForObservabilityEndpoints() throws NoSuchMethodException {
        Method observability = SearchController.class.getDeclaredMethod(
                "getObservabilitySnapshot", jakarta.servlet.http.HttpServletRequest.class);
        Method traces = SearchController.class.getDeclaredMethod(
                "getObservabilityTraces", int.class, jakarta.servlet.http.HttpServletRequest.class);

        assertThat(observability.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_MONITOR");
        assertThat(traces.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_MONITOR");
    }

    @Test
    void shouldRequireMonitorPermissionForHealthEndpoints() throws NoSuchMethodException {
        Method health = SearchController.class.getDeclaredMethod("getClusterHealth", jakarta.servlet.http.HttpServletRequest.class);
        Method operational = SearchController.class.getDeclaredMethod("getOperationalHealth", jakarta.servlet.http.HttpServletRequest.class);

        assertThat(health.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_MONITOR");
        assertThat(operational.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_MONITOR");
    }

    @Test
    void shouldRequireAdminPermissionForSchedulerEndpoints() throws NoSuchMethodException {
        Method scheduler = SearchController.class.getDeclaredMethod("getSchedulerStatus", jakarta.servlet.http.HttpServletRequest.class);

        assertThat(scheduler.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_ADMIN");
    }

    @Test
    void shouldRequireReindexPermissionForReindexEndpoints() throws NoSuchMethodException {
        Method reindex = SearchController.class.getDeclaredMethod(
                "reindexIndex", java.util.UUID.class, jakarta.servlet.http.HttpServletRequest.class);

        assertThat(reindex.getAnnotation(PreAuthorize.class).value())
                .contains("SRH_REINDEX");
    }
}
