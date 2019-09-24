package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.business.NatixisBanksResponse;
import com.payline.payment.natixis.bean.business.bank.Bank;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.service.LogoPaymentFormConfigurationService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.paymentform.bean.field.SelectOption;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.payline.payment.natixis.utils.Constants.PartnerConfigurationKeys.BANKS_LIST;

public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentFormConfigurationServiceImpl.class);

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        PaymentFormConfigurationResponse pfcResponse;
        try {
            Locale locale = paymentFormConfigurationRequest.getLocale();

            // TODO: replace with PluginConfiguration ! (in the comment too)
            // Retrieve banks list from partner configuration
            String serialized = paymentFormConfigurationRequest.getPartnerConfiguration().getProperty( BANKS_LIST );
            final List<SelectOption> banks = new ArrayList<>();
            for( Bank bank : NatixisBanksResponse.fromJson( serialized ).getList() ){
                banks.add(SelectOption.SelectOptionBuilder.aSelectOption().withKey(bank.getBic()).withValue(bank.getName()).build());
            }

            // Build form
            CustomForm form = BankTransferForm.builder()
                    .withBanks( banks )
                    .withDescription( i18n.getMessage( "paymentForm.description", locale ) )
                    .withDisplayButton(true)
                    .withButtonText( i18n.getMessage( "paymentForm.buttonText", locale ) )
                    .withCustomFields( new ArrayList<>() )
                    .build();

            pfcResponse = PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm( form )
                    .build();
        }
        catch( PluginException e ){
            pfcResponse = e.toPaymentFormConfigurationResponseFailureBuilder().build();
        }
        catch( RuntimeException e ){
            LOGGER.error("Unexpected plugin error", e);
            pfcResponse = PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder
                    .aPaymentFormConfigurationResponseFailure()
                    .withErrorCode( PluginException.runtimeErrorCode( e ) )
                    .withFailureCause( FailureCause.INTERNAL_ERROR )
                    .build();
        }

        return pfcResponse;
    }
}
