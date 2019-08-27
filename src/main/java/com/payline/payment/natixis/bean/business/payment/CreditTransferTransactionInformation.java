package com.payline.payment.natixis.bean.business.payment;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information on the individual transaction(s) included in the message.
 */
public class CreditTransferTransactionInformation {

    /**
     * Set of elements used to reference a payment instruction.
     */
    @SerializedName("paymentId")
    private PaymentIdentification paymentIdentification;
    /**
     * Amount of money to be moved between the debtor and creditor, before deduction of
     * charges, expressed in the currency as ordered by the initiating party.
     */
    private Amount instructedAmount;
    /**
     * Information supplied to enable the matching of an entry with the items that the transfer is
     * intended to settle, such as commercial invoices in an accounts' receivable system.
     */
    private List<String> remittanceInformation;
    /**
     * Specifies the status of the payment information group.
     */
    private String transactionStatus;
    /**
     * Provides detailed information on the status reason.
     */
    private String statusReasonInformation;

    private CreditTransferTransactionInformation(CreditTransferTransactionInformationBuilder builder) {
        this.paymentIdentification = builder.paymentIdentification;
        this.instructedAmount = builder.instructedAmount;
        this.remittanceInformation = builder.remittanceInformation;
        this.transactionStatus = builder.transactionStatus;
        this.statusReasonInformation = builder.statusReasonInformation;
    }

    public PaymentIdentification getPaymentIdentification() {
        return paymentIdentification;
    }

    public Amount getInstructedAmount() {
        return instructedAmount;
    }

    public List<String> getRemittanceInformation() {
        return remittanceInformation;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getStatusReasonInformation() {
        return statusReasonInformation;
    }

    public static class CreditTransferTransactionInformationBuilder {

        private PaymentIdentification paymentIdentification;
        private Amount instructedAmount;
        private List<String> remittanceInformation;
        private String transactionStatus;
        private String statusReasonInformation;

        public CreditTransferTransactionInformationBuilder withPaymentIdentification(PaymentIdentification paymentIdentification) {
            this.paymentIdentification = paymentIdentification;
            return this;
        }

        public CreditTransferTransactionInformationBuilder withInstructedAmount(Amount instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public CreditTransferTransactionInformationBuilder addRemittanceInformation(String remittanceInformation) {
            if( this.remittanceInformation == null ){
                this.remittanceInformation = new ArrayList<>();
            }
            this.remittanceInformation.add(remittanceInformation);
            return this;
        }

        public CreditTransferTransactionInformationBuilder withTransactionStatus(String transactionStatus) {
            this.transactionStatus = transactionStatus;
            return this;
        }

        public CreditTransferTransactionInformationBuilder withStatusReasonInformation(String statusReasonInformation) {
            this.statusReasonInformation = statusReasonInformation;
            return this;
        }

        public CreditTransferTransactionInformation build(){
            return new CreditTransferTransactionInformation(this);
        }
    }
}
