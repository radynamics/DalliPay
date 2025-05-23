package com.radynamics.dallipay.iso20022;

import org.apache.commons.lang3.StringUtils;

public class Address {
    private String name;
    private String street;
    private String zip;
    private String city;
    private String countryShort;

    public Address(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryShort() {
        return countryShort;
    }

    public void setCountryShort(String countryShort) {
        this.countryShort = countryShort;
    }

    public static String createPartyIdOrEmpty(Address address) {
        return address == null || StringUtils.isEmpty(address.getName()) ? "" : address.getName();
    }

    @Override
    public String toString() {
        return String.format("%s, %s", name, city);
    }
}
