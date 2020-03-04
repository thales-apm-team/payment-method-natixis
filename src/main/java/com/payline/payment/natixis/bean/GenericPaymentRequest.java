package com.payline.payment.natixis.bean;

import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.common.SubMerchant;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.WalletPaymentRequest;

import java.util.Date;
import java.util.Locale;

public class GenericPaymentRequest implements Request {
    private final Locale locale;
    private final Amount amount;
    private final Order order;
    private final Buyer buyer;
    private final ContractConfiguration contractConfiguration;
    private final Browser browser;
    private final Environment environment;
    private final PaymentFormContext paymentFormContext;
    private final RequestContext requestContext;
    private final String transactionId;
    private final String softDescriptor;
    private final boolean captureNow;
    private final PartnerConfiguration partnerConfiguration;
    private final SubMerchant subMerchant;
    private final String merchantName;
    private final String pluginConfiguration;
    private final Date differedActionDate;

    public GenericPaymentRequest(PaymentRequest request) {
        this.locale = request.getLocale();
        this.amount = request.getAmount();
        this.order = request.getOrder();
        this.buyer = request.getBuyer();
        this.contractConfiguration = request.getContractConfiguration();
        this.browser = request.getBrowser();
        this.environment = request.getEnvironment();
        this.paymentFormContext = request.getPaymentFormContext();
        this.requestContext = request.getRequestContext();
        this.transactionId = request.getTransactionId();
        this.softDescriptor = request.getSoftDescriptor();
        this.captureNow = request.isCaptureNow();
        this.partnerConfiguration = request.getPartnerConfiguration();
        this.subMerchant = request.getSubMerchant();
        this.merchantName = request.getMerchantName();
        this.pluginConfiguration = request.getPluginConfiguration();
        this.differedActionDate = request.getDifferedActionDate();
    }

    public GenericPaymentRequest(WalletPaymentRequest request){
        this.locale = request.getLocale();
        this.amount = request.getAmount();
        this.order = request.getOrder();
        this.buyer = request.getBuyer();
        this.contractConfiguration = request.getContractConfiguration();
        this.browser = request.getBrowser();
        this.environment = request.getEnvironment();
        this.paymentFormContext = request.getPaymentFormContext();
        this.requestContext = request.getRequestContext();
        this.transactionId = request.getTransactionId();
        this.softDescriptor = request.getSoftDescriptor();
        this.captureNow = request.isCaptureNow();
        this.partnerConfiguration = request.getPartnerConfiguration();
        this.subMerchant = request.getSubMerchant();
        this.merchantName = request.getMerchantName();
        this.pluginConfiguration = request.getPluginConfiguration();
        this.differedActionDate = request.getDifferedActionDate();
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public ContractConfiguration getContractConfiguration() {
        return this.contractConfiguration;
    }

    public Locale getLocale() {
        return locale;
    }

    public Amount getAmount() {
        return amount;
    }

    public Order getOrder() {
        return order;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public Browser getBrowser() {
        return browser;
    }

    public PaymentFormContext getPaymentFormContext() {
        return paymentFormContext;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSoftDescriptor() {
        return softDescriptor;
    }

    public boolean isCaptureNow() {
        return captureNow;
    }

    public PartnerConfiguration getPartnerConfiguration() {
        return partnerConfiguration;
    }

    public SubMerchant getSubMerchant() {
        return subMerchant;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getPluginConfiguration() {
        return pluginConfiguration;
    }

    public Date getDifferedActionDate() {
        return differedActionDate;
    }
}
