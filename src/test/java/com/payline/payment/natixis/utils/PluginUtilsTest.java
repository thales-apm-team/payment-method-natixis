package com.payline.payment.natixis.utils;

import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PluginUtilsTest {

    @Test
    void requestToString(){
        // given: a HTTP request with headers
        HttpGet request = new HttpGet( "http://domain.test.fr/endpoint" );
        request.setHeader("Authorization", "Basic sensitiveStringThatShouldNotAppear");
        request.setHeader("Other", "This is safe to display");

        // when: converting the request to String for display
        String result = PluginUtils.requestToString( request );

        // then: the result is as expected
        String ln = System.lineSeparator();
        String expected = "GET http://domain.test.fr/endpoint" + ln
                + "Authorization: Basic *****" + ln
                + "Other: This is safe to display";
        assertEquals(expected, result);
    }

    @Test
    void truncate() {
        assertEquals("0123456789", PluginUtils.truncate("01234567890123456789", 10));
        assertEquals("01234567890123456789", PluginUtils.truncate("01234567890123456789", 60));
        assertEquals("", PluginUtils.truncate("", 30));
        assertNull(PluginUtils.truncate(null, 30));
    }

}
