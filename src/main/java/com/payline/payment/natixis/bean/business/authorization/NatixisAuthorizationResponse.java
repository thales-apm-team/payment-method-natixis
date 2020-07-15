package com.payline.payment.natixis.bean.business.authorization;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class NatixisAuthorizationResponse {

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private Integer expiresIn;
    private String scope;
    @SerializedName("jwt.user.body")
    private String jwtUserBody;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getJwtUserBody() {
        return jwtUserBody;
    }

    public static NatixisAuthorizationResponse fromJson(String json ) {
        return new Gson().fromJson( json, NatixisAuthorizationResponse.class );
    }

}
