package com.govos.audit.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "aud_session", schema = "govos")
public class AuditSession extends AuditableEntity {

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime;

    @Column(name = "logout_time")
    private Instant logoutTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device", length = 255)
    private String device;

    @Column(name = "browser", length = 255)
    private String browser;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Instant loginTime) {
        this.loginTime = loginTime;
    }

    public Instant getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Instant logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }
}
