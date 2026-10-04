package com.irfan.airlinebooking.model;

/**
 * An airport served by the airline.
 * Holds the IATA-like code, city, country and geographic coordinates so the
 * flight-network generator can compute realistic distances and fares.
 */
public class Airport extends BaseEntity {
    private String code;
    private String city;
    private String country;
    private double latitude;
    private double longitude;

    public Airport() {
    }

    public Airport(String code, String city, String country, double latitude, double longitude) {
        this.code = code;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** Nice display label, e.g. "Delhi (DEL)". */
    public String getLabel() {
        return city + " (" + code + ")";
    }

    public boolean isDomesticTo(Airport other) {
        return other != null && country != null && country.equalsIgnoreCase(other.country);
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
