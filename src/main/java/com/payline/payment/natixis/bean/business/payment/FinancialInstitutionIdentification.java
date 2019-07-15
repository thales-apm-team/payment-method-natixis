package com.payline.payment.natixis.bean.business.payment;

/**
 * Unique and unambiguous identification of a financial institution, as assigned under an internationally recognised or
 * proprietary identification scheme.
 */
public class FinancialInstitutionIdentification {

    /**
     * Code allocated to a financial institution by the ISO 9362 Registration Authority as described in ISO 9362
     * "Banking - Banking telecommunication messages - Business identification code (BIC)".
     */
    private String bicFi;

    public FinancialInstitutionIdentification(String bicFi) {
        this.bicFi = bicFi;
    }
}
