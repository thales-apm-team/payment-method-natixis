package com.payline.payment.natixis.bean.business.authorization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class JwtUserBody {

    private Integer vers;
    @SerializedName("auth_time")
    private Long authTime;
    @SerializedName("appid")
    private String appId;
    @SerializedName("appname")
    private String appName;

    public Integer getVers() {
        return vers;
    }

    public Long getAuthTime() {
        return authTime;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public static JwtUserBody fromJson(String json ) throws JsonSyntaxException {
        return new Gson().fromJson( json, JwtUserBody.class );
    }

}
