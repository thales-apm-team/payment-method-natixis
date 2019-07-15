package com.payline.payment.natixis.bean.business.payment;

/**
 * Additional information that cannot be captured in the structured elements and/or any other
 * specific block.
 */
public class SupplementaryData {

    /**
     * URL to be used by the ASPSP/PISP in order to redirect the client after its consent successful (case of redirection)
     * (non ISO 20022)
     */
    private String successfulReportUrl;
    /**
     * URL to be used by the ASPSP/PISP in order to redirect the client after its consent unsuccessful (case of redirection)
     * (non ISO 20022)
     */
    private String unsuccessfulReportUrl;
    /**
     * Phone number of the client to send the SMS with the URL consentapprouval (E.164 formatted phone number)
     * (non ISO 20022)
     */
    private String endUserConsentMobile;

    private SupplementaryData(SupplementaryDataBuilder builder) {
        this.successfulReportUrl = builder.successfulReportUrl;
        this.unsuccessfulReportUrl = builder.unsuccessfulReportUrl;
        this.endUserConsentMobile = builder.endUserConsentMobile;
    }

    public static final class SupplementaryDataBuilder {
        private String successfulReportUrl;
        private String unsuccessfulReportUrl;
        private String endUserConsentMobile;

        public SupplementaryDataBuilder withSuccessfulReportUrl(String successfulReportUrl) {
            this.successfulReportUrl = successfulReportUrl;
            return this;
        }

        public SupplementaryDataBuilder withUnsuccessfulReportUrl(String unsuccessfulReportUrl) {
            this.unsuccessfulReportUrl = unsuccessfulReportUrl;
            return this;
        }

        public SupplementaryDataBuilder withEndUserConsentMobile(String endUserConsentMobile) {
            this.endUserConsentMobile = endUserConsentMobile;
            return this;
        }

        public SupplementaryData build() {
            return new SupplementaryData(this);
        }
    }
}
