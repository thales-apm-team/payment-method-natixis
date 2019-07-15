package com.payline.payment.natixis.utils.http;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import com.payline.payment.natixis.TestUtils;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.security.RSAHolder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.payline.payment.natixis.utils.http.HttpTestUtils.mockHttpResponse;

class NatixisHttpClientTest {

    @InjectMocks
    private NatixisHttpClient natixisHttpClient;

    @Mock
    private CloseableHttpClient client;
    @Mock
    private RSAHolder rsaHolder;

    @BeforeEach
    void setup() throws NoSuchFieldException {
        // Init tested instance and inject mocks
        natixisHttpClient = new NatixisHttpClient();
        MockitoAnnotations.initMocks(this);

        // Manual init of private attributes
        FieldSetter.setField( natixisHttpClient, natixisHttpClient.getClass().getDeclaredField("retries"), 3);
    }

    @Test
    void authorize_alreadyAuthorized(){
        // given: a valid authorization is already stored in the client
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        doReturn( true ).when( spiedClient ).isAuthorized();

        // when: calling the authorize method
        spiedClient.authorize( MockUtils.aRequestConfiguration() );

        // then: no HTTP call is made
        verify( spiedClient, never() ).execute( any( HttpRequestBase.class ) );
    }

    @Test
    void authorize_missingApiUrl(){
        // given: the API base URL is missing from the partner configuration
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( new HashMap<>(), new HashMap<>() ) );

