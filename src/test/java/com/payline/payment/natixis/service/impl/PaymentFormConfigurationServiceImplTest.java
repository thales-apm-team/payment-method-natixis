package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;
import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentFormConfigurationServiceImplTest {

    @InjectMocks
    private PaymentFormConfigurationServiceImpl service;

    @Mock private I18nService i18n;

    @BeforeEach
    void setup(){
        service = new PaymentFormConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getPaymentFormConfiguration_i18nException(){
        // given: i18n service throws a MissingResourceException (missing file) when retrieving a message
        doThrow( MissingResourceException.class )
                .when( i18n )
                .getMessage( anyString(), any(Locale.class) );

        // when: calling getPaymentFormConfiguration method
        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration( MockUtils.aPaymentFormConfigurationRequest() );

        // then: response is a failure
        assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        assertNotNull( ((PaymentFormConfigurationResponseFailure)response).getErrorCode() );
        assertNotNull( ((PaymentFormConfigurationResponseFailure)response).getFailureCause() );

        // check the mock has been called at least once (to prevent false positive due to a RuntimeException)
        verify( i18n, atLeastOnce() ).getMessage( anyString(), any(Locale.class) );
    }

    void getPaymentFormConfiguration_nominal(){
        // TODO: when the dev will be finished
    }

}
