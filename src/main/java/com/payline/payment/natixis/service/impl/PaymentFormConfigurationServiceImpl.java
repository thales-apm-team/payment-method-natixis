package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.business.NatixisBanksResponse;
import com.payline.payment.natixis.bean.business.bank.Bank;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.service.LogoPaymentFormConfigurationService;
import com.payline.payment.natixis.utils.Constants;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.paymentform.bean.field.FieldIcon;
import com.payline.pmapi.bean.paymentform.bean.field.InputType;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormInputFieldText;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.payline.payment.natixis.utils.Constants.PartnerConfigurationKeys.BANKS_LIST;

public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentFormConfigurationServiceImpl.class);

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        PaymentFormConfigurationResponse pfcResponse;
        try {
            // Retrieve banks list from partner configuration
            String serialized = paymentFormConfigurationRequest.getPartnerConfiguration().getProperty( BANKS_LIST );
            List<Bank> banks = NatixisBanksResponse.fromJson( serialized ).getList();

            // Temporary text input field for BIC
            PaymentFormInputFieldText tmpBicInput = PaymentFormInputFieldText.PaymentFormFieldTextBuilder
                    .aPaymentFormFieldText()
                    .withKey( Constants.PaymentFormContextKeys.DEBTOR_BIC )
                    .withLabel( "BIC" )
                    .withInputType( InputType.TEXT )
                    .withFieldIcon( FieldIcon.MONEY )
                    .build();

            List<PaymentFormField> fields = new ArrayList<>();
            fields.add( tmpBicInput );

            // Build form
            // TODO: use BankTransferForm instead of CustomForm
            CustomForm form = CustomForm.builder()
                    .withButtonText("Payer avec Natixis")
                    .withDisplayButton(true)
                    .withCustomFields( fields )
                    .withDescription("TODO")
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
