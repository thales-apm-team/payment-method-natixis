package com.payline.payment.natixis.utils;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginUtils {

    /* Static utility class : no need to instantiate it (Sonar bug fix) */
    private PluginUtils(){}

    /**
     * Put an entry into the given map only if the given key and value are not null.
     *
     * @param map The map
     * @param key The wanted key for the new entry
     * @param value The wanted value for the new entry
     */
    public static void safePut(Map<String, String> map, String key, String value ){
        if( key != null && value != null ){
            map.put( key, value );
        }
    }

    /**
     * Convert the path and headers of a {@link HttpRequestBase} to a readable {@link String}.
     * Mainly, for debugging purpose.
     *
     * @param httpRequest the request to convert
     * @return request method, path and headers as a string
     */
    public static String requestToString( HttpRequestBase httpRequest ){
        String ln = System.lineSeparator();
        String str = httpRequest.getMethod() + " " + httpRequest.getURI() + ln;

        List<String> strHeaders = new ArrayList<>();
        for( Header h : httpRequest.getAllHeaders() ){
            // For obvious security reason, the value of Authorization header is never printed in the logs
            if( HttpHeaders.AUTHORIZATION.equals( h.getName() ) ){
                String[] value = h.getValue().split(" ");
                strHeaders.add( h.getName() + ": " + ( value.length > 1 ? value[0] : "" ) + " *****" );
            }
            else {
                strHeaders.add( h.getName() + ": " + h.getValue() );
            }
        }
        str += String.join(ln, strHeaders);

        return str;
    }

    /**
     * Truncate the given string with the given length, if necessary.
     *
     * @param value The string to truncate
     * @param length The maximum allowed length
     * @return The truncated string
     */
    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

}
