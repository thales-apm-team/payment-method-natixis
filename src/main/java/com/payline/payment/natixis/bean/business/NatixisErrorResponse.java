package com.payline.payment.natixis.bean.business;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class NatixisErrorResponse {

    private String error;
    private String message;
    private String path;
    private String status;
    private Date timestamp;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getStatus() {
        return status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public static NatixisErrorResponse fromJson(String json ){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        return gson.fromJson( json, NatixisErrorResponse.class );
    }

}
