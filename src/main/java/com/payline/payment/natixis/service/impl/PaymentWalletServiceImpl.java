package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.GenericPaymentRequest;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.service.GenericPaymentService;
import com.payline.payment.natixis.utils.PluginUtils;
import com.payline.payment.natixis.utils.security.RSAUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.WalletPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWalletService;
import org.apache.logging.log4j.Logger;

public class PaymentWalletServiceImpl implements PaymentWalletService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentWalletServiceImpl.class);

    private RSAUtils rsaUtils = RSAUtils.getInstance();
    private GenericPaymentService service = GenericPaymentService.getInstance();

    @Override
    public PaymentResponse walletPaymentRequest(WalletPaymentRequest walletPaymentRequest) {
        try {
            GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(walletPaymentRequest);

            // get decrypted wallet data (BIC)
            String encryptedBic = walletPaymentRequest.getWallet().getPluginPaymentData();
            String key = PluginUtils.extractKey(walletPaymentRequest.getPluginConfiguration());
            String bic = rsaUtils.decrypt(encryptedBic, key);

            return service.paymentRequest(genericPaymentRequest, bic);
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            return PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }
    }
}
