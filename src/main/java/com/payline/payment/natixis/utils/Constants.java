package com.payline.payment.natixis.utils;

/**
 * Support for constants used everywhere in the plugin sources.
 */
public class Constants {

    /**
     * Keys for the entries in ContractConfiguration map.
     */
    public static class ContractConfigurationKeys {

        public static final String CATEGORY_PURPOSE = "categoryPurpose";
        public static final String CHARGE_BEARER = "chargeBearer";
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CREDITOR_IBAN = "creditorIban";
        public static final String CREDITOR_NAME = "creditorName";
        public static final String LOCAL_INSTRUMENT = "localInstrument";
        public static final String PURPOSE = "purpose";
        public static final String SERVICE_LEVEL = "serviceLevel";

    }

    /**
     * Keys for the entries in PartnerConfiguration maps.
     */
    public static class PartnerConfigurationKeys {

        public static final String API_AUTH_BASE_URL = "apiAuthBaseUrl";
        public static final String API_PAYMENT_BASE_URL  = "apiPaymentBaseUrl";
        public static final String CLIENT_CERTIFICATE = "clientCertificate";
        public static final String CLIENT_PRIVATE_KEY = "clientPrivateKey";

    }

}
