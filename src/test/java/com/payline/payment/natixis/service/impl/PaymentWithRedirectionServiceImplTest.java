package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.TestUtils;
import com.payline.payment.natixis.bean.business.payment.CreditTransferTransactionInformation;
import com.payline.payment.natixis.bean.business.payment.Payment;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class PaymentWithRedirectionServiceImplTest {

    @InjectMocks
    private PaymentWithRedirectionServiceImpl service;

    @Mock
    private NatixisHttpClient natixisHttpClient;

    @BeforeEach
    void setup(){
        service = new PaymentWithRedirectionServiceImpl();
        MockitoAnnotations.initMocks( this );
    }

    @Test
    void updateTransactionState_initException(){
        // given: the initialization of the HTTP client throws a RuntimeException
        doThrow( RuntimeException.class )
                .when( natixisHttpClient )
                .init( any(PartnerConfiguration.class) );

        // when: calling updateTransactionState method
        PaymentResponse response = service.updateTransactionState( MockUtils.aPaymentId(), MockUtils.aRequestConfiguration() );

        // then: the response is a failure
        assertEquals( PaymentResponseFailure.class, response.getClass() );
        TestUtils.checkPaymentResponse( (PaymentResponseFailure) response );

        // verify that mock has been called (to prevent false positive due to a RuntimeException)
        verify( natixisHttpClient, times(1) ).init( any(PartnerConfiguration.class) );
    }

    @Test
    void updateTransactionState_statusException(){
        // given: the paymentStatus method throws a PluginException
        doThrow( new PluginException("An error message", FailureCause.COMMUNICATION_ERROR) )
                .when( natixisHttpClient )
                .paymentStatus( anyString(), any(RequestConfiguration.class));

        // when: calling updateTransactionState method
        PaymentResponse response = service.updateTransactionState( MockUtils.aPaymentId(), MockUtils.aRequestConfiguration() );

        // then: the response is a failure
        assertEquals( PaymentResponseFailure.class, response.getClass() );
        TestUtils.checkPaymentResponse( (PaymentResponseFailure) response );

        // verify that mock has been called (to prevent false positive due to a RuntimeException)
        verify( natixisHttpClient, times(1) ).paymentStatus( anyString(), any(RequestConfiguration.class));
    }

    @Test
    void updateTransactionState_noCreditTransferTransactionInformation(){
        // given: the paymentStatus method returns a payment without CreditTransferTransactionInformation
        Payment paymentStatus = MockUtils.aPaymentBuilder()
                .withCreditTransferTransactionInformation( null )
                .build();
        doReturn( paymentStatus )
                .when( natixisHttpClient )
                .paymentStatus( anyString(), any(RequestConfiguration.class));

        // when: calling updateTransactionState method
        PaymentResponse response = service.updateTransactionState( MockUtils.aPaymentId(), MockUtils.aRequestConfiguration() );

        // then: the response is a failure
        assertEquals( PaymentResponseFailure.class, response.getClass() );
        TestUtils.checkPaymentResponse( (PaymentResponseFailure) response );

        // verify that mock has been called (to prevent false positive due to a RuntimeException)
        verify( natixisHttpClient, times(1) ).paymentStatus( anyString(), any(RequestConfiguration.class));
    }

    @ParameterizedTest
    @MethodSource("statusMappingSet")
    void updateTransactionState_statusMapping( String transactionStatus, Class expectedReturnType ){
        // given: the paymentStatus method returns a payment with the given status and reason
        doReturn( this.mockStatusReturn( transactionStatus ) )
                .when( natixisHttpClient )
                .paymentStatus( anyString(), any(RequestConfiguration.class));

        // when: calling updateTransactionState method
        PaymentResponse response = service.updateTransactionState( MockUtils.aPaymentId(), MockUtils.aRequestConfiguration() );

        // then: the response is of the given type, and complete
        assertEquals( expectedReturnType, response.getClass() );
        if( response instanceof PaymentResponseSuccess ){
            TestUtils.checkPaymentResponse( (PaymentResponseSuccess) response );
        } else if( response instanceof PaymentResponseOnHold ){
            TestUtils.checkPaymentResponse( (PaymentResponseOnHold) response );
        } else {
            TestUtils.checkPaymentResponse( (PaymentResponseFailure) response );

            // verify that mock has been called (to prevent false positive due to a RuntimeException)
            verify( natixisHttpClient, times(1) ).paymentStatus( anyString(), any(RequestConfiguration.class));
        }
    }
    static Stream<Arguments> statusMappingSet(){
        return Stream.of(
                Arguments.of( "ACSC", PaymentResponseSuccess.class ),
                Arguments.of( "ACSP", PaymentResponseOnHold.class ),
                Arguments.of( "PDNG", PaymentResponseOnHold.class ),
                Arguments.of( "RJCT", PaymentResponseFailure.class ),
                Arguments.of( "ACTC", PaymentResponseFailure.class )
        );
    }

    void getOwnerBankAcconut(){
        // TODO
    }

    void getReceiverBankAccount(){
        // TODO
    }


    private Payment mockStatusReturn( String transactionStatus ){
        String uid = MockUtils.aUniqueIdentifier();
        CreditTransferTransactionInformation ctti = MockUtils.aCreditTransferTransactionInformationBuilder( uid )
                .withTransactionStatus( transactionStatus )
                .build();
        return MockUtils.aPaymentBuilder()
                .withCreditTransferTransactionInformation( ctti )
                .withPaymentInformationIdentification( uid )
                .build();
    }

}
