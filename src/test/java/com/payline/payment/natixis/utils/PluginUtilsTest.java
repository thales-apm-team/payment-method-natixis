package com.payline.payment.natixis.utils;

import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PluginUtilsTest {

    @Test
    void safePut_nominal(){
        // given: proper key and value
        Map<String, String> map = new HashMap<>();
        String key = "thekey";
        String value = "thevalue";

        // when: calling safePut method
        PluginUtils.safePut( map, key, value );

        // then: the entry is added to the map
        assertEquals( 1, map.size() );
        assertEquals( value, map.get(key) );
    }

    @Test
    void safePut_nullKey(){
        // given: a proper value and a null key
        Map<String, String> map = new HashMap<>();
        String key = null;
        String value = "thevalue";

        // when: calling safePut method
        PluginUtils.safePut( map, key, value );

        // then: no entry is added to the map
        assertEquals( 0, map.size() );
    }

    @Test
    void safePut_nullValue(){
        // given: a proper key and a null value
        Map<String, String> map = new HashMap<>();
        String key = "thekey";
        String value = null;

        // when: calling safePut method
        PluginUtils.safePut( map, key, value );

        // then: no entry is added to the map
        assertEquals( 0, map.size() );
    }

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
