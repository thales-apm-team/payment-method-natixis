package com.payline.payment.natixis.integration;

import com.payline.payment.natixis.utils.Constants;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an integration test class to validate the full payment process, via the partner API.
 * It must be run with several system property set on the JVM :
 * - project.clientId: the client ID, provided by the partner and used to retrieve an access token.
 * - project.clientSecret: the client secret, provided by the partner and used to retrieve an access token.
 * - project.certificateChainPath: the path of a local file containing the full certificate chain in PEM format.
 * - project.pkPath: the path of a local file containing the private key in PEM format, non encrypted and exported following PKCS#8 standard
 */
public abstract class NatixisIT extends AbstractPaymentIntegration {

    protected abstract void run();

    protected abstract String getDebtorAgent();

    @Override
    protected ContractConfiguration generateContractConfiguration() {
        Map<String, ContractProperty> contractProperties = new HashMap<>();

        contractProperties.put(Constants.ContractConfigurationKeys.CLIENT_ID, new ContractProperty( System.getProperty("project.clientId") ));
        contractProperties.put(Constants.ContractConfigurationKeys.CLIENT_SECRET, new ContractProperty( System.getProperty("project.clientSecret") ));

        contractProperties.put(Constants.ContractConfigurationKeys.CREDITOR_NAME, new ContractProperty( "TEST IP 13135" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CREDITOR_BIC, new ContractProperty( "CEPAFRPP313" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CREDITOR_IBAN, new ContractProperty( "FR7613135000800400000840090" ));

        contractProperties.put(Constants.ContractConfigurationKeys.CATEGORY_PURPOSE, new ContractProperty( "DVPM" ));
        contractProperties.put(Constants.ContractConfigurationKeys.CHARGE_BEARER, new ContractProperty( "SLEV" ));
        contractProperties.put(Constants.ContractConfigurationKeys.LOCAL_INSTRUMENT, new ContractProperty( "INST" ));
        contractProperties.put(Constants.ContractConfigurationKeys.PURPOSE, new ContractProperty( "COMC" ));
        contractProperties.put(Constants.ContractConfigurationKeys.SERVICE_LEVEL, new ContractProperty( "SEPA" ));

        return new ContractConfiguration("Natixis", contractProperties);
    }

    @Override
    protected PartnerConfiguration generatePartnerConfiguration(){
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_AUTH_BASE_URL, "https://np-auth.api.qua.natixis.com/api");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https://np.api.qua.natixis.com/hub-pisp/v1");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.SIGNATURE_KEYID, "sign-qua-monext");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        try {
            sensitiveConfigurationMap.put(Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE, new String(Files.readAllBytes(Paths.get(System.getProperty("project.certificateChainPath")))));
            sensitiveConfigurationMap.put(Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY, new String(Files.readAllBytes(Paths.get(System.getProperty("project.pkPath")))));
        }
        catch (IOException e) {
            assert false;
        }

        return new PartnerConfiguration( partnerConfigurationMap, sensitiveConfigurationMap );
    }

    @Override
    protected PaymentFormContext generatePaymentFormContext() {
        Map<String, String> paymentFormParameter = new HashMap<>();
        paymentFormParameter.put( BankTransferForm.BANK_KEY, this.getDebtorAgent() );

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter( paymentFormParameter )
                .withSensitivePaymentFormParameter( new HashMap<>() )
                .build();
    }

    @Override
    protected String cancelOnPartnerWebsite(String url) {
        return null;
    }
}
