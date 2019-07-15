package com.payline.payment.natixis.utils.http;

import java.util.Date;

public class Authorization {

    private String accessToken;
    private Date expiresAt;
    private String tokenType;

    Authorization( AuthorizationBuilder builder ){
        this.accessToken = builder.accessToken;
        this.expiresAt = builder.expiresAt;
        this.tokenType = builder.tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getHeaderValue(){
        return this.tokenType + " " + this.accessToken;
    }

    public static class AuthorizationBuilder {

        private String accessToken;
        private Date expiresAt;
        private String tokenType;

        public AuthorizationBuilder withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AuthorizationBuilder withExpiresAt(Date expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public AuthorizationBuilder withTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Authorization build(){
            if( this.accessToken == null || this.expiresAt == null || this.tokenType == null ){
                throw new IllegalStateException("Authorization must not have any null attribute when built");
            }
            return new Authorization(this);
        }

    }

}
