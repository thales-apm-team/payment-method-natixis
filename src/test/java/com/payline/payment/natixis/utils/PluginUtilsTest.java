package com.payline.payment.natixis.utils;

import com.payline.pmapi.bean.common.FailureCause;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PluginUtilsTest {

    @Test
    void jsonMinify(){
        // given: a JSON string, containing various forms of spaces and line breaks
        String json = "{\n" +
                "  \"property\" : \"value containing space\",\n" +
                "  \"otherProperty\":  {\n" +
                "\t\"stringChild\" : \"with comma,\",\n" +
                "    \"array child\"    : [\n" +
                "      \"element 1  \"  ,\r\n" +
                "      \"element2\",\n" +
                "        \"element   3\"\n" +
                "    ]     \r\n" +
                "  }\t\n" +
                "}  ";

        // when: minifying it
        String result = PluginUtils.jsonMinify( json );

        // then: result match the expected value
        String expected = "{\"property\":\"value containing space\",\"otherProperty\":{\"stringChild\":\"with comma,\"," +
                "\"array child\":[\"element 1  \",\"element2\",\"element   3\"]}}";
        assertEquals( expected, result );
    }

    @ParameterizedTest
    @MethodSource("replaceChars_set")
    void replaceChars( String input, String expectedOutput ){
        assertEquals( expectedOutput, PluginUtils.replaceChars( input ) );
    }
    private static Stream<Arguments> replaceChars_set() {
        return Stream.of(
                Arguments.of( "àâäçèéêëïîôöùû", "aaaceeeeiioouu" ),
                Arguments.of( "æÆ", "aeAE" ),
                Arguments.of( "œŒ", "oeOE" ),
                Arguments.of( "ÀÂÄÇÈÉÊËÏÎÔÖÙÛ", "AAACEEEEIIOOUU" ),
                Arguments.of( "l'apostrophe", "l apostrophe" ),
                Arguments.of( "/-?:().,\"+ ", "/-?:().,\"+ " ),
                Arguments.of( "", "" ),
                Arguments.of( null, null )
        );
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
    void truncate() {
        assertEquals("0123456789", PluginUtils.truncate("01234567890123456789", 10));
        assertEquals("01234567890123456789", PluginUtils.truncate("01234567890123456789", 60));
        assertEquals("", PluginUtils.truncate("", 30));
        assertNull(PluginUtils.truncate(null, 30));
    }

}
