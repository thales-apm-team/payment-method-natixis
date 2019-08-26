package com.payline.payment.natixis.bean.business;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payline.payment.natixis.bean.business.bank.Bank;

import java.util.ArrayList;
import java.util.List;

public class NatixisBanksResponse {

    private List<Bank> accountServiceProviders;

    public List<Bank> getList() {
        return accountServiceProviders == null ? new ArrayList<>() : accountServiceProviders;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
        return gson.toJson( this );
    }

    public static NatixisBanksResponse fromJson(String json ){
        return new Gson().fromJson( json, NatixisBanksResponse.class );
    }

}
