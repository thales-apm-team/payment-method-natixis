package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.business.NatixisPaymentInitResponse;
import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.*;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.payment.natixis.utils.properties.ConfigProperties;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.natixis.utils.Constants.PaymentFormContextKeys.DEBTOR_BIC;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

    private ConfigProperties config = ConfigProperties.getInstance();
    private NatixisHttpClient natixisHttpClient = NatixisHttpClient.getInstance();

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse;

        try {
            RequestConfiguration requestConfiguration = RequestConfiguration.build( paymentRequest );

            // Init HTTP client
            natixisHttpClient.init( paymentRequest.getPartnerConfiguration() );

            // Retrieve contract info
            ContractConfiguration contract = paymentRequest.getContractConfiguration();

            // Perform controls on the input data
            if( paymentRequest.getAmount() == null ){
                throw new InvalidDataException("paymentRequest.amount is required");
            } else if( paymentRequest.getAmount().getCurrency() == null ){
                throw new InvalidDataException("paymentRequest.amount.currency is required");
            }
            String debtorName = null;
            if( paymentRequest.getBuyer() != null && paymentRequest.getBuyer().getFullName() != null ){
                debtorName = paymentRequest.getBuyer().getFullName().toString();
            }
            String reference = paymentRequest.getOrder() != null ? paymentRequest.getOrder().getReference() : null;
            String debtorBic;
            if( paymentRequest.getPaymentFormContext() == null
                    || paymentRequest.getPaymentFormContext().getPaymentFormParameter() == null
                    || paymentRequest.getPaymentFormContext().getPaymentFormParameter().get( DEBTOR_BIC ) == null ){
                throw new InvalidDataException("debtor BIC is required in payment form context");
            }
            debtorBic = paymentRequest.getPaymentFormContext().getPaymentFormParameter().get( DEBTOR_BIC );

            // Build Payment from PaymentRequest
            Payment payment = new Payment.PaymentBuilder()
                    .withPaymentInformationIdentification( paymentRequest.getTransactionId() )
                    .withCreationDateTime( new Date() )
                    .withNumberOfTransactions( 1 )
                    .withInitiatingParty( new PartyIdentification.PartyIdentificationBuilder()
                            .withName( config.get("payment.initiatingParty.name") )
                            .build() )
                    .withPaymentTypeInformation( new PaymentTypeInformation.PaymentTypeInformationBuilder()
                            .withServiceLevel( contract.getProperty(Constants.ContractConfigurationKeys.SERVICE_LEVEL ).getValue() )
                            .withCategoryPurpose( contract.getProperty(Constants.ContractConfigurationKeys.CATEGORY_PURPOSE ).getValue() )
                            .withLocalInstrument( contract.getProperty(Constants.ContractConfigurationKeys.LOCAL_INSTRUMENT ).getValue() )
                            .build() )
                    .withDebtor( new PartyIdentification.PartyIdentificationBuilder()
                            .withName( debtorName )
                            .build() )
                    .withDebtorAgent( new FinancialInstitutionIdentification( debtorBic ) )
                    .withBeneficiary( new Beneficiary.BeneficiaryBuilder()
                            .withCreditor( new PartyIdentification.PartyIdentificationBuilder()
                                    .withName( contract.getProperty(Constants.ContractConfigurationKeys.CREDITOR_NAME ).getValue() )
                                    .build()
                            )
                            .withCreditorAccount( new AccountIdentification.AccountIdentificationBuilder()
                                    .withIban( contract.getProperty(Constants.ContractConfigurationKeys.CREDITOR_IBAN ).getValue() )
                                    .build()
                            )
                            .build()
                    )
                    .withPurpose( contract.getProperty(Constants.ContractConfigurationKeys.PURPOSE ).getValue() )
                    .withChargeBearer( contract.getProperty(Constants.ContractConfigurationKeys.CHARGE_BEARER ).getValue() )
                    .withRequestedExecutionDate( paymentRequest.getDifferedActionDate() )
                    .addCreditTransferTransactionInformation( new CreditTransferTransactionInformation.CreditTransferTransactionInformationBuilder()
                            .withInstructedAmount( new Amount.AmountBuilder()
                                    .withAmount( this.formatAmount( paymentRequest.getAmount().getAmountInSmallestUnit() ) )
                                    .withCurrency( paymentRequest.getAmount().getCurrency().getCurrencyCode() )
                                    .build()
                            )
                            .withPaymentIdentification( new PaymentIdentification( paymentRequest.getTransactionId(), paymentRequest.getTransactionId() ))
                            .addRemittanceInformation( reference )
                            .build()
                    )
                    .withSupplementaryData( new SupplementaryData.SupplementaryDataBuilder()
                            .withSuccessfulReportUrl( paymentRequest.getEnvironment().getRedirectionReturnURL() )
                            .withUnsuccessfulReportUrl( paymentRequest.getEnvironment().getRedirectionCancelURL() )
                            .build()
                    )
                    .build();

            // Build PSUInformation from PaymentRequest
            PsuInformation psuInformation = new PsuInformation.PsuInformationBuilder()
                    .withIpAddress( paymentRequest.getBrowser().getIp() )
                    .withHeaderUserAgent( paymentRequest.getBrowser().getUserAgent() )
                    .build();

            // Initiate the payment
            NatixisPaymentInitResponse response = natixisHttpClient.paymentInit( payment, psuInformation, requestConfiguration );

            // URL
            PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder redirectionRequestBuilder = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder.aRedirectionRequest()
                    .withUrl( response.getContentApprovalUrl() );

            // request context
            Map<String, String> requestData = new HashMap<>();
            requestData.put("paymentId", response.getPaymentId());
            RequestContext requestContext = RequestContext.RequestContextBuilder.aRequestContext()
                    .withRequestData( requestData )
                    .withSensitiveRequestData( new HashMap<>() )
                    .build();

            // Build PaymentResponse
            paymentResponse = PaymentResponseRedirect.PaymentResponseRedirectBuilder.aPaymentResponseRedirect()
                    .withPartnerTransactionId( response.getPaymentId() )
                    .withStatusCode( response.getStatusCode() )
                    .withRedirectionRequest( new PaymentResponseRedirect.RedirectionRequest( redirectionRequestBuilder ) )
                    .withRequestContext( requestContext )
                    .build();
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

    /**
     * Format the {@link BigInteger} amount in smallest units to {@link String} amount in euros.
     *
     * @param amountInSmallestUnit The amount in smallest units
     * @return The amount in euros, as a string
     */
    String formatAmount( BigInteger amountInSmallestUnit ){
        NumberFormat nf = NumberFormat.getInstance( Locale.UK );
        nf.setMinimumFractionDigits(2);
        return nf.format( amountInSmallestUnit.floatValue()/100 );
    }
}
