package com.payline.payment.natixis.bean.business.payment;

import com.google.gson.annotations.SerializedName;

/**
 * Set of elements used to reference a payment instruction.
 */
public class PaymentIdentification {

    /**
     * Unique identification as assigned by an instructing party for an instructed party to
     * unambiguously identify the instruction.
     */
    @SerializedName("instructionId")
    private String instructionIdentification;

    /**
     * Unique identification assigned by the initiating party to unambiguously identify the
     * transaction. This identification is passed on, unchanged, throughout the entire end-to-end chain.
     */
    @SerializedName("endToEndId")
    private String endToEndIdentification;

    public PaymentIdentification(String instructionIdentification, String endToEndIdentification) {
        this.instructionIdentification = instructionIdentification;
        this.endToEndIdentification = endToEndIdentification;
    }
}
