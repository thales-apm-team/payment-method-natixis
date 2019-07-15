package com.payline.payment.natixis.bean.business.payment;

public class Amount {

    /**
     * Amount of money to be moved between debtor and creditor, before deduction of charges,
     * expressed in the currency of the debtor's account, and to be moved in a different currency.
     */
    private String amount;

    /**
     * Specifies the currency of the to be transferred amount, which is different from the currency of
     * the debtor's account.
     */
    private String currency;

    private Amount(AmountBuilder builder) {
        this.amount = builder.amount;
        this.currency = builder.currency;
    }

    public static class AmountBuilder {

        private String amount;
        private String currency;

        public AmountBuilder withAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public AmountBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Amount build(){
            return new Amount(this);
        }
    }

}
