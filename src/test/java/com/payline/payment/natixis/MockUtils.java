package com.payline.payment.natixis;

import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.*;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.http.Authorization;
import com.payline.payment.natixis.utils.security.RSAHolder;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.payline.payment.natixis.TestUtils.addTime;

/**
 * Utility class that generates mocks of frequently used objects.
 */
public class MockUtils {

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance.
     */
    public static Map<String, String> anAccountInfo(){
        return anAccountInfo( aContractConfiguration() );
    }

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance,
     * from the given {@link ContractConfiguration}.
     *
     * @param contractConfiguration The model object from which the properties will be copied
     */
    public static Map<String, String> anAccountInfo( ContractConfiguration contractConfiguration ){
        Map<String, String> accountInfo = new HashMap<>();
        for( Map.Entry<String, ContractProperty> entry : contractConfiguration.getContractProperties().entrySet() ){
            accountInfo.put(entry.getKey(), entry.getValue().getValue());
        }
        return accountInfo;
    }

    /**
     * Generate a valid {@link Authorization}.
     */
    public static Authorization anAuthorization(){
        return anAuthorizationBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link Authorization}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static Authorization.AuthorizationBuilder anAuthorizationBuilder(){
        return new Authorization.AuthorizationBuilder()
                .withAccessToken("ABCD1234567890")
                .withTokenType("Bearer")
                .withExpiresAt( addTime(new Date(), Calendar.HOUR, 1) );
    }

    /**
     * Generate a valid {@link Browser}.
     */
    public static Browser aBrowser(){
        return Browser.BrowserBuilder.aBrowser()
                .withLocale( Locale.getDefault() )
                .withIp( "192.168.0.1" )
                .withUserAgent( aUserAgent() )
                .build();
    }

    /**
     * Generate a valid {@link Buyer}.
     */
    public static Buyer aBuyer(){
        return Buyer.BuyerBuilder.aBuyer()
                .withFullName( new Buyer.FullName( "Marie", "Durand", "1" ) )
                .build();
    }

