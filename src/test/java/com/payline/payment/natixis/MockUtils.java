package com.payline.payment.natixis;

import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.*;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.http.Authorization;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.payline.payment.natixis.TestUtils.addTime;

/**
 * Utility class that generates mocks of frequently used objects.
 */
public class MockUtils {


    public static Authorization.AuthorizationBuilder anAuthorizationBuilder(){
        return new Authorization.AuthorizationBuilder()
                .withAccessToken("ABCD1234567890")
                .withTokenType("Bearer")
                .withExpiresAt( addTime(new Date(), Calendar.HOUR, 1) );
    }

    /**
     * Generate a valid {@link ContractConfiguration}.
     */
    public static ContractConfiguration aContractConfiguration(){
        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put(Constants.ContractConfigurationKeys.CATEGORY_PURPOSE, new ContractProperty( "DVPM" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CHARGE_BEARER, new ContractProperty( "SLEV" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CLIENT_ID, new ContractProperty( UUID.randomUUID().toString() ));
        contractProperties.put(Constants.ContractConfigurationKeys.CLIENT_SECRET, new ContractProperty( UUID.randomUUID().toString() ));
        contractProperties.put(Constants.ContractConfigurationKeys.CREDITOR_IBAN, new ContractProperty( "FR7630001007941234567890185" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CREDITOR_NAME, new ContractProperty( "Jean Martin" ));
        contractProperties.put(Constants.ContractConfigurationKeys.LOCAL_INSTRUMENT, new ContractProperty( "INST" ));
        contractProperties.put(Constants.ContractConfigurationKeys.PURPOSE, new ContractProperty( "COMC" ));
        contractProperties.put(Constants.ContractConfigurationKeys.SERVICE_LEVEL, new ContractProperty( "SEPA" ));

        return new ContractConfiguration("Natixis", contractProperties);
    }

    /**
     * Generate a valid {@link Environment}.
     */
    public static Environment anEnvironment(){
        return new Environment("http://notificationURL.com",
                "http://redirectionURL.com",
                "http://redirectionCancelURL.com",
                true);
    }

    /**
     * Generate a valid {@link Payment}
     */
    public static Payment aPayment(){
        SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String uid = "MONEXT" +  timestampDateFormat.format(new Date());

        return new Payment.PaymentBuilder()
                .withPaymentInformationIdentification( uid )
                .withCreationDateTime( new Date() )
                .withNumberOfTransactions( 1 )
                .withInitiatingParty( new PartyIdentification.PartyIdentificationBuilder()
                        .withName("NATIXIS PAYMENT SOLUTIONS")
                        .withPostalAddress( new PostalAddress.PostalAddressBuilder()
                                .addAddressLine("30 AVENUE PIERRE MENDES FRANCE")
                                .addAddressLine("75013 PARIS")
                                .withCountry("FR")
                                .build() )
                        .withOrganisationId( new Identification.IdentificationBuilder()
                                .withIdentification("15930")
                                .withSchemeName("COID")
                                .withIssuer("ACPR")
                                .build() )
                        .build() )
                .withPaymentTypeInformation( new PaymentTypeInformation.PaymentTypeInformationBuilder()
                        .withServiceLevel("SEPA")
                        .withCategoryPurpose("DVPM")
                        .withLocalInstrument("INST")
                        .build() )
                /*
                .withDebtor( new PartyIdentification.PartyIdentificationBuilder()
                        .withName("Jean Dupont")
                        .withPostalAddress( new PostalAddress.PostalAddressBuilder()
                                .withCountry("FR")
                                .addAddressLine("25 rue de la petite taverne")
                                .addAddressLine("74120 Megève")
                                .build()
                        )
                        .withPrivateId( new Identification.IdentificationBuilder()
                                .withIdentification("123456753")
                                .withSchemeName("BANK")
                                .withIssuer("CCBPFRPPNAN")
                                .build()
                        )
                        .build() )
                */
                .withDebtorAgent( new FinancialInstitutionIdentification( "CMBRFR2BARK" ) )
                .withBeneficiary( new Beneficiary.BeneficiaryBuilder()
                        /*
                        .withCreditor( new PartyIdentification.PartyIdentificationBuilder()
                                .withName("Marie Durand")
                                .withPostalAddress( new PostalAddress.PostalAddressBuilder()
                                        .withCountry("FR")
                                        .addAddressLine("8 rue pavée d'andouilles")
                                        .addAddressLine("71460 Saint-Gengoux-le-national")
                                        .build()
                                )
                                .build()
                        )
                        */
                        .withCreditorAccount( new AccountIdentification.AccountIdentificationBuilder()
                                .withIban("FR7613807000343142150215863")
                                .build()
                        )
                        .withCreditorAgent( new FinancialInstitutionIdentification("CCBPFRPPNAN"))
                        .build()
                )
                .withPurpose("COMC")
                .withChargeBearer("SLEV")
                .withRequestedExecutionDate( addTime( new Date(), Calendar.DATE, 1 ) )
                .addCreditTransferTransactionInformation( new CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder()
                        .withInstructedAmount( new Amount.AmountBuilder()
                                .withAmount("150")
                                .withCurrency("EUR")
                                .build()
                        )
                        .withPaymentIdentification( new PaymentIdentification( uid, uid ))
                        .addRemittanceInformation( "Argent de poche" )
                        .build()
                )
                .withSupplementaryData( new SupplementaryData.SupplementaryDataBuilder()
                        .withSuccessfulReportUrl("https://www.successful.fr")
                        .withUnsuccessfulReportUrl("https://www.unsuccessful.fr")
                        .build()
                )
                .build();
    }

    /**
     * Generate a valid {@link PartnerConfiguration}.
     */
    public static PartnerConfiguration aPartnerConfiguration(){
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_AUTH_BASE_URL, "https://np-auth.api.qua.natixis.com/api");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https://np.api.qua.natixis.com/hub-pisp/v1");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.SIGNATURE_KEYID, "signature-key-id");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        // This PEM certificate is fake
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE,
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIDsTCCApmgAwIBAgIEK96RSTANBgkqhkiG9w0BAQsFADCBiDELMAkGA1UEBhMC\n" +
                "RlIxDzANBgNVBAgTBkZyYW5jZTEYMBYGA1UEBxMPQWl4LWVuLVByb3ZlbmNlMRgw\n" +
                "FgYDVQQKEw9UaGFsZXMgU2VydmljZXMxGDAWBgNVBAsTD01vbmV4dCBBUE0gVGVh\n" +
                "bTEaMBgGA1UEAxMRU2ViYXN0aWVuIFBsYW5hcmQwHhcNMTkwODA2MDk0NjU2WhcN\n" +
                "MjAwODA1MDk0NjU2WjCBiDELMAkGA1UEBhMCRlIxDzANBgNVBAgTBkZyYW5jZTEY\n" +
                "MBYGA1UEBxMPQWl4LWVuLVByb3ZlbmNlMRgwFgYDVQQKEw9UaGFsZXMgU2Vydmlj\n" +
                "ZXMxGDAWBgNVBAsTD01vbmV4dCBBUE0gVGVhbTEaMBgGA1UEAxMRU2ViYXN0aWVu\n" +
                "IFBsYW5hcmQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5V6x4Ljhr\n" +
                "riUEj171bPjAd38F/WC/Qdw9FvpiqpoJ1p85qncqFmDd5nYaWW1rnGjoLu0apzD0\n" +
                "PLvAK8cbAMDn+PKA0vjkabndQrUp0vDNyYvTuCg4DLFdO/XfZP2IsTSACgctNp//\n" +
                "G/IKH5nWE9w04g9d4oOT0klB4FC8XQd7ceWQOaaDbGqetzWv1neuVqv++tnsNtS0\n" +
                "vYdIIgkh+acLxVTyliSOQNeOrCI4ZGt9RClJgcmah5JZ1VbaQjisAIv8a//PhgbO\n" +
                "ULKT7B8Ol6R1DQHh8MGT+1Aju6KVTQXra1cVELIu25sBGnIeoAZ1YF0T0eZbiXLc\n" +
                "Qvs1lUbb1FlfAgMBAAGjITAfMB0GA1UdDgQWBBSQ/k9OCF9bw8UiVmjkZSqTiVaG\n" +
                "9zANBgkqhkiG9w0BAQsFAAOCAQEAFdrUHZZksNehc4N2pFrnnnq6KjbVC1BeQaPj\n" +
                "uSOS2r8AyOmBp121s5XUgDw+SN3JqHd9XMJceAvTsrstyL+JFUtibShP1eXNKoEB\n" +
                "bXqMUmP5d1qSa8vmLgb/sYPNKRwT0cxlrMYOpQGtO1FRjIJrthTPJ4B2mExZxZWe\n" +
                "f21DIzhFzqqaR3aullpcQt8i5xFYlhJUtlcAPQPjPCUqQ8GOOGyWnYWwMp62CsZD\n" +
                "tF5HZMno+ctxHXcGjLjFSgr5+/pN5X5aAaI+lVxajwFGGlMUN+9l9wQN/KL6kGq8\n" +
                "EoLe9DHIFvmhXi80iUBauD7NgdoyyjKeT+jogEm4LeJgM3islA==\n" +
                "-----END CERTIFICATE-----" );
        // This PEM private key is fake
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY,
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC5V6x4LjhrriUE\n" +
                "j171bPjAd38F/WC/Qdw9FvpiqpoJ1p85qncqFmDd5nYaWW1rnGjoLu0apzD0PLvA\n" +
                "K8cbAMDn+PKA0vjkabndQrUp0vDNyYvTuCg4DLFdO/XfZP2IsTSACgctNp//G/IK\n" +
                "H5nWE9w04g9d4oOT0klB4FC8XQd7ceWQOaaDbGqetzWv1neuVqv++tnsNtS0vYdI\n" +
                "Igkh+acLxVTyliSOQNeOrCI4ZGt9RClJgcmah5JZ1VbaQjisAIv8a//PhgbOULKT\n" +
                "7B8Ol6R1DQHh8MGT+1Aju6KVTQXra1cVELIu25sBGnIeoAZ1YF0T0eZbiXLcQvs1\n" +
                "lUbb1FlfAgMBAAECggEAQe+p4Nt4oP5EFxo2SxOobzhTzTq193SjuGv7SaytvkBH\n" +
                "RwmY+TUa4vaBH6Ce58CuJaPEIE5IgSs2FAi+/aFH/362LxRfDUt7nvaDYsyzdFJ2\n" +
                "VyzVyhLh6mxRbVcNR1rbdY2bvf8H7obvlaBmZx2LopilpN3Xt3gBLdlyf4CND+H6\n" +
                "YqCMWgIjhLkUk98/p479gSh5qaxkj7lsjlGk+4CLB/MlHuZ7WgWKH+q058l3JeAp\n" +
                "KQBHi/N/HOEdBrg2dilFwoPy0xRwxAjK5TCdn2277DIwr4lA28O4VqlICDXGNgUx\n" +
                "dKWwBnhOpfvlvDQr4Ktt2pyuL7H5KfrkC8lvxOCJGQKBgQDxItb9M2hfrEY5uq8U\n" +
                "4T/aEKV6lrQfeGRoEx2smotzt7hx9J4XUb3RAMnuK108kfWrjhqyOmDgIHwyL1ag\n" +
                "9NsKgVWvNw+ZGLfZHblTvTNPVqPu9xQk6Rrq8kQzFqYelAKhYKrEVJiZ5MK3NCts\n" +
                "a24Qn46mN2mBN/6mD0m9fr3B8wKBgQDExGcvdfUddf9vp640Sq7TxiWOZJXo1Gk8\n" +
                "z+kVa6rIWB87zFq82IojmjofYYmYF+d50kTyw/s5trob8LqWL2GLrDF0K64nbon7\n" +
                "tW1MSggHwQu5BtbxIwkyL8pv6BWnpf0k2Lf29txzBo1hvBZ1M9fPGvpd/eP2Hz4x\n" +
                "yU+G92555QKBgQC4vfp8boBOnEwJOo+crZ4f0ZUWQJOrcK9sVQjtDlI8y8rR85mT\n" +
                "QBrvH22VvT9ngmP3lZ26YqOJ0xmT0VTLaAzRFZmx7btTje58txsfntrKtBRQppeW\n" +
                "V8k5q3a4tWd8EeWaAdeTJ0Tq0qqjdaK3I+9laPj/O4DncSD11MyoE4wKJQKBgFWv\n" +
                "XWvKhyoEJ2786xx0ZTttbw9Z9/oC/azwsQSV9TH3Reqpa94OweENGUBvHhbwWemv\n" +
                "yjyZYX5ZdyQRqX8bNPQ40PRQzS74sPe+otD08BhIVY2GT/WEF04Wh6ZBv6RY4Sq5\n" +
                "gSr3hzpD4S9tU65IHDNhASQLGskkA9Z0XsBcYWyNAoGAO7UpGmNCywW8x/MrSqF+\n" +
                "Nl8EIyL+oPat0awur9FwxL3AyKTL75fykdiOf6Qy96Je4X7WojGmyL7a3Hbh29NT\n" +
                "VNAzHrCYpRtxCNVoatW2lA8AvySWsiEwMTmdNMubjWcSPx8gHVmzGoOnKK44Ytaf\n" +
                "TZVu0T1HwCkWzUMS7ULfwtw=\n" +
                "-----END PRIVATE KEY-----" );

        return new PartnerConfiguration( partnerConfigurationMap, sensitiveConfigurationMap );
    }

    public static PsuInformation aPsuInformation(){
        return new PsuInformation.PsuInformationBuilder()
                .withLastLogin( new Date() )
                .withIpAddress( "192.168.0.0" )
                .withIpPort( 443 )
                .withHttpMethod( "GET" )
                .withHeaderUserAgent( "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0" )
                .withHeaderReferer( "https://test.domain.com" )
                .withHeaderAccept( "application/json" )
                .withHeaderAcceptCharset( "utf-8, iso-8859-1;q=0.5" )
                .withHeaderAcceptEncoding( "gzip, deflate, br" )
                .withHeaderAcceptLanguage( "fr" )
                .withGeoLocation( "GEO:52.506931,13.144558" )
                .withDeviceId( UUID.randomUUID().toString() )
                .build();
    }

    /**
     * Generate a valid {@link RequestConfiguration}.
     */
    public static RequestConfiguration aRequestConfiguration(){
        return new RequestConfiguration( aContractConfiguration(), anEnvironment(), aPartnerConfiguration() );
    }

}
