package com.govos.cmp.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/**
 * Embedded geographic context for a complaint (CMP-001.6).
 */
@Embeddable
public class ComplaintLocation {

    @Column(name = "state_key", length = 100)
    private String stateKey;

    @Column(name = "district_key", length = 100)
    private String districtKey;

    @Column(name = "ulb_key", length = 100)
    private String ulbKey;

    @Column(name = "ward_key", length = 100)
    private String wardKey;

    @Column(name = "village_key", length = 100)
    private String villageKey;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "landmark", length = 255)
    private String landmark;

    @Column(name = "pincode", length = 20)
    private String pincode;

    @Column(name = "geo_json", columnDefinition = "TEXT")
    private String geoJson;

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getDistrictKey() {
        return districtKey;
    }

    public void setDistrictKey(String districtKey) {
        this.districtKey = districtKey;
    }

    public String getUlbKey() {
        return ulbKey;
    }

    public void setUlbKey(String ulbKey) {
        this.ulbKey = ulbKey;
    }

    public String getWardKey() {
        return wardKey;
    }

    public void setWardKey(String wardKey) {
        this.wardKey = wardKey;
    }

    public String getVillageKey() {
        return villageKey;
    }

    public void setVillageKey(String villageKey) {
        this.villageKey = villageKey;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }
}
