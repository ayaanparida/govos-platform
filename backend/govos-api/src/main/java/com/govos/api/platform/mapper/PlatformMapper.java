package com.govos.api.platform.mapper;

import com.govos.api.platform.config.PlatformProperties;
import com.govos.api.platform.response.BuildResponse;
import com.govos.api.platform.response.HealthResponse;
import com.govos.api.platform.response.ModuleResponse;
import com.govos.api.platform.response.PlatformInfoResponse;
import com.govos.api.platform.response.PlatformVersionResponse;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class PlatformMapper {

    private static final String UNKNOWN = "unknown";
    private static final String NOT_CONFIGURED = "NOT_CONFIGURED";

    public PlatformInfoResponse toPlatformInfo(
            Environment environment,
            BuildProperties buildProperties,
            GitProperties gitProperties,
            String databaseProduct,
            String flywayVersion) {
        return new PlatformInfoResponse(
                environment.getProperty("spring.application.name", "govos-api"),
                resolveVersion(environment, buildProperties),
                resolveActiveProfiles(environment),
                System.getProperty("java.version", UNKNOWN),
                org.springframework.boot.SpringBootVersion.getVersion(),
                databaseProduct,
                flywayVersion,
                resolveBuildTime(buildProperties),
                resolveGitCommit(buildProperties, gitProperties));
    }

    public PlatformVersionResponse toPlatformVersion(
            Environment environment,
            BuildProperties buildProperties,
            PlatformProperties platformProperties) {
        return new PlatformVersionResponse(
                resolveVersion(environment, buildProperties),
                platformProperties.getRelease(),
                platformProperties.getReleaseDate());
    }

    public List<ModuleResponse> toModuleResponses(PlatformProperties platformProperties) {
        return platformProperties.getModules().stream()
                .map(module -> new ModuleResponse(module.getName(), module.getVersion(), module.getStatus()))
                .toList();
    }

    public BuildResponse toBuildResponse(BuildProperties buildProperties, GitProperties gitProperties) {
        return new BuildResponse(
                buildProperties.getArtifact(),
                buildProperties.get("number") != null ? buildProperties.get("number") : UNKNOWN,
                resolveGitBranch(gitProperties),
                resolveGitCommit(buildProperties, gitProperties),
                resolveBuildTime(buildProperties));
    }

    public HealthResponse toHealthResponse(HealthComponent healthComponent) {
        return new HealthResponse(
                resolveComponentStatus(healthComponent, "db", "database", "postgresql"),
                resolveOptionalComponentStatus(healthComponent, "redis"),
                resolveOptionalComponentStatus(healthComponent, "minio"),
                resolveOptionalComponentStatus(healthComponent, "opensearch", "elasticsearch"),
                resolveComponentStatus(healthComponent, "diskSpace", "disk"),
                resolveMemoryStatus(),
                formatUptime());
    }

    private String resolveVersion(Environment environment, BuildProperties buildProperties) {
        if (buildProperties.getVersion() != null && !buildProperties.getVersion().isBlank()) {
            return buildProperties.getVersion();
        }
        return environment.getProperty("info.app.version", UNKNOWN);
    }

    private String resolveActiveProfiles(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return environment.getProperty("spring.profiles.default", "default");
        }
        return String.join(",", profiles);
    }

    private String resolveBuildTime(BuildProperties buildProperties) {
        Instant buildTime = buildProperties.getTime();
        return buildTime != null ? buildTime.toString() : UNKNOWN;
    }

    private String resolveGitCommit(BuildProperties buildProperties, GitProperties gitProperties) {
        if (gitProperties != null
                && gitProperties.getCommitId() != null
                && !gitProperties.getCommitId().isBlank()) {
            return abbreviateCommit(gitProperties.getCommitId());
        }
        String buildCommit = buildProperties.get("commit.id.abbrev");
        if (buildCommit != null && !buildCommit.isBlank()) {
            return buildCommit;
        }
        return UNKNOWN;
    }

    private String resolveGitBranch(GitProperties gitProperties) {
        if (gitProperties == null) {
            return UNKNOWN;
        }
        String branch = gitProperties.getBranch();
        return branch != null && !branch.isBlank() ? branch : UNKNOWN;
    }

    private String resolveComponentStatus(HealthComponent root, String... candidateNames) {
        String status = findComponentStatus(root, candidateNames);
        return status != null ? status : UNKNOWN;
    }

    private String resolveOptionalComponentStatus(HealthComponent root, String... candidateNames) {
        String status = findComponentStatus(root, candidateNames);
        return status != null ? status : NOT_CONFIGURED;
    }

    private String findComponentStatus(HealthComponent root, String... candidateNames) {
        if (root instanceof Health health) {
            return health.getStatus().getCode();
        }
        if (!(root instanceof CompositeHealth composite)) {
            return null;
        }

        Map<String, HealthComponent> components = composite.getComponents();
        for (String candidate : candidateNames) {
            HealthComponent component = components.get(candidate);
            if (component != null) {
                return extractStatus(component);
            }
        }
        return null;
    }

    private String extractStatus(HealthComponent component) {
        if (component instanceof Health health) {
            return health.getStatus().getCode();
        }
        if (component instanceof CompositeHealth composite) {
            return composite.getStatus().getCode();
        }
        return Status.UNKNOWN.getCode();
    }

    private String resolveMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        if (max <= 0) {
            return Status.UNKNOWN.getCode();
        }
        long used = runtime.totalMemory() - runtime.freeMemory();
        double usageRatio = (double) used / max;
        if (usageRatio >= 0.95) {
            return Status.DOWN.getCode();
        }
        return Status.UP.getCode();
    }

    private String formatUptime() {
        Duration uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        long hours = uptime.toHours();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    private String abbreviateCommit(String commitId) {
        return commitId.length() > 7 ? commitId.substring(0, 7) : commitId;
    }
}
