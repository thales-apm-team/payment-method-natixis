package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.service.GenericPaymentService;
import com.payline.payment.natixis.utils.security.RSAUtils;
import com.payline.pmapi.bean.payment.Wallet;
import com.payline.pmapi.bean.payment.request.WalletPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

class PaymentWalletServiceImplTest {

    @InjectMocks
    PaymentWalletServiceImpl service;

    @Mock
    GenericPaymentService genericPaymentService;

    @Mock
    RSAUtils rsaUtils;

    @BeforeEach
    void setUp() {
        service = new PaymentWalletServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void walletPaymentRequest() throws Exception{
        // given: a valid walletPaymentRequest
        Wallet wallet = Wallet.builder()
                .pluginPaymentData("thisIsWalletEncryptedData")
                .build();

        WalletPaymentRequest paymentRequest = WalletPaymentRequest.builder()
                .wallet(wallet)
                .pluginConfiguration(MockUtils.aPluginConfiguration())
                .build();

        PaymentResponseRedirect.RedirectionRequest redirectionRequest = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder
                .aRedirectionRequest()
                .withRequestType(PaymentResponseRedirect.RedirectionRequest.RequestType.GET)
                .withUrl(new URL("http://www.foo.com"))
                .build();

        PaymentResponseRedirect responseRedirect = PaymentResponseRedirect.PaymentResponseRedirectBuilder
                .aPaymentResponseRedirect()
                .withPartnerTransactionId("123123")
                .withStatusCode("foo")
                .withRedirectionRequest(redirectionRequest)
                .build();

        doReturn(responseRedirect).when(genericPaymentService).paymentRequest(any(), anyString());

        doReturn("PSSTFRPP").when(rsaUtils).decrypt(any(), any());

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.walletPaymentRequest( paymentRequest );

        // then: the payment response is a success
        assertEquals( PaymentResponseRedirect.class, paymentResponse.getClass() );
    }

    @Test
    void walletPaymentRequestFailure() throws Exception{
        // given: a valid walletPaymentRequest
        Wallet wallet = Wallet.builder()
                .pluginPaymentData("thisIsWalletEncryptedData")
                .build();

        WalletPaymentRequest paymentRequest = WalletPaymentRequest.builder()
                .wallet(wallet)
                .pluginConfiguration(MockUtils.aPluginConfiguration())
                .build();

        PaymentResponseRedirect.RedirectionRequest redirectionRequest = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder
                .aRedirectionRequest()
                .withRequestType(PaymentResponseRedirect.RedirectionRequest.RequestType.GET)
                .withUrl(new URL("http://www.foo.com"))
                .build();

        PaymentResponseRedirect responseRedirect = PaymentResponseRedirect.PaymentResponseRedirectBuilder
                .aPaymentResponseRedirect()
                .withPartnerTransactionId("123123")
                .withStatusCode("foo")
                .withRedirectionRequest(redirectionRequest)
                .build();

        doReturn(responseRedirect).when(genericPaymentService).paymentRequest(any(), anyString());

        doThrow(new PluginException("foo")).when(rsaUtils).decrypt(any(), any());

        // when: calling paymentRequest() method
        PaymentResponse paymentResponse = service.walletPaymentRequest( paymentRequest );

        // then: the payment response is a success
        assertEquals( PaymentResponseFailure.class, paymentResponse.getClass() );
    }
}