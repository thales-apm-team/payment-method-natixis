package com.payline.payment.natixis.bean.business.authorization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Error response format described in RFC 6749.
 * @see https://tools.ietf.org/html/rfc6749#section-5.2
 */
public class RFC6749AccessTokenErrorResponse {

    private String error;
    @SerializedName("error_description")
    private String errorDescription;
    @SerializedName("error_uri")
    private String errorUri;

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public static RFC6749AccessTokenErrorResponse fromJson(String json ) {
        return new Gson().fromJson( json, RFC6749AccessTokenErrorResponse.class );
    }

}
