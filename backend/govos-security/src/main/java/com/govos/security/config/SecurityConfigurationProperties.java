package com.govos.security.config;

import java.time.Duration;

/**
 * Nested configuration property groups for {@link SecurityProperties}.
 */
public class SecurityConfigurationProperties {

    public static class Jwt {

        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(7);
        private int permissionEmbedThreshold = 50;

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }

        public int getPermissionEmbedThreshold() {
            return permissionEmbedThreshold;
        }

        public void setPermissionEmbedThreshold(int permissionEmbedThreshold) {
            this.permissionEmbedThreshold = permissionEmbedThreshold;
        }
    }

    public static class Password {

        private int bcryptStrength = 12;
        private Duration maxAge = Duration.ofDays(90);

        public int getBcryptStrength() {
            return bcryptStrength;
        }

        public void setBcryptStrength(int bcryptStrength) {
            this.bcryptStrength = bcryptStrength;
        }

        public Duration getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class Lockout {

        private int maxAttempts = 5;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }

    public static class Session {

        private int maxPerUser = 5;

        public int getMaxPerUser() {
            return maxPerUser;
        }

        public void setMaxPerUser(int maxPerUser) {
            this.maxPerUser = maxPerUser;
        }
    }
}
