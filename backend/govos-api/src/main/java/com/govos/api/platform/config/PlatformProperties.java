package com.govos.api.platform.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "govos.platform")
public class PlatformProperties {

    @NotBlank
    private String release = "0.1.0";

    @NotBlank
    private String releaseDate = "2026-07-17";

    @Valid
    private List<ModuleDefinition> modules = defaultModules();

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<ModuleDefinition> getModules() {
        return modules;
    }

    public void setModules(List<ModuleDefinition> modules) {
        this.modules = modules;
    }

    private static List<ModuleDefinition> defaultModules() {
        List<ModuleDefinition> modules = new ArrayList<>();
        modules.add(new ModuleDefinition("govos-common", "0.1.0-SNAPSHOT", "ACTIVE"));
        modules.add(new ModuleDefinition("govos-shared", "0.1.0-SNAPSHOT", "ACTIVE"));
        modules.add(new ModuleDefinition("govos-domain", "0.1.0-SNAPSHOT", "ACTIVE"));
        modules.add(new ModuleDefinition("govos-infrastructure", "0.1.0-SNAPSHOT", "ACTIVE"));
        modules.add(new ModuleDefinition("govos-security", "0.1.0-SNAPSHOT", "ACTIVE"));
        modules.add(new ModuleDefinition("govos-api", "0.1.0-SNAPSHOT", "ACTIVE"));
        return modules;
    }

    @Validated
    public static class ModuleDefinition {

        @NotBlank
        private String name;

        @NotBlank
        private String version;

        @NotBlank
        private String status;

        public ModuleDefinition() {
        }

        public ModuleDefinition(String name, String version, String status) {
            this.name = name;
            this.version = version;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
