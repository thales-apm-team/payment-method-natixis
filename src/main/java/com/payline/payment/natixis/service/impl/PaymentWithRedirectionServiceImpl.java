package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.business.payment.Payment;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.natixis.utils.Constants.RequestContextKeys.PAYMENT_ID;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);

    private NatixisHttpClient natixisHttpClient = NatixisHttpClient.getInstance();

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        PaymentResponse paymentResponse;

        try {
            // Build request configuration
            RequestConfiguration requestConfiguration = RequestConfiguration.build( redirectionPaymentRequest );

            // Retrieve payment ID from request context
            if( redirectionPaymentRequest.getRequestContext() == null
                    || redirectionPaymentRequest.getRequestContext().getRequestData() == null
                    || redirectionPaymentRequest.getRequestContext().getRequestData().get(PAYMENT_ID) == null ){
                throw new InvalidDataException("Missing payment ID from request context");
            }
            String paymentId = redirectionPaymentRequest.getRequestContext().getRequestData().get(PAYMENT_ID);

            // Update transaction state
            paymentResponse = this.updateTransactionState( paymentId, requestConfiguration );
        }
        catch( PluginException e ){
            paymentResponse = e.toPaymentResponseFailureBuilder().build();
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        PaymentResponse paymentResponse;

        try {
            // Build request configuration
            RequestConfiguration requestConfiguration = RequestConfiguration.build( transactionStatusRequest );

            // Update transaction state
            // TODO: find a way to recover the payment ID from here !

            paymentResponse = null;
        }
        catch( PluginException e ){
            paymentResponse = e.toPaymentResponseFailureBuilder().build();
        }

        return paymentResponse;
    }

    /**
     * Retrieve the status of the payment request, analyze its status and provide the associated {@link PaymentResponse}.
     *
     * @param paymentId The payment id (partner)
     * @param requestConfiguration the request configuration
     * @return a PaymentResponse
     */
    PaymentResponse updateTransactionState( String paymentId, RequestConfiguration requestConfiguration ){
        PaymentResponse paymentResponse;

        try {
            // Init HTTP client
            natixisHttpClient.init( requestConfiguration.getPartnerConfiguration() );

            // Get payment status
            Payment payment = natixisHttpClient.paymentStatus( paymentId, requestConfiguration );
            if( payment.getCreditTransferTransactionInformation() == null
                    || payment.getCreditTransferTransactionInformation().isEmpty() ){
                throw new PluginException("Missing CreditTransferInformation in response", FailureCause.PARTNER_UNKNOWN_ERROR);
            }
            String transactionStatus = payment.getCreditTransferTransactionInformation().get(0).getTransactionStatus();

            switch( transactionStatus ){
                // Success
                case "ACSP":
                case "ACSC":
                    paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                            .withPartnerTransactionId( paymentId )
                            .withStatusCode( transactionStatus )
                            // TODO...
                            .build();
                    break;
                // Pending
                case "PDNG":
                    paymentResponse = PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                            .withPartnerTransactionId( paymentId )
                            .withOnHoldCause( null ) // TODO
                            .withStatusCode( transactionStatus )
                            .build();
                    break;
                // Rejected
                case "RJCT":
                    String reason = payment.getCreditTransferTransactionInformation().get(0).getStatusReasonInformation();
                    paymentResponse = mapRejectReason( reason )
                            .withPartnerTransactionId( paymentId )
                            .build();
                    break;

                default:
                    throw new PluginException("Unknown transaction status "+transactionStatus, FailureCause.PARTNER_UNKNOWN_ERROR);
            }
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
     * Map the status reason information to an error code and a {@link FailureCause}.
     * Provide these fields to a builder, then return it.
     *
     * @param statusReasonInformation the status reason information
     * @return the builder
     */
    PaymentResponseFailure.PaymentResponseFailureBuilder mapRejectReason( String statusReasonInformation ){
        PaymentResponseFailure.PaymentResponseFailureBuilder builder = PaymentResponseFailure.PaymentResponseFailureBuilder
                .aPaymentResponseFailure();

        switch( statusReasonInformation ){
            case "AC01":
                builder.withErrorCode("the account number is invalid or does not exist")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "AC04":
                builder.withErrorCode("the account is closed and cannot be used")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "AC06":
                builder.withErrorCode("the account is blocked and cannot be used")
                        .withFailureCause( FailureCause.REFUSED );
                break;

            case "AG01":
                builder.withErrorCode("transaction forbidden on this type of account")
                        .withFailureCause( FailureCause.REFUSED );
                break;

            case "AM18":
                builder.withErrorCode("number of transactions exceeds the ASPSP limit")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "CH03":
                builder.withErrorCode("requested execution date is too far in the future")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "CUST":
                builder.withErrorCode("due to the debtor: refusal or lack of liquidity")
                        .withFailureCause( FailureCause.REFUSED );
                break;

            case "DS02":
                builder.withErrorCode("an authorized user has cancelled the order")
                        .withFailureCause( FailureCause.CANCEL );
                break;

            case "FF01":
                builder.withErrorCode("the original payment request is invalid")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "FRAD":
                builder.withErrorCode("the payment request is considered as fraudulent")
                        .withFailureCause( FailureCause.FRAUD_DETECTED );
                break;

            case "MS03":
                builder.withErrorCode("no reason specified by the ASPSP")
                        .withFailureCause( FailureCause.PARTNER_UNKNOWN_ERROR );
                break;

            case "NOAS":
                builder.withErrorCode("PSU has neither accepted nor rejected the payment")
                        .withFailureCause( FailureCause.PARTNER_UNKNOWN_ERROR );
                break;

            case "RR01":
                builder.withErrorCode("debtor account and/or identification incorrect")
                        .withFailureCause( FailureCause.INVALID_DATA );
            break;

            case "RR03":
                builder.withErrorCode("missing creditor name or address")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            case "RR04":
                builder.withErrorCode("reject from regulatory reason")
                        .withFailureCause( FailureCause.REFUSED );
                break;

            case "RR12":
                builder.withErrorCode("invalid or missing identification")
                        .withFailureCause( FailureCause.INVALID_DATA );
                break;

            default:
                builder.withErrorCode("Unknown StatusReasonInformation " + statusReasonInformation )
                        .withFailureCause( FailureCause.PARTNER_UNKNOWN_ERROR );
        }
        return builder;
    }
}
