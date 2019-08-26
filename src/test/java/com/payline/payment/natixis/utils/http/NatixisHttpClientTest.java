package com.payline.payment.natixis.utils.http;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.TestUtils;
import com.payline.payment.natixis.bean.business.NatixisPaymentInitResponse;
import com.payline.payment.natixis.bean.business.bank.AccountServiceProviders;
import com.payline.payment.natixis.bean.business.fraud.PsuInformation;
import com.payline.payment.natixis.bean.business.payment.Payment;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.InvalidDataException;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.security.RSAHolder;
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
import org.mockito.*;
import org.mockito.internal.util.reflection.FieldSetter;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.payline.payment.natixis.utils.http.HttpTestUtils.mockHttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Two test instances of {@link NatixisHttpClient} are available in this test class :
 *
 *  - natixisHttpClient: is instantiated and injected with mocked attributes.
 *    Use it to test low level (often package-private) methods or if you need to use reflection.
 *
 *  - spiedClient: is created from the previous. You can't use reflection on a spied class.
 *    But you can spy on method calls and mock their returns to isolate the tested method.
 */
class NatixisHttpClientTest {

    @InjectMocks private NatixisHttpClient natixisHttpClient;
    private NatixisHttpClient spiedClient;

    @Mock private CloseableHttpClient http;
    @Mock private RSAHolder rsaHolder;

    @BeforeEach
    void setup() throws NoSuchFieldException {
        // Init tested instance and inject mocks
        natixisHttpClient = new NatixisHttpClient();
        MockitoAnnotations.initMocks(this);

        // Manual init of private attributes
        FieldSetter.setField( natixisHttpClient, natixisHttpClient.getClass().getDeclaredField("retries"), 3);

        // Init spied client
        spiedClient = Mockito.spy( natixisHttpClient );
    }

    // --- Test NatixisHttpClient#authorize ---

    @Test
    void authorize_alreadyAuthorized(){
        // given: a valid authorization is already stored in the client
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
    void authorize_blockingResponseContent( String responseContent ) {
        // given: the server returns a response with a non-sufficient content
        StringResponse response = HttpTestUtils.mockStringResponse( HttpStatus.SC_OK, "OK", responseContent, null );
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
    void authorize_nonBlockingResponseContent( String responseContent ) {
        // given: the server returns a response with a sufficient content
        StringResponse response = HttpTestUtils.mockStringResponse( HttpStatus.SC_OK, "OK", responseContent, null );
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method
        spiedClient.authorize( MockUtils.aRequestConfiguration() );

        // then: the client now contains a valid authorization
        assertTrue( spiedClient.isAuthorized() );
    }


    @Test
    void authorize_wrongClientId() {
        // given: the server returns an Internal Server Error
        // Test case built upon observation of the API responses during development
        StringResponse response = HttpTestUtils.mockStringResponse( HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "grant_type=client_credentials", null );
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method, an exception is thrown
        PluginException e = assertThrows( PluginException.class, () -> spiedClient.authorize( MockUtils.aRequestConfiguration() ) );
    }

    @Test
    void authorize_wrongClientSecret() {
        // given: the server returns an Internal Server Error
        String content = "{\n  \"error\" : \"invalid_client\",\n  \"error_description\" : \"Client authentication failed (e.g. unknown client, no client authentication included, or unsupported authentication method).  The authorization server MAY return an HTTP 401 (Unauthorized) status code to indicate which HTTP authentication schemes are supported. \"\n}";
        StringResponse response = HttpTestUtils.mockStringResponse( HttpStatus.SC_UNAUTHORIZED, "Unauthorized", content, null );
        doReturn( response ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the authorize method, an exception is thrown
        PluginException e = assertThrows( PluginException.class, () -> spiedClient.authorize( MockUtils.aRequestConfiguration() ) );
        assertTrue( e.getErrorCode().contains("invalid_client") );
    }

    // --- Test NatixisHttpClient#banks ---

    @Test
    void banks_nominal(){
        // given: the partner API returns a valid response
        String responseContent = "{\"accountServiceProviders\":[{\"id\":\"CCBPFRPPNAN\",\"bic\":\"CCBPFRPPNAN\",\"bankCode\":\"13807\",\"name\":\"BANQUE POPULAIRE GRAND OUEST\",\"serviceLevel\":\"SEPA\",\"localInstrument\":null,\"maxAmount\":null},{\"id\":\"CMBRFR2BARK\",\"bic\":\"CMBRFR2BARK\",\"bankCode\":\"15589\",\"name\":\"Crédit Mutuel de Bretagne\",\"serviceLevel\":\"SEPA\",\"localInstrument\":\"INST\",\"maxAmount\":15000}]}";
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_OK, "OK", responseContent, null );
        doReturn( stringResponse ).when( spiedClient ).get( anyString(), any(RequestConfiguration.class) );

        // when: retrieving the banks list
        AccountServiceProviders banks = spiedClient.banks( MockUtils.aRequestConfiguration() );

        // then: result contains 2 banks
        assertEquals( 2, banks.getList().size() );
    }

    @Test
    void banks_missingApiUrl(){
        // given: the API base URL is missing from the partner configuration
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( new HashMap<>(), new HashMap<>() ) );

        // when calling the paymentStatus method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.banks( requestConfiguration ));
    }

    @Test
    void banks_parsingError(){
        // given: the partner API returns a valid response
        String responseContent = "[{\"id\":\"CCBPFRPPNAN\",\"bic\":\"CCBPFRPPNAN\",\"bankCode\":\"13807\",\"name\":\"BANQUE POPULAIRE GRAND OUEST\",\"serviceLevel\":\"SEPA\",\"localInstrument\":null,\"maxAmount\":null},{\"id\":\"CMBRFR2BARK\",\"bic\":\"CMBRFR2BARK\",\"bankCode\":\"15589\",\"name\":\"Crédit Mutuel de Bretagne\",\"serviceLevel\":\"SEPA\",\"localInstrument\":\"INST\",\"maxAmount\":15000}]";
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_OK, "OK", responseContent, null );
        doReturn( stringResponse ).when( spiedClient ).get( anyString(), any(RequestConfiguration.class) );

        // when: retrieving the banks list, then: an exception is thrown
        assertThrows( PluginException.class, () -> spiedClient.banks( MockUtils.aRequestConfiguration() ) );
    }

