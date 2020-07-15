package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.bean.GenericPaymentRequest;
import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.payment.natixis.service.GenericPaymentService;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.service.PaymentService;

public class PaymentServiceImpl implements PaymentService {
    private GenericPaymentService genericPaymentService = GenericPaymentService.getInstance();

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getPaymentFormContext() == null
                || paymentRequest.getPaymentFormContext().getPaymentFormParameter() == null
                || paymentRequest.getPaymentFormContext().getPaymentFormParameter().get(BankTransferForm.BANK_KEY) == null) {
            throw new InvalidDataException("debtor BIC is required in payment form context");
        }

        GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);
        String bic = paymentRequest.getPaymentFormContext().getPaymentFormParameter().get(BankTransferForm.BANK_KEY);

        return genericPaymentService.paymentRequest(genericPaymentRequest, bic);
    }
}