    /**
     * @return A fake client certificate in PEM format.
     */
    public static String aClientCertificatePem(){
        return "-----BEGIN CERTIFICATE-----\n" +
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
                "-----END CERTIFICATE-----";
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
     * Generate a valid {@link ContractParametersCheckRequest}.
     */
    public static ContractParametersCheckRequest aContractParametersCheckRequest(){
        return aContractParametersCheckRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link ContractParametersCheckRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static ContractParametersCheckRequest.CheckRequestBuilder aContractParametersCheckRequestBuilder(){
        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo( anAccountInfo() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withLocale( Locale.getDefault() )
                .withPartnerConfiguration( aPartnerConfiguration() );
    }

    public static CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder aCreditTransferTransactionInformationBuilder( String uid ){
        return new CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder()
                .withInstructedAmount( new Amount.AmountBuilder()
                        .withAmount("150")
                        .withCurrency("EUR")
                        .build()
                )
                .withPaymentIdentification( new PaymentIdentification( uid, uid ))
                .addRemittanceInformation( "REF123456" );
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
     * Generate a valid, but not complete, {@link Order}
     */
    public static Order anOrder(){
        return Order.OrderBuilder.anOrder()
                .withDate( new Date() )
                .withAmount( aPaylineAmount() )
                .withReference( "ORDER-REF-123456" )
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

        String banks = "{\"accountServiceProviders\":[{\"id\":\"CCBPFRPPNAN\",\"bic\":\"CCBPFRPPNAN\",\"bankCode\":\"13807\",\"name\":\"BANQUE POPULAIRE GRAND OUEST\",\"serviceLevel\":\"SEPA\",\"localInstrument\":null,\"maxAmount\":null},{\"id\":\"CMBRFR2BARK\",\"bic\":\"CMBRFR2BARK\",\"bankCode\":\"15589\",\"name\":\"Crédit Mutuel de Bretagne\",\"serviceLevel\":\"SEPA\",\"localInstrument\":\"INST\",\"maxAmount\":15000},{\"id\":\"CEPAFRPP313\",\"bic\":\"CEPAFRPP313\",\"bankCode\":\"13135\",\"name\":\"CAISSE D EPARGNE DE MIDI PYRENEES\",\"serviceLevel\":\"SEPA\",\"localInstrument\":\"INST\",\"maxAmount\":15000},{\"id\":\"BLUXLULLXXX\",\"bic\":\"BLUXLULLXXX\",\"bankCode\":\"008\",\"name\":\"BANQUE DE LUXEMBOURG\",\"serviceLevel\":\"SEPA\",\"localInstrument\":null,\"maxAmount\":null},{\"id\":\"SOGEFRPPXXX\",\"bic\":\"SOGEFRPPXXX\",\"bankCode\":\"30003\",\"name\":\"Société Générale\",\"serviceLevel\":\"SEPA\",\"localInstrument\":null,\"maxAmount\":null},{\"id\":\"GPBAFRPPXXX\",\"bic\":\"GPBAFRPPXXX\",\"bankCode\":\"18370\",\"name\":\"ORANGE BANK\",\"serviceLevel\":null,\"localInstrument\":null,\"maxAmount\":null}]}";
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.BANKS_LIST, banks);

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE, aClientCertificatePem() );
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY, aPrivateKeyPem() );

        return new PartnerConfiguration( partnerConfigurationMap, sensitiveConfigurationMap );
    }

    /**
     * Generate a valid Payline Amount.
     */
    public static com.payline.pmapi.bean.common.Amount aPaylineAmount(){
        return new com.payline.pmapi.bean.common.Amount(BigInteger.valueOf(1000), Currency.getInstance("EUR"));
    }

    /**
     * Generate a valid {@link PaymentRequest}.
     */
    public static PaymentRequest aPaylinePaymentRequest(){
        return aPaylinePaymentRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static PaymentRequest.Builder aPaylinePaymentRequestBuilder(){
        return PaymentRequest.builder()
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withBrowser( aBrowser() )
                .withAmount( aPaylineAmount() )
                .withOrder( anOrder() )
                .withSoftDescriptor( "softDescriptor" )
                .withTransactionId( "123456789012345678901" )
                .withBuyer( aBuyer() )
                .withLocale( Locale.getDefault() )
                .withPaymentFormContext( aPaymentFormContext() )
                .withDifferedActionDate( TestUtils.addTime( new Date(), Calendar.DATE, 5) )
                .withCaptureNow( true );
    }

    /**
     * Generate a valid {@link Payment}
     */
    public static Payment aPayment(){
        return aPaymentBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link Payment}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public static Payment.PaymentBuilder aPaymentBuilder(){
        String uid = aUniqueIdentifier();

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
                        .withLocalInstrument("INST")
                        .withCategoryPurpose("DVPM")
                        .build() )
                .withDebtor( new PartyIdentification.PartyIdentificationBuilder()
                        .withName("Gaby Gallet Fourcade")
                        .withPostalAddress( new PostalAddress.PostalAddressBuilder()
                                .withCountry("FR")
                                .addAddressLine("25 rue de la petite taverne")
                                .addAddressLine("74120 Megeve")
                                .build()
                        )
                        .withPrivateId( new Identification.IdentificationBuilder()
                                .withIdentification("123456753")
                                .withSchemeName("BANK")
                                .withIssuer("SOGEFRPPXXX")
                                .build()
                        )
                        .build() )
                .withDebtorAgent( new FinancialInstitutionIdentification( "SOGEFRPPXXX" ) )
                .withBeneficiary( new Beneficiary.BeneficiaryBuilder()
                        .withCreditor( new PartyIdentification.PartyIdentificationBuilder()
                                .withName("TEST IP 13135")
                                .withPostalAddress( new PostalAddress.PostalAddressBuilder()
                                        .withCountry("FR")
                                        .addAddressLine("8 rue pavee d'andouilles")
                                        .addAddressLine("71460 Saint-Gengoux-le-national")
                                        .build()
                                )
                                .build()
                        )
                        .withCreditorAccount( new AccountIdentification.AccountIdentificationBuilder()
                                .withIban("FR7613135000800400000840090")
                                .build()
                        )
                        .withCreditorAgent( new FinancialInstitutionIdentification("CEPAFRPP313"))
                        .build()
                )
                .withPurpose("COMC")
                .withChargeBearer("SLEV")
                .withRequestedExecutionDate( addTime( new Date(), Calendar.DATE, 1 ) )
                .addCreditTransferTransactionInformation( aCreditTransferTransactionInformationBuilder( uid ).build() )
                .withSupplementaryData( new SupplementaryData.SupplementaryDataBuilder()
                        .withSuccessfulReportUrl("https://www.successful.fr")
                        .withUnsuccessfulReportUrl("https://www.unsuccessful.fr")
                        .build()
                );
    }

    /**
     * @return a valid payment ID
     */
    public static String aPaymentId(){
        return "0000000634-156620939900013135879318";
    }

    /**
     * Generate a valid {@link PaymentFormContext}.
     */
    public static PaymentFormContext aPaymentFormContext(){
        Map<String, String> paymentFormParameter = new HashMap<>();
        paymentFormParameter.put( Constants.PaymentFormContextKeys.DEBTOR_BIC, "CMBRFR2BARK" );

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter( paymentFormParameter )
                .withSensitivePaymentFormParameter( new HashMap<>() )
                .build();
    }

    /**
     * Generate a valid {@link PaymentFormLogoRequest}.
     */
    public static PaymentFormLogoRequest aPaymentFormLogoRequest(){
        return PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withLocale( Locale.getDefault() )
                .build();
    }

    /**
     * @return A fake private key, for test purpose.
     */
    public static PrivateKey aPrivateKey(){
        try {
            return (PrivateKey) anRsaHolder().getPrivateKey();
        } catch (Exception e) {
            // this is testing context: ignore the exception
            return null;
        }
    }

    /**
     * @return A fake private key in PEM format.
     */
    public static String aPrivateKeyPem(){
        return "-----BEGIN PRIVATE KEY-----\n" +
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
                "-----END PRIVATE KEY-----";
    }

    public static PsuInformation aPsuInformation(){
        return new PsuInformation.PsuInformationBuilder()
                .withLastLogin( new Date() )
                .withIpAddress( "192.168.0.0" )
                .withIpPort( 443 )
                .withHttpMethod( "GET" )
                .withHeaderUserAgent( aUserAgent() )
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
     * @return A public key, for test purpose.
     */
    public static PublicKey aPublicKey(){
        String pemKey = aPublicKeyPem().replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("[\\r\\n]+", "");
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode( pemKey ));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic( spec );
        }
        catch( Exception e ){
            // This would happen in a testing context: spare the exception throwing.
            // The test case using this will probably fail anyway.
            return null;
        }
    }

