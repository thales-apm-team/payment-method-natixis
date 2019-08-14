package com.payline.payment.natixis.bean.business.payment;

public class Beneficiary {

    /**
     * Id of the beneficiary.
     */
    private String id;
    /**
     * Financial institution servicing an account for the creditor.
     */
    private FinancialInstitutionIdentification creditorAgent;
    /**
     * Party to which an amount of money is due.
     */
    private PartyIdentification creditor;
    /**
     * Unambiguous identification of the account of the creditor to which a credit entry will be
     * posted as a result of the payment transaction.
     */
    private AccountIdentification creditorAccount;

    private Beneficiary(BeneficiaryBuilder builder) {
        this.id = builder.id;
        this.creditorAgent = builder.creditorAgent;
        this.creditor = builder.creditor;
        this.creditorAccount = builder.creditorAccount;
    }

    public String getId() {
        return id;
    }

    public FinancialInstitutionIdentification getCreditorAgent() {
        return creditorAgent;
    }

    public PartyIdentification getCreditor() {
        return creditor;
    }

    public AccountIdentification getCreditorAccount() {
        return creditorAccount;
    }

    public static class BeneficiaryBuilder {

        private String id;
        private FinancialInstitutionIdentification creditorAgent;
        private PartyIdentification creditor;
        private AccountIdentification creditorAccount;

        public BeneficiaryBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public BeneficiaryBuilder withCreditorAgent(FinancialInstitutionIdentification creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public BeneficiaryBuilder withCreditor(PartyIdentification creditor) {
            this.creditor = creditor;
            return this;
        }

        public BeneficiaryBuilder withCreditorAccount(AccountIdentification creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Beneficiary build(){
            return new Beneficiary(this);
        }
    }
}