        // when calling the authorize method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.authorize( requestConfiguration ));
    }

    @Test
    void authorize_missingClientId(){
        // given: the client Id is missing from the contract configuration
        ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        contractConfiguration.getContractProperties().remove(Constants.ContractConfigurationKeys.CLIENT_ID);
        RequestConfiguration requestConfiguration = new RequestConfiguration( contractConfiguration, MockUtils.anEnvironment(), MockUtils.aPartnerConfiguration() );

        // when calling the authorize method, an exception is thrown
        assertThrows( InvalidDataException.class, () -> natixisHttpClient.authorize( requestConfiguration ));
    }

    @Test
    void authorize_missingClientSecret(){
        // given: the client Id is missing from the contract configuration
        ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        contractConfiguration.getContractProperties().remove(Constants.ContractConfigurationKeys.CLIENT_SECRET);
        RequestConfiguration requestConfiguration = new RequestConfiguration( contractConfiguration, MockUtils.anEnvironment(), MockUtils.aPartnerConfiguration() );

        // when calling the authorize method, an exception is thrown
        assertThrows( InvalidDataException.class, () -> natixisHttpClient.authorize( requestConfiguration ));
    }

    @Test
    void authorize_invalidApiUrl(){
        // given: the API base URL from the partner configuration in invalid
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_AUTH_BASE_URL, "https:||np-auth.api.qua.natixis.com/api");
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( partnerConfigurationMap, new HashMap<>() ) );

        // when calling the authorize method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.authorize( requestConfiguration ));
    }

    /**
     * This is a test set of server response contents to an authorization request,
     * which SHOULD prevent the building of a valid Authorization.
     * In all these cases, the authorize() method is expected to throw an exception.
     */
    private static Stream<String> authorize_blockingResponseContent_set() {
        String timestamp = TestUtils.currentTimestampWithoutMillis();

        Stream.Builder<String> builder = Stream.builder();
        // non valid JSON content
        builder.accept( "{/}" );
        // valid JSON content, but not an object
        builder.accept( "[]" );
        // the response does not contain an access_token
        builder.accept( "{\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{\\n  \\\"vers\\\" : 1,\\n  \\\"auth_time\\\" : " + timestamp + ",\\n  \\\"appid\\\" : \\\"f5f36db8-8507-452f-bd66-c33f286e2cce\\\",\\n  \\\"appname\\\" : \\\"FLQ_HUBPISP_MONEXT\\\"\\n}\"}" );
        // the JWT body is not valid JSON
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{/}\"}" );
        // the JWT body is valid JSON, but not a JSON object
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"[]\"}" );

        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("authorize_blockingResponseContent_set")
    void authorize_blockingResponseContent( String responseContent ) throws NoSuchFieldException {
        // given: the server returns a response with a non-sufficient content
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        StringResponse response = new StringResponse();
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("content"), responseContent);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusCode"), HttpStatus.SC_OK);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusMessage"), "OK");
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method, an exception is thrown
        assertThrows( PluginException.class, () -> spiedClient.authorize( MockUtils.aRequestConfiguration() ) );
    }

    /**
     * This is a test set of server response contents to an authorization request,
     * which SHOULD NOT prevent the service from building a valid Authorization instance.
     * In all these cases, a valid authorization must be built in the end.
     */
    private static Stream<String> authorize_nonBlockingResponseContent_set(){
        String timestamp = TestUtils.currentTimestampWithoutMillis();

        Stream.Builder<String> builder = Stream.builder();
        // nominal case
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{\\n  \\\"vers\\\" : 1,\\n  \\\"auth_time\\\" : " + timestamp + ",\\n  \\\"appid\\\" : \\\"f5f36db8-8507-452f-bd66-c33f286e2cce\\\",\\n  \\\"appname\\\" : \\\"FLQ_HUBPISP_MONEXT\\\"\\n}\"}" );
        // no token type
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{\\n  \\\"vers\\\" : 1,\\n  \\\"auth_time\\\" : " + timestamp + ",\\n  \\\"appid\\\" : \\\"f5f36db8-8507-452f-bd66-c33f286e2cce\\\",\\n  \\\"appname\\\" : \\\"FLQ_HUBPISP_MONEXT\\\"\\n}\"}" );
        // no expiration delay
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{\\n  \\\"vers\\\" : 1,\\n  \\\"auth_time\\\" : " + timestamp + ",\\n  \\\"appid\\\" : \\\"f5f36db8-8507-452f-bd66-c33f286e2cce\\\",\\n  \\\"appname\\\" : \\\"FLQ_HUBPISP_MONEXT\\\"\\n}\"}" );
        // no JWT body
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\"}" );
        // no auth time in the JWT body
        builder.accept( "{\"access_token\":\"ABCD012345679\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"managePaymentRequestHubPISP\",\"jwt.user.body\":\"{\\n  \\\"vers\\\" : 1,\\n  \\\"appid\\\" : \\\"f5f36db8-8507-452f-bd66-c33f286e2cce\\\",\\n  \\\"appname\\\" : \\\"FLQ_HUBPISP_MONEXT\\\"\\n}\"}" );

        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("authorize_nonBlockingResponseContent_set")
    void authorize_nonBlockingResponseContent( String responseContent ) throws NoSuchFieldException {
        // given: the server returns a response with a sufficient content
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        StringResponse response = new StringResponse();
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("content"), responseContent);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusCode"), HttpStatus.SC_OK);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusMessage"), "OK");
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method
        spiedClient.authorize( MockUtils.aRequestConfiguration() );

        // then: the client now contains a valid authorization
        assertTrue( spiedClient.isAuthorized() );
    }


    @Test
    void authorize_wrongClientId() throws NoSuchFieldException {
        // given: the server returns an Internal Server Error
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        StringResponse response = new StringResponse();
        /*
         * Test case built upon observation of the API responses during development
         */
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("content"), "grant_type=client_credentials");
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusCode"), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusMessage"), "Internal Server Error");
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method, an exception is thrown
        PluginException e = assertThrows( PluginException.class, () -> spiedClient.authorize( MockUtils.aRequestConfiguration() ) );
    }

    @Test
    void authorize_wrongClientSecret() throws NoSuchFieldException {
        // given: the server returns an Internal Server Error
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        StringResponse response = new StringResponse();
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("content"), "{\n  \"error\" : \"invalid_client\",\n  \"error_description\" : \"Client authentication failed (e.g. unknown client, no client authentication included, or unsupported authentication method).  The authorization server MAY return an HTTP 401 (Unauthorized) status code to indicate which HTTP authentication schemes are supported. \"\n}");
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusCode"), HttpStatus.SC_UNAUTHORIZED);
        FieldSetter.setField( response, StringResponse.class.getDeclaredField("statusMessage"), "Unauthorized");
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method, an exception is thrown
        PluginException e = assertThrows( PluginException.class, () -> spiedClient.authorize( MockUtils.aRequestConfiguration() ) );
        assertTrue( e.getErrorCode().contains("invalid_client") );
    }

    @Test
    void execute_nominal() throws IOException {
        // given: a properly formatted request, which gets a proper response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        int expectedStatusCode = 200;
        String expectedStatusMessage = "OK";
        String expectedContent = "{\"content\":\"fake\"}";
        doReturn( mockHttpResponse( expectedStatusCode, expectedStatusMessage, expectedContent, null ) )
                .when( client ).execute( request );

        // when: sending the request
        StringResponse stringResponse = natixisHttpClient.execute( request );

        // then: the content of the StringResponse reflects the content of the HTTP response
        assertNotNull( stringResponse );
        assertEquals( expectedStatusCode, stringResponse.getStatusCode() );
        assertEquals( expectedStatusMessage, stringResponse.getStatusMessage() );
        assertEquals( expectedContent, stringResponse.getContent() );
    }

    @Test
    void execute_retry() throws IOException {
        // given: the first 2 requests end up in timeout, the third request gets a response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        when( client.execute( request ) )
                .thenThrow( ConnectTimeoutException.class )
                .thenThrow( ConnectTimeoutException.class )
                .thenReturn( mockHttpResponse( 200, "OK", "content", null) );

        // when: sending the request
        StringResponse stringResponse = natixisHttpClient.execute( request );

        // then: the client finally gets the response
        assertNotNull( stringResponse );
    }

    @Test
    void execute_retryFail() throws IOException {
        // given: a request which always gets an exception
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        doThrow( IOException.class ).when( client ).execute( request );

        // when: sending the request, a PluginException is thrown
        assertThrows( PluginException.class, () -> natixisHttpClient.execute( request ) );
    }

    @Test
    void execute_invalidResponse() throws IOException {
        // given: a request that gets an invalid response (null)
        HttpGet request = new HttpGet("http://domain.test.fr/malfunctioning-endpoint");
        doReturn( null ).when( client ).execute( request );

        // when: sending the request, a PluginException is thrown
        assertThrows( PluginException.class, () -> natixisHttpClient.execute( request ) );
    }

    @Test
    void isAuthorized_valid() throws NoSuchFieldException {
        // given: a valid authorization
        Authorization validAuth = MockUtils.anAuthorizationBuilder().build();
        FieldSetter.setField( natixisHttpClient, natixisHttpClient.getClass().getDeclaredField("authorization"), validAuth);

        // when called, isAuthorized method returns true
        assertTrue( natixisHttpClient.isAuthorized() );
    }

    @Test
    void isAuthorized_null() throws NoSuchFieldException {
        // given: the client does not contain a valid authorization
        FieldSetter.setField( natixisHttpClient, natixisHttpClient.getClass().getDeclaredField("authorization"), null);

        // when called, isAuthorized method returns false
        assertFalse( natixisHttpClient.isAuthorized() );
    }

    @Test
    void isAuthorized_expired() throws NoSuchFieldException {
        // given: an expired authorization
        Authorization expiredAuth = MockUtils.anAuthorizationBuilder()
                .withExpiresAt(TestUtils.addTime(new Date(), Calendar.HOUR, -1))
                .build();
        FieldSetter.setField( natixisHttpClient, natixisHttpClient.getClass().getDeclaredField("authorization"), expiredAuth);

        // when called, isAuthorized method returns false
        assertFalse( natixisHttpClient.isAuthorized() );
    }

    // TODO: test handleErrorResponse()

    @Test
    void paymentInit_missingApiUrl(){
        // given: the API base URL is missing from the partner configuration
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( new HashMap<>(), new HashMap<>() ) );

        // when calling the paymentInit method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.paymentInit( MockUtils.aPayment(), MockUtils.aPsuInformation(), requestConfiguration ));
    }

    @Test
    void paymentInit_invalidApiUrl(){
        // given: the client is authorized and the API base URL from the partner configuration in invalid
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        doReturn( true ).when( spiedClient ).isAuthorized();

        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https:||np.api.qua.natixis.com/hub-pisp/v1");
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( partnerConfigurationMap, new HashMap<>() ) );

        // when calling the paymentInit method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> spiedClient.paymentInit( MockUtils.aPayment(), MockUtils.aPsuInformation(), requestConfiguration ));
    }

    // TODO: complete tests when paymentInit method will return something

    @Test
    void paymentStatus_missingApiUrl(){
        // given: the API base URL is missing from the partner configuration
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( new HashMap<>(), new HashMap<>() ) );

        // when calling the paymentStatus method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.paymentStatus( "1234567890", requestConfiguration ));
    }

    @Test
    void paymentStatus_invalidApiUrl(){
        // given: the client is authorized and the API base URL from the partner configuration in invalid
        NatixisHttpClient spiedClient = Mockito.spy( natixisHttpClient );
        doReturn( true ).when( spiedClient ).isAuthorized();

        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https:||np.api.qua.natixis.com/hub-pisp/v1");
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( partnerConfigurationMap, new HashMap<>() ) );

        // when calling the paymentStatus method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> spiedClient.paymentStatus( "1234567890", requestConfiguration ));
    }

    // TODO: complete tests when paymentStatus method will return something

}