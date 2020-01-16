package com.payline.payment.natixis.bean.business.payment;

import java.util.ArrayList;
import java.util.List;

/**
 * Information that locates and identifies a specific address, as defined by postal services.
 */
public class PostalAddress {

    /**
     * Information that locates and identifies a specific address, as defined by postal services,
     * presented in free format text.
     */
    private List<String> addressLine;

    /**
     * Country in which a person resides (the place of a person's home).
     * In the case of a company, it is the country from which the affairs of that company are directed.
     */
    private String country;

    private PostalAddress(PostalAddressBuilder builder) {
        this.country = builder.country;
        this.addressLine = builder.addressLine;
    }

    public List<String> getAddressLine() {
        return addressLine;
    }

    public String getCountry() {
        return country;
    }

    public static final class PostalAddressBuilder {
        private String country;
        private List<String> addressLine;

        public PostalAddressBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public PostalAddressBuilder addAddressLine(String addressLine) {
            if( this.addressLine == null ){
                this.addressLine = new ArrayList<>();
            }
            this.addressLine.add( addressLine );
            return this;
        }

        public PostalAddress build() {
            return new PostalAddress(this);
        }
    }
}