    /**
     * @return a public key in PEM format.
     */
    public static String aPublicKeyPem(){
        return "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuVeseC44a64lBI9e9Wz4\n" +
                "wHd/Bf1gv0HcPRb6YqqaCdafOap3KhZg3eZ2Gllta5xo6C7tGqcw9Dy7wCvHGwDA\n" +
                "5/jygNL45Gm53UK1KdLwzcmL07goOAyxXTv132T9iLE0gAoHLTaf/xvyCh+Z1hPc\n" +
                "NOIPXeKDk9JJQeBQvF0He3HlkDmmg2xqnrc1r9Z3rlar/vrZ7DbUtL2HSCIJIfmn\n" +
                "C8VU8pYkjkDXjqwiOGRrfUQpSYHJmoeSWdVW2kI4rACL/Gv/z4YGzlCyk+wfDpek\n" +
                "dQ0B4fDBk/tQI7uilU0F62tXFRCyLtubARpyHqAGdWBdE9HmW4ly3EL7NZVG29RZ\n" +
                "XwIDAQAB\n" +
                "-----END PUBLIC KEY-----";
    }

    /**
     * Generate an {@link RSAHolder} instance, containing fake elements, for test purpose.
     */
    public static RSAHolder anRsaHolder(){
        try {
            return new RSAHolder.RSAHolderBuilder()
                    .parseChain( aClientCertificatePem() )
                    .parsePrivateKey( aPrivateKeyPem() )
                    .build();
        } catch (Exception e) {
            // this is testing context: ignore the exception. The test case using this will probably fail anyway.
            return null;
        }
    }

    /**
     * Generate a valid {@link RedirectionPaymentRequest}.
     */
    public static RedirectionPaymentRequest aRedirectionPaymentRequest(){
        return RedirectionPaymentRequest.builder()
                .withAmount( aPaylineAmount() )
                .withBrowser( aBrowser() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withTransactionId( aTransactionId() )
                .build();
    }

    /**
     * Generate a valid {@link RequestConfiguration}.
     */
    public static RequestConfiguration aRequestConfiguration(){
        return new RequestConfiguration( aContractConfiguration(), anEnvironment(), aPartnerConfiguration() );
    }

    /**
     * Generate a sample {@link Signature}.
     */
    public static Signature aSignature(){
        Signature signature = new Signature("a-key-id", Algorithm.RSA_SHA256, null, "(request-target)" );
        Signer signer = new Signer( aPrivateKey(), signature );
        try {
            signature = signer.sign( "POST", "/some/path", new HashMap<>() );
        } catch (Exception e) {
            // This would happen in a testing context: spare the exception throwing. The test case will probably fail anyway.
            return null;
        }

        return signature;
    }

    /**
     * @return a valid transaction ID.
     */
    public static String aTransactionId(){
        return "123456789012345678901";
    }

    /**
     * Generate a valid {@link TransactionStatusRequest}.
     */
    public static TransactionStatusRequest aTransactionStatusRequest(){
        return TransactionStatusRequest.TransactionStatusRequestBuilder.aNotificationRequest()
                .withAmount( aPaylineAmount() )
                .withBuyer( aBuyer() )
                .withContractConfiguration( aContractConfiguration() )
                .withEnvironment( anEnvironment() )
                .withOrder( anOrder() )
                .withPartnerConfiguration( aPartnerConfiguration() )
                .withTransactionId( aTransactionId() )
                .build();
    }

    /**
     * Generate a unique identifier that matches the API expectations.
     */
    public static String aUniqueIdentifier(){
        return "MONEXT" +  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /**
     * @return a valid user agent.
     */
    public static String aUserAgent(){
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";
    }
}
