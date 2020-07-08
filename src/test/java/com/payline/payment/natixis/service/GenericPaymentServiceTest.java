package com.payline.payment.natixis.service;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.TestUtils;
import com.payline.payment.natixis.bean.GenericPaymentRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenericPaymentServiceTest {
    private static final String BIC = "aBic";

    @InjectMocks
    GenericPaymentService service;

    @Mock
    private NatixisHttpClient natixisHttpClient;

    @BeforeEach
    void setup() {
        service = GenericPaymentService.getInstance();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequest_nominal() throws MalformedURLException {
        // given: a valid payment request and every HTTP call returns a success response
        String url = "http://test.domain.com/redirection/endpoint";
        String statusCode = "201 Created";
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);
        NatixisPaymentInitResponse successResponse = new NatixisPaymentInitResponse(
                new URL(url),
                MockUtils.aPaymentId(),
                statusCode
        );
        doReturn(successResponse)
                .when(natixisHttpClient)
                .paymentInit(any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class));

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest(genericPaymentRequest, BIC);

        // then: the payment response is a success
        assertEquals(PaymentResponseRedirect.class, paymentResponse.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) paymentResponse;
        TestUtils.checkPaymentResponse(responseRedirect);

        assertEquals(statusCode, responseRedirect.getStatusCode());
        assertEquals(url, responseRedirect.getRedirectionRequest().getUrl().toString());
        assertEquals(PaymentResponseRedirect.RedirectionRequest.RequestType.GET, responseRedirect.getRedirectionRequest().getRequestType());
    }

    @Test
    void paymentRequest_httpError() {
        // given: a payment request that seems valid, but the HTTP call fails
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);

        String errorMessage = "An error message";
        FailureCause cause = FailureCause.COMMUNICATION_ERROR;
        doThrow(new PluginException(errorMessage, cause))
                .when(natixisHttpClient)
                .paymentInit(any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class));

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest(genericPaymentRequest, BIC);

        // then: exception is caught and the payment response is a failure
        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        TestUtils.checkPaymentResponse(responseFailure);

        assertEquals(errorMessage, responseFailure.getErrorCode());
        assertEquals(cause, responseFailure.getFailureCause());

        // verify that mocks have been called (to prevent false positive due to a RuntimeException)
        verify(natixisHttpClient, times(1))
                .paymentInit(any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class));
    }

    @Test
    void paymentRequest_unexpectedError_init() {
        // given: an unexpected error occurs during client initialization
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);

        doThrow(RuntimeException.class)
                .when(natixisHttpClient)
                .init(any(PartnerConfiguration.class));

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest(genericPaymentRequest, BIC);

        // then: exception is caught and the payment response is a failure
        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        TestUtils.checkPaymentResponse(responseFailure);

        assertEquals("plugin error: RuntimeException", responseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());

        // verify that mocks have been called (to prevent false positive due to a RuntimeException)
        verify(natixisHttpClient, times(1)).init(any(PartnerConfiguration.class));
    }

    @Test
    void paymentRequest_unexpectedError_paymentInit() {
        // given: a payment request that seems valid, but the HTTP call fails
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);
        doThrow(RuntimeException.class)
                .when(natixisHttpClient)
                .paymentInit(any(Payment.class), any(PsuInformation.class), any(RequestConfiguration.class));

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.paymentRequest(genericPaymentRequest, BIC);

        // then: exception is caught and the payment response is a failure
        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        TestUtils.checkPaymentResponse(responseFailure);

        assertEquals("plugin error: RuntimeException", responseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());

        // verify that mocks have been called (to prevent false positive due to a RuntimeException)
        verify(natixisHttpClient, times(1)).init(any(PartnerConfiguration.class));
    }

    @ParameterizedTest
    @MethodSource("amounts")
    void formatAmount(BigInteger input, String expectedOutput) {
        assertEquals(expectedOutput, service.formatAmount(input));
    }

    static Stream<Arguments> amounts() {
        return Stream.of(
                Arguments.of(BigInteger.valueOf(80), "0.80"),
                Arguments.of(BigInteger.valueOf(100), "1.00"),
                Arguments.of(BigInteger.valueOf(1599), "15.99")
        );
    }
}