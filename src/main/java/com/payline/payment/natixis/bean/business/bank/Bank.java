package com.payline.payment.natixis.bean.business.bank;

public class Bank {

    private String bankCode;
    private String bic;
    private String id;
    private String localInstrument;
    private Float maxAmount;
    private String name;
    private String serviceLevel;

    public String getBankCode() {
        return bankCode;
    }

    public String getBic() {
        return bic;
    }

    public String getId() {
        return id;
    }

    public String getLocalInstrument() {
        return localInstrument;
    }

    public Float getMaxAmount() {
        return maxAmount;
    }

    public String getName() {
        return name;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }
}