    // --- Test NatixisHttpClient#execute ---

    @Test
    void execute_nominal() throws IOException {
        // given: a properly formatted request, which gets a proper response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        int expectedStatusCode = 200;
        String expectedStatusMessage = "OK";
        String expectedContent = "{\"content\":\"fake\"}";
        doReturn( mockHttpResponse( expectedStatusCode, expectedStatusMessage, expectedContent, null ) )
                .when( http ).execute( request );

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
        when( http.execute( request ) )
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
        doThrow( IOException.class ).when( http ).execute( request );

        // when: sending the request, a PluginException is thrown
        assertThrows( PluginException.class, () -> natixisHttpClient.execute( request ) );
    }

    @Test
    void execute_invalidResponse() throws IOException {
        // given: a request that gets an invalid response (null)
        HttpGet request = new HttpGet("http://domain.test.fr/malfunctioning-endpoint");
        doReturn( null ).when( http ).execute( request );

        // when: sending the request, a PluginException is thrown
        assertThrows( PluginException.class, () -> natixisHttpClient.execute( request ) );
    }

    // --- Test NatixisHttpClient#generateSignature ---

    @Test
    void generateSignature_nominal() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: valid inputs and rsaHolder returns a key
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "header_value");
        String keyId = "a-key-id";
        doReturn( MockUtils.aPrivateKey() ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature
        Signature signature = natixisHttpClient.generateSignature( httpRequest, headers, keyId, "(request-target)", "SomeHeader" );

        // then: the generated signature seems valid
        assertNotNull( signature );
        assertTrue( signature.toString().contains( keyId ) );
    }

    @Test
    void generateSignature_publicKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: valid inputs, but rsaHolder returns a public key instead of a private key
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "header_value");
        String keyId = "a-key-id";
        doReturn( MockUtils.aPublicKey() ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature, then: a PluginException is thrown
        assertThrows( PluginException.class, () ->  natixisHttpClient.generateSignature( httpRequest, headers, keyId, "(request-target)", "SomeHeader" ) );
    }

    @Test
    void generateSignature_nullKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: valid inputs, but rsaHolder returns null instead of a key
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "header_value");
        String keyId = "a-key-id";
        doReturn( null ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature, then: a PluginException is thrown
        assertThrows( PluginException.class, () ->  natixisHttpClient.generateSignature( httpRequest, headers, keyId, "(request-target)", "SomeHeader" ) );
    }

    @Test
    void generateSignature_rsaHolderException() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: valid inputs, but rsaHolder throws an exception (key not recoverable, for example)
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "header_value");
        String keyId = "a-key-id";
        doThrow( UnrecoverableKeyException.class ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature, then: a PluginException is thrown
        assertThrows( PluginException.class, () ->  natixisHttpClient.generateSignature( httpRequest, headers, keyId, "(request-target)", "SomeHeader" ) );
    }

    @Test
    void generateSignature_nullKeyId() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: rsaHolder returns a valid private key, but keyId is null
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "header_value");
        doReturn( MockUtils.aPrivateKey() ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature, then: a PluginException is thrown
        assertThrows( PluginException.class, () ->  natixisHttpClient.generateSignature( httpRequest, headers, null, "(request-target)", "SomeHeader" ) );
    }

    @Test
    void generateSignature_missingHeader() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // given: rsaHolder returns a valid private key, but one header used to sign is missing from the request headers
        HttpPost httpRequest = new HttpPost( "http://test.domain.com/some/path" );
        Map<String, String> headers = new HashMap<>();
        String keyId = "a-key-id";
        doReturn( MockUtils.aPrivateKey() ).when( rsaHolder ).getPrivateKey();

        // when: generating the signature, then: a PluginException is thrown
        assertThrows( PluginException.class, () ->  natixisHttpClient.generateSignature( httpRequest, headers, keyId, "(request-target)", "SomeHeader" ) );
    }

    // --- Test NatixisHttpClient#get ---

    @Test
    void get_nominal(){
        // given: the client is authorized, the method inputs are correct, the partner API returns a valid response
        // mock authorize()
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );
        // mock generateSignature()
        doReturn( MockUtils.aSignature() ).when( spiedClient ).generateSignature( any(HttpRequestBase.class), anyMap(), anyString(), ArgumentMatchers.<String>any() );
        // mock execute()
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_OK, "OK", "some content", null );
        doReturn( stringResponse ).when( spiedClient ).execute( any(HttpGet.class) );

        // when: calling the paymentStatus method
        StringResponse response = spiedClient.get( "http://test.domain.com/service", MockUtils.aRequestConfiguration() );

        // then: the response is returned as-is
        assertEquals( stringResponse.getStatusCode(), response.getStatusCode() );
        assertEquals( stringResponse.getContent(), response.getContent() );
    }

    @Test
    void get_invalidUrl(){
        // given: the client is authorized and the given URL is invalid
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );
        String url = "https:||np.api.qua.natixis.com/hub-pisp/v1/someEndpoint";

        // when calling the get method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> spiedClient.get( url, MockUtils.aRequestConfiguration() ));
    }

    @Test
    void get_error(){
        // given: the client is authorized, the method inputs seem correct, but the partner API returns an error
        // mock authorize()
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );
        // mock generateSignature()
        doReturn( MockUtils.aSignature() ).when( spiedClient ).generateSignature( any(HttpRequestBase.class), anyMap(), anyString(), ArgumentMatchers.<String>any() );
        // mock execute()
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_NOT_FOUND, "Not Found", "{\"code\":\"404\",\"message\":\"Paiement recherché inexistant\"}", null);
        doReturn( stringResponse ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the get method, then: an exception is thrown
        assertThrows( PluginException.class, () -> spiedClient.get( "http://test.domaine.com/service", MockUtils.aRequestConfiguration() ) );
    }

    // --- Test NatixisHttpClient#isAuthorized ---

    @Test
    void isAuthorized_valid() throws NoSuchFieldException {
        // given: a valid authorization
        Authorization validAuth = MockUtils.anAuthorization();
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

    // --- Test NatixisHttpClient#handleErrorResponse ---

    // TODO

    // --- Test NatixisHttpClient#paymentInit ---

    @Test
    void paymentInit_nominal() {
        // given: the client is authorized, the method inputs are correct, the partner API returns a valid response
        // mock authorize()
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );
        // mock generateSignature()
        doReturn( MockUtils.aSignature() ).when( spiedClient ).generateSignature( any(HttpRequestBase.class), anyMap(), anyString(), ArgumentMatchers.<String>any() );
        // mock execute()
        String responseContent = "{\"appliedAuthenticationApproach\":\"REDIRECT\",\"_links\":{\"consentApproval\":{\"href\":\"https://www.rs-ex-hml-89c3api.qpa.bpce.fr/89C3api/accreditation/v1/identificationPisp?paymentRequestRessourceId="+MockUtils.aPaymentId()+"&nonce=E3BNDmkGVP4qtO1FLQZS\",\"templated\":true}}}";
        Map<String, String> headers = new HashMap<>();
        headers.put("location", "payment-requests/" + MockUtils.aPaymentId());
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_CREATED, "Created", responseContent, headers );
        doReturn( stringResponse ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the paymentInit method
        NatixisPaymentInitResponse response = spiedClient.paymentInit( MockUtils.aPayment(), MockUtils.aPsuInformation(), MockUtils.aRequestConfiguration() );

        // then:
        assertNotNull( response.getPaymentId() );
        assertNotNull( response.getContentApprovalUrl() );
    }

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
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );

        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https:||np.api.qua.natixis.com/hub-pisp/v1");
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( partnerConfigurationMap, new HashMap<>() ) );

        // when calling the paymentInit method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> spiedClient.paymentInit( MockUtils.aPayment(), MockUtils.aPsuInformation(), requestConfiguration ));
    }

    @Test
    void paymentInit_error() {
        // given: the client is authorized, the method inputs are correct, but the partner API returns an error
        // mock authorize()
        doReturn( MockUtils.anAuthorization() ).when( spiedClient ).authorize( any(RequestConfiguration.class) );
        // mock generateSignature()
        doReturn( MockUtils.aSignature() ).when( spiedClient ).generateSignature( any(HttpRequestBase.class), anyMap(), anyString(), ArgumentMatchers.<String>any() );
        // mock execute()
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_BAD_REQUEST, "Bad Request", "{\"message\":\"The object beneficiary.creditorAccount must not be null.\"}", null );
        doReturn( stringResponse ).when( spiedClient ).execute( any(HttpPost.class) );

        // when: calling the paymentInit method, then: an exception is thrown
        assertThrows( PluginException.class, () -> spiedClient.paymentInit( MockUtils.aPayment(), MockUtils.aPsuInformation(), MockUtils.aRequestConfiguration() ) );
    }

    // --- Test NatixisHttpClient#paymentStatus ---

    @Test
    void paymentStatus_nominal(){
        String paymentId = MockUtils.aPaymentId();

        // given: the partner API returns a valid response
        String responseContent = "{\"paymentRequest\":" +
                MockUtils.aPaymentBuilder()
                        .withResourceId( paymentId )
                        .build() +
                "}";
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_OK, "OK", responseContent, null );
        doReturn( stringResponse ).when( spiedClient ).get( anyString(), any(RequestConfiguration.class) );

        // when: calling the paymentStatus method
        Payment status = spiedClient.paymentStatus( paymentId, MockUtils.aRequestConfiguration() );

        // then:
        assertNotNull( status );
        assertEquals( paymentId, status.getResourceId() );
    }

    @Test
    void paymentStatus_missingApiUrl(){
        // given: the API base URL is missing from the partner configuration
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(), MockUtils.anEnvironment(), new PartnerConfiguration( new HashMap<>(), new HashMap<>() ) );

        // when calling the paymentStatus method, an exception is thrown
        assertThrows(InvalidDataException.class, () -> natixisHttpClient.paymentStatus( "1234567890", requestConfiguration ));
    }

    @Test
    void paymentStatus_parsingError(){
        String paymentId = MockUtils.aPaymentId();

        // given: the partner API returns a valid response
        String responseContent = MockUtils.aPaymentBuilder()
                .withResourceId( paymentId )
                .build()
                .toString();
        StringResponse stringResponse = HttpTestUtils.mockStringResponse(HttpStatus.SC_OK, "OK", responseContent, null );
        doReturn( stringResponse ).when( spiedClient ).get( anyString(), any(RequestConfiguration.class) );

        // when: calling the paymentStatus method
        assertThrows( PluginException.class, () -> spiedClient.paymentStatus( paymentId, MockUtils.aRequestConfiguration() ) );
    }

    // --- Test NatixisHttpClient#psuHeaders ---

    @Test
    void psuHeaders_full(){
        // given: a full PsuInformation object
        PsuInformation psuInformation = MockUtils.aPsuInformation();

        // when: calling psuHeaders method
        Map<String, String> headers = natixisHttpClient.psuHeaders( psuInformation );

        // then: one header is added for each attribute
        assertEquals( 12, headers.size() );
    }

    @Test
    void psuHeaders_partial(){
        // given: a partial PsuInformation object
        PsuInformation psuInformation = new PsuInformation.PsuInformationBuilder()
                .withIpAddress( "192.168.0.0" )
                .withHeaderUserAgent( "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0" )
                .build();

        // when: calling psuHeaders method
        Map<String, String> headers = natixisHttpClient.psuHeaders( psuInformation );

        // then: one header per non-null attribute
        assertEquals( 2, headers.size() );
    }

    // --- Test NatixisHttpClient#sha256DigestHeader ---

    // TODO

}