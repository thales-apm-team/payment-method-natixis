package com.payline.payment.natixis.bean.configuration;

import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.RetrievePluginConfigurationRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;

/**
 * Generic class that supports any type of request's configuration.
 */
public class RequestConfiguration {

    private ContractConfiguration contractConfiguration;
    private Environment environment;
    private PartnerConfiguration partnerConfiguration;

    public RequestConfiguration(ContractConfiguration contractConfiguration, Environment environment, PartnerConfiguration partnerConfiguration) {
        if( contractConfiguration == null ){
            throw new InvalidDataException("Missing request contractConfiguration");
        }
        if( environment == null ){
            throw new InvalidDataException("Missing request environment");
        }
        if( partnerConfiguration == null ){
            throw new InvalidDataException("Missing request partnerConfiguration");
        }
        this.contractConfiguration = contractConfiguration;
        this.environment = environment;
        this.partnerConfiguration = partnerConfiguration;
    }

    public ContractConfiguration getContractConfiguration() {
        return contractConfiguration;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public PartnerConfiguration getPartnerConfiguration() {
        return partnerConfiguration;
    }

    public static RequestConfiguration build(CaptureRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(ContractParametersCheckRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(NotificationRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(PaymentFormConfigurationRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(PaymentRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(RefundRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(ResetRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(RetrievePluginConfigurationRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }

    public static RequestConfiguration build(TransactionStatusRequest request){
        return new RequestConfiguration( request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration() );
    }
}
