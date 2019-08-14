package com.payline.payment.natixis.bean.business.payment;

/**
 * Description of a Party which can be either a person or an organization.
 */
public class PartyIdentification {

    /**
     * Name by which a party is known and which is usually used to identify that party.
     */
    private String name;
    /**
     * Information that locates and identifies a specific address, as defined by postal services.
     */
    private PostalAddress postalAddress;
    /**
     * Unique identification of an account, a person or an organisation, as assigned by an issuer.
     */
    private Identification organisationId;
    /**
     * Unique identification of an account, a person or an organisation, as assigned by an issuer.
     */
    private Identification privateId;

    private PartyIdentification(PartyIdentificationBuilder builder) {
        this.name = builder.name;
        this.postalAddress = builder.postalAddress;
        this.organisationId = builder.organisationId;
        this.privateId = builder.privateId;
    }

    public String getName() {
        return name;
    }

    public PostalAddress getPostalAddress() {
        return postalAddress;
    }

    public Identification getOrganisationId() {
        return organisationId;
    }

    public Identification getPrivateId() {
        return privateId;
    }

    public static final class PartyIdentificationBuilder {
        private String name;
        private PostalAddress postalAddress;
        private Identification organisationId;
        private Identification privateId;

        public PartyIdentificationBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public PartyIdentificationBuilder withPostalAddress(PostalAddress postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public PartyIdentificationBuilder withOrganisationId(Identification organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public PartyIdentificationBuilder withPrivateId(Identification privateId) {
            this.privateId = privateId;
            return this;
        }

        public PartyIdentification build() {
            return new PartyIdentification(this);
        }
    }
}
