package com.payline.payment.natixis.bean.business.payment;

/**
 * Unique identification of an account, a person or an organisation, as assigned by an issuer.
 */
public class Identification {

    /**
     * Identification assigned by an institution
     */
    private String identification;
    /**
     * Name of the identification scheme.
     */
    private String schemeName;
    /**
     * Entity that assigns the identification.
     */
    private String issuer;

    private Identification(IdentificationBuilder builder) {
        this.identification = builder.identification;
        this.schemeName = builder.schemeName;
        this.issuer = builder.issuer;
    }

    public static final class IdentificationBuilder {
        private String identification;
        private String schemeName;
        private String issuer;

        public IdentificationBuilder withIdentification(String identification) {
            this.identification = identification;
            return this;
        }

        public IdentificationBuilder withSchemeName(String schemeName) {
            this.schemeName = schemeName;
            return this;
        }

        public IdentificationBuilder withIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Identification build() {
            return new Identification(this);
        }
    }
}
