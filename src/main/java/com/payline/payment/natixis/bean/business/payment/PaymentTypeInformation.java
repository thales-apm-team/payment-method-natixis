package com.payline.payment.natixis.bean.business.payment;

/**
 * Set of elements used to further specify the type of transaction.
 */
public class PaymentTypeInformation {

    /**
     * Indicator of the urgency or order of importance that the instructing party would like the instructed party to
     * apply to the processing of the instruction.
     */
    private String instructionPriority;
    /**
     * Agreement under which or rules under which the transaction should be processed.
     * Specifies a pre-agreed service or level of service between the parties, as published in an external service level
     * code list.
     */
    private String serviceLevel;
    /**
     * User community specific instrument.
     * Usage: This element is used to specify a local instrument, local clearing option and/or further qualify the
     * service or service level.
     */
    private String localInstrument;
    /**
     * Specifies the high level purpose of the instruction based on a set of pre-defined categories.
     * This is used by the initiating party to provide information concerning the processing of the payment.
     * It is likely to trigger special processing by any of the agents involved in the payment chain.
     */
    private String categoryPurpose;

    private PaymentTypeInformation(PaymentTypeInformationBuilder builder) {
        this.instructionPriority = builder.instructionPriority;
        this.serviceLevel = builder.serviceLevel;
        this.localInstrument = builder.localInstrument;
        this.categoryPurpose = builder.categoryPurpose;
    }

    public String getInstructionPriority() {
        return instructionPriority;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public String getLocalInstrument() {
        return localInstrument;
    }

    public String getCategoryPurpose() {
        return categoryPurpose;
    }

    public static final class PaymentTypeInformationBuilder {
        private String instructionPriority;
        private String serviceLevel;
        private String localInstrument;
        private String categoryPurpose;

        public PaymentTypeInformationBuilder withInstructionPriority(String instructionPriority) {
            this.instructionPriority = instructionPriority;
            return this;
        }

        public PaymentTypeInformationBuilder withServiceLevel(String serviceLevel) {
            this.serviceLevel = serviceLevel;
            return this;
        }

        public PaymentTypeInformationBuilder withLocalInstrument(String localInstrument) {
            this.localInstrument = localInstrument;
            return this;
        }

        public PaymentTypeInformationBuilder withCategoryPurpose(String categoryPurpose) {
            this.categoryPurpose = categoryPurpose;
            return this;
        }

        public PaymentTypeInformation build() {
            return new PaymentTypeInformation(this);
        }
    }
}
