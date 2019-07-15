package com.payline.payment.natixis.bean.business.payment;

public class AccountIdentification {

    /**
     * International Bank Account Number (IBAN) - identification used internationally by financial institutions to
     * uniquely identify the account of a customer.
     * Further specifications of the format and content of the IBAN can be found in the standard ISO 13616 "Banking and
     * related financial services - International Bank Account Number (IBAN)" version 1997-10-01, or later revisions.
     */
    private String iban;
    /**
     * Unique identification of an account, a person or an organisation, as assigned by an issuer.
     */
    private Identification other;

    private AccountIdentification(AccountIdentificationBuilder builder) {
        this.iban = builder.iban;
        this.other = builder.other;
    }

    public static class AccountIdentificationBuilder {

        private String iban;
        private Identification other;

        public AccountIdentificationBuilder withIban(String iban) {
            this.iban = iban;
            return this;
        }

        public AccountIdentificationBuilder withOther(Identification other) {
            this.other = other;
            return this;
        }

        public AccountIdentification build(){
            return new AccountIdentification(this);
        }
    }

}
