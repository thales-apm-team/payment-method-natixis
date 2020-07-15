package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.bean.business.NatixisBanksResponse;
import com.payline.payment.natixis.bean.business.bank.Bank;
import com.payline.payment.natixis.utils.PluginUtils;
import com.payline.payment.natixis.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.bean.form.AbstractPaymentForm;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.*;
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
        checkPaymentResponse( (PaymentFormConfigurationResponseFailure)response );

        // check the mock has been called at least once (to prevent false positive due to a RuntimeException)
        verify( i18n, atLeastOnce() ).getMessage( anyString(), any(Locale.class) );
    }

    @Test
    void getPaymentFormConfiguration_noPluginConfiguration(){
        // given: the plugin configuration is invalid
        PaymentFormConfigurationRequest request = MockUtils.aPaymentFormConfigurationRequestBuilder()
                .withPluginConfiguration( null )
                .build();

        // when: calling getPaymentFormConfiguration method
        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration( request );

        // then: response is a failure
        assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        checkPaymentResponse( (PaymentFormConfigurationResponseFailure)response );
    }

    @Test
    void getPaymentFormConfiguration_invalidPluginConfiguration(){
        // given: the plugin configuration is invalid
        PaymentFormConfigurationRequest request = MockUtils.aPaymentFormConfigurationRequestBuilder()
                .withPluginConfiguration( "{not valid" )
                .build();

        // when: calling getPaymentFormConfiguration method
        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration( request );

        // then: response is a failure
        assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        checkPaymentResponse( (PaymentFormConfigurationResponseFailure)response );
    }

    @Test
    void getPaymentFormConfiguration_nominal(){
        // given: i18n service behaves normally and the plugin configuration is correct
        doReturn( "message" )
                .when( i18n )
                .getMessage( anyString(), any(Locale.class) );
        PaymentFormConfigurationRequest request = MockUtils.aPaymentFormConfigurationRequest();
        List<Bank> banks = NatixisBanksResponse.fromJson( PluginUtils.extractBanks( MockUtils.aPluginConfiguration()) ).getList();

        // when: calling getPaymentFormConfiguration method
        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration( request );

        // then: response is a success, the form is a BankTransferForm and the number of banks is correct
        assertEquals(PaymentFormConfigurationResponseSpecific.class, response.getClass());
        AbstractPaymentForm form = ((PaymentFormConfigurationResponseSpecific) response).getPaymentForm();
        assertNotNull( form.getButtonText() );
        assertNotNull( form.getDescription() );
        assertEquals(BankTransferForm.class, form.getClass());
        BankTransferForm bankTransferForm = (BankTransferForm) form;
        assertEquals(banks.size(), bankTransferForm.getBanks().size());
    }

    /**
     * Check the validity of a <code>PaymentFormConfigurationResponseFailure</code>,
     * based on <code>PaymentFormConfigurationResponseFailure#verifyIntegrity()</code> content
     * and the best practices.
     */
    private static void checkPaymentResponse( PaymentFormConfigurationResponseFailure response ){
        assertNotNull( response.getErrorCode() );
        assertTrue( response.getErrorCode().length() <= 50 );
        assertNotNull( response.getFailureCause() );
    }

}
