package com.payline.payment.natixis.utils.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationTest {

    private Authorization.AuthorizationBuilder builder;

    @BeforeEach
    void setup(){
        builder = new Authorization.AuthorizationBuilder();
    }

    @Test
    void builder_nominal(){
        // given: the builder is passed all required information
        Date now = new Date();
        builder.withAccessToken("access token")
                .withTokenType("Type")
                .withExpiresAt(now);

        // when: building the instance
        Authorization instance = builder.build();

        // then: instance attributes values match the one given to the builder
        assertEquals( "access token", instance.getAccessToken() );
        assertEquals( "Type", instance.getTokenType() );
        assertEquals( now, instance.getExpiresAt() );
        assertEquals( "Type access token", instance.getHeaderValue() );
    }

    @Test
    void builder_missingAccesstoken(){
        // given: the builder is not passed an access token
        Date now = new Date();
        builder.withTokenType("token type")
                .withExpiresAt(now);

        // when: building the instance, an exception is thrown
        assertThrows( IllegalStateException.class, () -> builder.build() );
    }

    @Test
    void builder_missingTokenType(){
        // given: the builder is not passed a token type
        Date now = new Date();
        builder.withAccessToken("access token")
                .withExpiresAt(now);

        // when: building the instance, an exception is thrown
        assertThrows( IllegalStateException.class, () -> builder.build() );
    }

    @Test
    void builder_missingExpiresAt(){
        // given: the builder is not passed an expiration date
        builder.withAccessToken("access token")
                .withTokenType("token type");

        // when: building the instance, an exception is thrown
        assertThrows( IllegalStateException.class, () -> builder.build() );
    }

}
