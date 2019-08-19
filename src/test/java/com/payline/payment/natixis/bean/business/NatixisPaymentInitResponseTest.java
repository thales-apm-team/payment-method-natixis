package com.payline.payment.natixis.bean.business;

import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.StringResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NatixisPaymentInitResponseTest {

    private static final String PAYMENT_ID = "0000000634-156620939900013135879318";
    private static final String CONSENT_APPROVAL_URL = "https://www.rs-ex-hml-89c3api.qpa.bpce.fr/89C3api/accreditation/v1/identificationPisp?paymentRequestRessourceId="+PAYMENT_ID+"&nonce=E3BNDmkGVP4qtO1FLQZS";

    private String responseContent;
    private Map<String, String> headers;
    private StringResponse stringResponse;

    @BeforeEach
    void setup() throws NoSuchFieldException {
        stringResponse = new StringResponse();
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("statusCode"), HttpStatus.SC_CREATED);
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("statusMessage"), "Created");
    }

    /**
     * Test set of valid response elements to a payment initiation.
     * In all these cases, the fromStringResponse method SHOULD be able to extract elements from the server response.
     */
    static Stream<Arguments> fromStringResponse_passing_set(){
        Stream.Builder<Arguments> builder = Stream.builder();

        // nominal case
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                CONSENT_APPROVAL_URL,
                "payment-requests/" + PAYMENT_ID,
                PAYMENT_ID ));

        // variants in location header value
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                CONSENT_APPROVAL_URL,
                "/" + PAYMENT_ID,
                PAYMENT_ID ));
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                CONSENT_APPROVAL_URL,
                "https://full.domain.com/payment-requests/" + PAYMENT_ID,
                PAYMENT_ID ));

        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("fromStringResponse_passing_set")
    void fromStringResponse_passing( String responseContent, String expectedUrl, String locationHeader, String expectedPaymentId ) throws NoSuchFieldException {
        // given: response content and location header
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("content"), responseContent);
        Map<String, String> headers = new HashMap<>();
        headers.put("location", locationHeader);
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("headers"), headers);

        // when: building instance from StringResponse
        NatixisPaymentInitResponse instance = NatixisPaymentInitResponse.fromStringResponse( stringResponse );

        // then: extracted values are as expected
        assertEquals( expectedPaymentId, instance.getPaymentId() );
        assertEquals( expectedUrl, instance.getContentApprovalUrl() );
    }

    /**
     * Test set of invalid or incomplete response elements to a payment initiation.
     * In all these cases, the fromStringResponse method SHOULD throw a PluginException.
     */
    static Stream<Arguments> fromStringResponse_blocking_set(){
        Stream.Builder<Arguments> builder = Stream.builder();

        // response content is not valid JSON
        builder.accept(Arguments.of( "{/}",
                "payment-requests/" + PAYMENT_ID ));
        // response content is valid JSON, but not an object
        builder.accept(Arguments.of( "[]",
                "payment-requests/" + PAYMENT_ID ));
        // no _links property
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                "payment-requests/" + PAYMENT_ID ));
        // _links is not a JSON object
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":[{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}]}",
                "payment-requests/" + PAYMENT_ID ));
        // _links.consentApproval does not exist
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consent\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                "payment-requests/" + PAYMENT_ID ));
        // _links.consentApproval is not a valid JSON object
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}",
                "payment-requests/" + PAYMENT_ID ));
        // _links.consentApproval does not have a href property
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"templated\":true}}}",
                "payment-requests/" + PAYMENT_ID ));

        // No Location header
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                null ));
        // Location header does not contain a payment Id
        builder.accept(Arguments.of( "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"" + CONSENT_APPROVAL_URL + "\",\"templated\":true}}}",
                "payment-requests" ));

        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("fromStringResponse_blocking_set")
    void fromStringResponse_blocking( String responseContent, String locationHeader ) throws NoSuchFieldException {
        // given: response content and location header
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("content"), responseContent);
        Map<String, String> headers = new HashMap<>();
        if( locationHeader != null ){
            headers.put("location", locationHeader);
        }
        FieldSetter.setField( stringResponse, StringResponse.class.getDeclaredField("headers"), headers);

        // when: building instance from StringResponse, an exception is thrown
        PluginException e = assertThrows(PluginException.class, () -> NatixisPaymentInitResponse.fromStringResponse( stringResponse ) );
    }

}
