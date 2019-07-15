package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.*;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

    private NatixisHttpClient natixisHttpClient = NatixisHttpClient.getInstance();

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = null; // TODO: remove useless initialization

        try {
            // Init HTTP client
            natixisHttpClient.init( paymentRequest.getPartnerConfiguration() );

            // Build request configuration
            RequestConfiguration requestConfiguration = RequestConfiguration.build( paymentRequest );

            // Build Payment from PaymentRequest
            // TODO !
            Payment payment = new Payment.PaymentBuilder()
                    .withPaymentInformationIdentification( paymentRequest.getTransactionId() )
                    .withCreationDateTime( new Date() )
                    .withNumberOfTransactions( 1 )
                    .withInitiatingParty( new PartyIdentification.PartyIdentificationBuilder()
                            .withName("NATIXIS PAYMENT SOLUTIONS")
                            .build() )
                    .withPaymentTypeInformation( new PaymentTypeInformation.PaymentTypeInformationBuilder()
                            .withServiceLevel("SEPA")
                            .withCategoryPurpose("DVPM")
                            .withLocalInstrument("INST")
                            .build() )
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
                    .withDebtorAgent( new FinancialInstitutionIdentification( "CMBRFR2BARK" ) )
                    .withBeneficiary( new Beneficiary.BeneficiaryBuilder()
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
                            .withCreditorAccount( new AccountIdentification.AccountIdentificationBuilder()
                                    .withIban("FR7613807000343142150215863")
                                    .build()
                            )
                            .withCreditorAgent( new FinancialInstitutionIdentification("CCBPFRPPNAN"))
                            .build()
                    )
                    .withPurpose("COMC")
                    .withChargeBearer("SLEV")
                    .withRequestedExecutionDate( new Date() )
                    .addCreditTransferTransactionInformation( new CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder()
                            .withInstructedAmount( new Amount.AmountBuilder()
                                    .withAmount("150")
                                    .withCurrency("EUR")
                                    .build()
                            )
                            .withPaymentIdentification( new PaymentIdentification( "TODO", "TODO" ))
                            .addRemittanceInformation( "Argent de poche" )
                            .build()
                    )
                    .withSupplementaryData( new SupplementaryData.SupplementaryDataBuilder()
                            .withSuccessfulReportUrl("https://www.successful.fr")
                            .withUnsuccessfulReportUrl("https://www.unsuccessful.fr")
                            .build()
                    )
                    .build();

            // Build PSUInformation from PaymentRequest
            PsuInformation psuInformation = new PsuInformation.PsuInformationBuilder()
                    .withIpAddress( paymentRequest.getBrowser().getIp() )
                    .withHeaderUserAgent( paymentRequest.getBrowser().getUserAgent() )
                    .build();

            // Initiate the payment
            natixisHttpClient.paymentInit( payment, psuInformation, requestConfiguration );

            // TODO: what next ?
        }
        catch( PluginException e ){
            paymentResponse = e.toPaymentResponseFailureBuilder().build();
        }
        catch( RuntimeException e ){
            LOGGER.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode( PluginException.runtimeErrorCode( e ) )
                    .withFailureCause( FailureCause.INTERNAL_ERROR )
                    .build();
        }

        return paymentResponse;
    }
}
