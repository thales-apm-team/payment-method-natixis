import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;
import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.tomitribe.auth.signatures.Algorithm.RSA_SHA256;

public class Helper {

    private static final Logger LOGGER = LogManager.getLogger(Helper.class);

    private static final String PAYMENT_REQUEST_PATH = "/payment-requests";
    private static final String PAYMENT_STATUS_PATH = "/payment-requests/{paymentRequestResourceId}";

    private static String accessToken(){
        return "7B5cCVcHT6eNHg8RvMr6o9vWlv7wETnV3GDgCiy7J1mDUEZ2qFzeEs";
    }

    public static void main( String[] args ) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, URISyntaxException {
        // Configuration (partner / contract)
        String keyStorePath = System.getProperty("javax.net.ssl.keyStore");
        String keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
        char[] passwd = System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
        String privateKeyAlias = "1";
        String paymentsApiBaseUrl = "https://np.api.qua.natixis.com/hub-pisp/v1";

        // Load the keystore and recover elements (private key, certificate, serial number, CA, etc.
        // @see https://www.baeldung.com/java-keystore
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(new FileInputStream(keyStorePath), passwd);
        Key key = ks.getKey(privateKeyAlias, passwd);
        X509Certificate cert = (X509Certificate) ks.getCertificate(privateKeyAlias);

        // Request URI & method
        String post = "POST";
        URI requestUriPost = new URI( paymentsApiBaseUrl + PAYMENT_REQUEST_PATH );
        String get = "GET";
        URI requestUriGet = new URI( paymentsApiBaseUrl + PAYMENT_STATUS_PATH.replace("{paymentRequestResourceId}", "0000000556-156352853200013807958897") );

        // Request body sample
        //SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        //SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        //String current = isoDateFormat.format(new Date());
        //String uniqueId = "MONEXT" + timestampDateFormat.format(new Date());
        String jsonBody = "{\"paymentInformationId\":\"MONEXT20190805135238\",\"creationDateTime\":\"2019-08-05T13:52:38.116+02:00\",\"numberOfTransactions\":1,\"initiatingParty\":{\"name\":\"NATIXIS PAYMENT SOLUTIONS\",\"postalAddress\":{\"addressLine\":[\"30 AVENUE PIERRE MENDES FRANCE\",\"75013 PARIS\"],\"country\":\"FR\"},\"organisationId\":{\"identification\":\"15930\",\"schemeName\":\"COID\",\"issuer\":\"ACPR\"}},\"paymentTypeInformation\":{\"serviceLevel\":\"SEPA\",\"localInstrument\":\"INST\",\"categoryPurpose\":\"DVPM\"},\"debtor\":{\"name\":\"Jean Dupont\",\"postalAddress\":{\"addressLine\":[\"25 rue de la petite taverne\",\"74120 Megève\"],\"country\":\"FR\"},\"privateId\":{\"identification\":\"123456753\",\"schemeName\":\"BANK\",\"issuer\":\"CCBPFRPPNAN\"}},\"debtorAgent\":{\"bicFi\":\"CMBRFR2BARK\"},\"beneficiary\":{\"creditorAgent\":{\"bicFi\":\"CCBPFRPPNAN\"},\"creditor\":{\"name\":\"Marie Durand\",\"postalAddress\":{\"addressLine\":[\"8 rue pavée Andouilles\",\"71460 Saint-Gengoux-le-national\"],\"country\":\"FR\"}},\"creditorAccount\":{\"iban\":\"FR7613807000343142150215863\"}},\"purpose\":\"COMC\",\"chargeBearer\":\"SLEV\",\"requestedExecutionDate\":\"2019-08-06T13:52:38.119+02:00\",\"creditTransferTransaction\":[{\"paymentId\":{\"instructionId\":\"MONEXT20190805135238\",\"endToEndId\":\"MONEXT20190805135238\"},\"instructedAmount\":{\"amount\":\"150\",\"currency\":\"EUR\"},\"remittanceInformation\":[\"Argent de poche\"]}],\"supplementaryData\":{\"successfulReportUrl\":\"https://www.successful.fr\",\"unsuccessfulReportUrl\":\"https://www.unsuccessful.fr\"}}";
        String digestBody = jsonBody.replaceAll("[\\n\\r]+[ ]*", "")
                .replace(": ", ":")
                .replace(", ", ",");

        // Request headers
        Map<String, String> headersPost = new HashMap<>();
        headersPost.put("Authorization", "Bearer " + accessToken());
        headersPost.put("X-Request-ID", UUID.randomUUID().toString());
        headersPost.put("Content-Length",  Integer.toString(digestBody.length()));
        headersPost.put("Content-Type", "application/json");
        Map<String, String> headersGet = new HashMap<>();
        headersGet.put("Authorization", "Bearer " + accessToken());
        headersGet.put("X-Request-ID", UUID.randomUUID().toString());

        // Create digest
        byte[] digest = MessageDigest.getInstance("SHA-256").digest( digestBody.getBytes() );
        String digestHeader = "SHA-256=" + new String(Base64.encodeBase64(digest));
        headersPost.put("Digest", digestHeader);

        // Create the keyId
        // @see Specification_API_HUB_PISP2.0.docx
        String sn = cert.getSerialNumber().toString(16);
        String caDn = cert.getIssuerDN().getName();
        //String keyId = "SN="+sn+",CA="+caDn;
        String keyId = "sign-qua-monext";

        // POST
        System.out.println(post + " " + requestUriPost);
        for( Map.Entry<String,String> entry : headersPost.entrySet() ){
            System.out.println( entry.getKey() + ": " + entry.getValue() );
        }

        // Create a signer
        // @see https://github.com/tomitribe/http-signatures-java
        Signature signature = new Signature(keyId, RSA_SHA256, null, "(request-target)", "X-Request-ID", "digest");
        Signer signer = new Signer(key, signature);

        // Sign the HTTP message
        // @see https://github.com/tomitribe/http-signatures-java
        signature = signer.sign(post, requestUriPost.getPath(), headersPost);
        System.out.println("Signature POST: " + signature.toString());

        System.out.println("\n"+jsonBody);

        // GET
        System.out.println("\n" + get + " " + requestUriGet);
        for( Map.Entry<String,String> entry : headersGet.entrySet() ){
            System.out.println( entry.getKey() + ": " + entry.getValue() );
        }

        signature = new Signature(keyId, RSA_SHA256, null, "(request-target)", "X-Request-ID");
        signer = new Signer(key, signature);
        signature = signer.sign(get, requestUriGet.getPath(), headersGet);
        System.out.println("Signature GET: " + signature.toString());
    }

    private static Date add( Date to, int days ){
        Calendar cal = Calendar.getInstance();
        cal.setTime( to );
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

}
