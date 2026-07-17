package com.govos.security.config;

import com.govos.security.constant.SecurityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Platform security configuration bound from {@code application.yml}.
 */
@ConfigurationProperties(prefix = SecurityConstants.CONFIG_PREFIX)
public class SecurityProperties {

    private SecurityConfigurationProperties.Jwt jwt = new SecurityConfigurationProperties.Jwt();
    private SecurityConfigurationProperties.Password password = new SecurityConfigurationProperties.Password();
    private SecurityConfigurationProperties.Lockout lockout = new SecurityConfigurationProperties.Lockout();
    private SecurityConfigurationProperties.Session session = new SecurityConfigurationProperties.Session();

    public SecurityConfigurationProperties.Jwt getJwt() {
        return jwt;
    }

    public void setJwt(SecurityConfigurationProperties.Jwt jwt) {
        this.jwt = jwt;
    }

    public SecurityConfigurationProperties.Password getPassword() {
        return password;
    }

    public void setPassword(SecurityConfigurationProperties.Password password) {
        this.password = password;
    }

    public SecurityConfigurationProperties.Lockout getLockout() {
        return lockout;
    }

    public void setLockout(SecurityConfigurationProperties.Lockout lockout) {
        this.lockout = lockout;
    }

    public SecurityConfigurationProperties.Session getSession() {
        return session;
    }

    public void setSession(SecurityConfigurationProperties.Session session) {
        this.session = session;
    }
}
