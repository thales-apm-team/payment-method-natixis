package com.payline.payment.natixis.bean.business.payment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ISO 20022: The PaymentRequestResource message is sent by the Creditor sending party to the Debtor receiving party,
 * directly or through agents.
 * It is used by a Creditor to request movement of funds from the debtor account to a creditor.
 */
public class Payment {

    /**
     * Identifier assigned by the ASPSP for further use of the created resource through API calls.
     */
    private String resourceId;
    /**
     * Reference assigned by a sending party to unambiguously identify the payment information block within the message.
     */
    @SerializedName("paymentInformationId")
    private String paymentInformationIdentification;
    /**
     * Date and time at which a (group of) payment instruction(s) was created by the instructing party.
     */
    private Date creationDateTime;
    /**
     * Number of individual transactions contained in the message.
     */
    private Integer numberOfTransactions;
    /**
     * Party that initiates the payment.
     */
    private PartyIdentification initiatingParty;
    /**
     * Set of elements used to further specify the type of transaction.
     */
    private PaymentTypeInformation paymentTypeInformation;
    /**
     * Party that owes an amount of money to the (ultimate) creditor.
     */
    private PartyIdentification debtor;
    /**
     * Unambiguous identification of the account of the debtor to which a debit entry will be made
     * as a result of the transaction.
     */
    private AccountIdentification debtorAccount;
    /**
     * Financial institution servicing an account for the debtor.
     */
    private FinancialInstitutionIdentification debtorAgent;
    /**
     * Beneficiary.
     */
    private Beneficiary beneficiary;
    /**
     * Underlying reason for the payment transaction, as published in an external purpose code list.
     */
    private String purpose;
    /**
     * Specifies which party/parties will bear the charges associated with the processing of the
     * payment transaction.
     */
    private String chargeBearer;
    /**
     * Specifies which party/parties will bear the charges associated with the processing of the payment transaction.
     */
    private Date requestedExecutionDate;
    /**
     * Payment processes required to transfer cash from the debtor to the creditor.
     */
    @SerializedName("creditTransferTransaction")
    private List<CreditTransferTransactionInformation> creditTransferTransactionInformation;
    /**
     * Additional information that cannot be captured in the structured elements and/or any other specific block
     */
    private SupplementaryData supplementaryData;

    private Payment( PaymentBuilder builder ){
        this.resourceId = builder.resourceId;
        this.paymentInformationIdentification = builder.paymentInformationIdentification;
        this.creationDateTime = builder.creationDateTime;
        this.numberOfTransactions = builder.numberOfTransactions;
        this.initiatingParty = builder.initiatingParty;
        this.paymentTypeInformation = builder.paymentTypeInformation;
        this.debtor = builder.debtor;
        this.debtorAccount = builder.debtorAccount;
        this.debtorAgent = builder.debtorAgent;
        this.beneficiary = builder.beneficiary;
        this.purpose = builder.purpose;
        this.chargeBearer = builder.chargeBearer;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.creditTransferTransactionInformation = builder.creditTransferTransactionInformation;
        this.supplementaryData = builder.supplementaryData;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPaymentInformationIdentification() {
        return paymentInformationIdentification;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public Integer getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public PartyIdentification getInitiatingParty() {
        return initiatingParty;
    }

    public PaymentTypeInformation getPaymentTypeInformation() {
        return paymentTypeInformation;
    }

    public PartyIdentification getDebtor() {
        return debtor;
    }

    public AccountIdentification getDebtorAccount() {
        return debtorAccount;
    }

    public FinancialInstitutionIdentification getDebtorAgent() {
        return debtorAgent;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getChargeBearer() {
        return chargeBearer;
    }

    public Date getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public List<CreditTransferTransactionInformation> getCreditTransferTransactionInformation() {
        return creditTransferTransactionInformation;
    }

    public SupplementaryData getSupplementaryData() {
        return supplementaryData;
    }

    public static class PaymentBuilder {
        private String resourceId;
        private String paymentInformationIdentification;
        private Date creationDateTime;
        private Integer numberOfTransactions;
        private PartyIdentification initiatingParty;
        private PaymentTypeInformation paymentTypeInformation;
        private PartyIdentification debtor;
        private AccountIdentification debtorAccount;
        private FinancialInstitutionIdentification debtorAgent;
        private Beneficiary beneficiary;
        private String purpose;
        private String chargeBearer;
        private Date requestedExecutionDate;
        private List<CreditTransferTransactionInformation> creditTransferTransactionInformation;
        private SupplementaryData supplementaryData;

        public PaymentBuilder withResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public PaymentBuilder withPaymentInformationIdentification(String paymentInformationIdentification) {
            this.paymentInformationIdentification = paymentInformationIdentification;
            return this;
        }

        public PaymentBuilder withCreationDateTime(Date creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public PaymentBuilder withNumberOfTransactions(Integer numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
            return this;
        }

        public PaymentBuilder withInitiatingParty(PartyIdentification initiatingParty) {
            this.initiatingParty = initiatingParty;
            return this;
        }

        public PaymentBuilder withPaymentTypeInformation(PaymentTypeInformation paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
            return this;
        }

        public PaymentBuilder withDebtor(PartyIdentification debtor) {
            this.debtor = debtor;
            return this;
        }

        public PaymentBuilder withDebtorAccount(AccountIdentification debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public PaymentBuilder withDebtorAgent(FinancialInstitutionIdentification debtorAgent) {
            this.debtorAgent = debtorAgent;
            return this;
        }

        public PaymentBuilder withBeneficiary(Beneficiary beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public PaymentBuilder withPurpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public PaymentBuilder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public PaymentBuilder withRequestedExecutionDate(Date requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public PaymentBuilder addCreditTransferTransactionInformation(CreditTransferTransactionInformation creditTransferTransactionInformation) {
            if( this.creditTransferTransactionInformation == null ){
                this.creditTransferTransactionInformation = new ArrayList<>();
            }
            this.creditTransferTransactionInformation.add( creditTransferTransactionInformation );
            return this;
        }

        public PaymentBuilder withSupplementaryData(SupplementaryData supplementaryData) {
            this.supplementaryData = supplementaryData;
            return this;
        }

        public Payment build(){
            return new Payment( this );
        }
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .disableHtmlEscaping()
                .create();
        return gson.toJson( this );
    }

    public static Payment fromJson(String json ){
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .create();
        return gson.fromJson( json, Payment.class );
    }

}
