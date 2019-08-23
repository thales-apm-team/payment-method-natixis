package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.bean.business.NatixisPaymentInitResponse;
import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.Payment;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @Mock private NatixisHttpClient natixisHttpClient;

    @BeforeEach
    void setup(){
        service = new PaymentServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @MethodSource("amounts")
    void formatAmount(BigInteger input, String expectedOutput ){
        assertEquals( expectedOutput, service.formatAmount( input ));
    }
    static Stream<Arguments> amounts(){
        return Stream.of(
                Arguments.of( BigInteger.valueOf( 80 ), "0.80" ),
                Arguments.of( BigInteger.valueOf( 100 ), "1.00" ),
                Arguments.of( BigInteger.valueOf( 1599 ), "15.99" )
        );
    }

    @Test
    void paymentRequest_nominal() throws MalformedURLException {
        // given: a valid payment request and every HTTP call returns a success response
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        NatixisPaymentInitResponse successResponse = new NatixisPaymentInitResponse(
                new URL("http://test.domain.com/redirection/endpoint"),
                MockUtils.aPaymentId(),
                "201 Created"
        );
        doReturn( successResponse )
                .when( natixisHttpClient )
                .paymentInit( any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class) );

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest( paymentRequest );

        // then: the payment response is a success
        assertEquals( PaymentResponseRedirect.class, paymentResponse.getClass() );
        assertNotNull( ((PaymentResponseRedirect)paymentResponse).getPartnerTransactionId() );
    }

    @Test
    void paymentRequest_httpError(){
        // given: a payment request that seems valid, but the HTTP call fails
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        doThrow( new PluginException("An error message", FailureCause.COMMUNICATION_ERROR) )
                .when( natixisHttpClient )
                .paymentInit( any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class) );

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest( paymentRequest );

        // then: exception is caught and the payment response is a failure
        assertEquals( PaymentResponseFailure.class, paymentResponse.getClass() );
    }

    @Test
    void paymentRequest_unexpectedError_init(){
        // given: an unexpected error occurs during client initialization
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        doThrow( RuntimeException.class )
                .when( natixisHttpClient )
                .init( any(PartnerConfiguration.class) );

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest( paymentRequest );

        // then: exception is caught and the payment response is a failure
        assertEquals( PaymentResponseFailure.class, paymentResponse.getClass() );
    }

    @Test
    void paymentRequest_unexpectedError_paymentInit(){
        // given: a payment request that seems valid, but the HTTP call fails
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        doThrow( RuntimeException.class )
                .when( natixisHttpClient )
                .paymentInit( any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class) );

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest( paymentRequest );

        // then: exception is caught and the payment response is a failure
        assertEquals( PaymentResponseFailure.class, paymentResponse.getClass() );
    }
}
