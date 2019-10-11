import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.bean.business.NatixisBanksResponse;
import com.payline.payment.natixis.bean.business.NatixisPaymentInitResponse;
import com.payline.payment.natixis.bean.business.payment.*;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.PluginUtils;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * To run this manual test class, you need to send several system properties to the JVM :
 * project.clientId: The client ID to retrieve access tokens
 * project.clientSecret: The client secret to retrieve access tokens
 * project.certificateChainPath: the path of the local file containing the full certificate chain in PEM format
 * project.pkPath: the path of the local file containing the private key, exported following PKCS#8 standard, not encryped, in PEM format
 *
 * This information being sensitive, it must not appear in the source code !
 */
public class MainHttp {

    private static final Logger LOGGER = LogManager.getLogger(MainHttp.class);
    private static NatixisHttpClient natixisHttpClient = NatixisHttpClient.getInstance();

    public static void main( String[] args ) throws IOException {
        PartnerConfiguration partnerConfiguration = initPartnerConfiguration();
        RequestConfiguration requestConfiguration = new RequestConfiguration(initContractConfiguration(), MockUtils.anEnvironment(), partnerConfiguration);

        try {
            natixisHttpClient.init( partnerConfiguration );

            /*
            LOGGER.info("PAYMENT INIT");
            NatixisPaymentInitResponse paymentInit = natixisHttpClient.paymentInit( initPayment(), MockUtils.aPsuInformation(), requestConfiguration );
            LOGGER.info("PaymentId: " + paymentInit.getPaymentId() );
            LOGGER.info("Approval URL: " + paymentInit.getContentApprovalUrl());
            //*/

            /*
            LOGGER.info("PAYMENT STATUS");
            String paymentId = "fb1f14f9-7108-4588-913b-1ec3fac5bf8a";
            //String paymentId = paymentInit.getPaymentId();
            Payment payment = natixisHttpClient.paymentStatus(paymentId, requestConfiguration);
            LOGGER.info("Status: " + payment.getCreditTransferTransactionInformation().get(0).getTransactionStatus());
            //*/

            //*
            LOGGER.info("BANKS");
            NatixisBanksResponse banks = natixisHttpClient.banks( requestConfiguration );

            LOGGER.info("END");
        } catch( PluginException e ){
            LOGGER.error("PluginException: errorCode={}, failureCause={}", e.getErrorCode(), e.getFailureCause().toString());
            e.printStackTrace();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    private static ContractConfiguration initContractConfiguration(){
        ContractConfiguration contractConfiguration = new ContractConfiguration("Natixis", new HashMap<>());

        contractConfiguration.getContractProperties().put(Constants.ContractConfigurationKeys.CLIENT_ID, new ContractProperty( System.getProperty("project.clientId") ));
        contractConfiguration.getContractProperties().put(Constants.ContractConfigurationKeys.CLIENT_SECRET, new ContractProperty( System.getProperty("project.clientSecret") ));

        return contractConfiguration;
    }

    private static PartnerConfiguration initPartnerConfiguration() throws IOException {
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_AUTH_BASE_URL, "https://np-auth.api.qua.natixis.com/api");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https://np.api.qua.natixis.com/hub-pisp/v1");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.SIGNATURE_KEYID, "sign-qua-monext");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE, new String(Files.readAllBytes(Paths.get(System.getProperty("project.certificateChainPath")))) );
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY, new String(Files.readAllBytes(Paths.get(System.getProperty("project.pkPath")))) );

        return new PartnerConfiguration( partnerConfigurationMap, sensitiveConfigurationMap );
    }

    private static Payment initPayment(){
        //Payment payment = MockUtils.aPaymentBuilder().build();
        String uid = MockUtils.aUniqueIdentifier();
        Payment payment = new Payment.PaymentBuilder()
                .withPaymentInformationIdentification( uid )
                .withCreationDateTime( new Date() )
                .withNumberOfTransactions( 1 )
                .withInitiatingParty( new PartyIdentification.PartyIdentificationBuilder()
                        .withName("NATIXIS PAYMENT SOLUTIONS")
                        .build() )
                .withPaymentTypeInformation( new PaymentTypeInformation.PaymentTypeInformationBuilder()
                        .withServiceLevel("SEPA")
                        .withLocalInstrument("INST")
                        .withCategoryPurpose("DVPM")
                        .build() )
                .withDebtor( new PartyIdentification.PartyIdentificationBuilder()
                        .withName("Gaby Gallet Fourcade")
                        .build() )
                .withDebtorAgent( new FinancialInstitutionIdentification( "SOGEFRPPXXX" ) )
                .withBeneficiary( new Beneficiary.BeneficiaryBuilder()
                        .withCreditor( new PartyIdentification.PartyIdentificationBuilder()
                                .withName("TEST IP 13135")
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
                .withRequestedExecutionDate( PluginUtils.addTime( new Date(), Calendar.DATE, 1 ) )
                //.withRequestedExecutionDate( new Date() )
                .addCreditTransferTransactionInformation(
                        new CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder()
                                .withInstructedAmount( new Amount.AmountBuilder()
                                        .withAmount("1500")
                                        .withCurrency("EUR")
                                        .build()
                                )
                                .withPaymentIdentification( new PaymentIdentification( uid, uid ))
                                .addRemittanceInformation( "REF123456" )
                                .build()
                )
                .withSupplementaryData( new SupplementaryData.SupplementaryDataBuilder()
                        .withSuccessfulReportUrl("https://www.successful.fr")
                        .withUnsuccessfulReportUrl("https://www.unsuccessful.fr")
                        .build()
                )
                .build();
        return payment;
    }

    private static Date add( Date to, int days ){
        Calendar cal = Calendar.getInstance();
        cal.setTime( to );
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
}
