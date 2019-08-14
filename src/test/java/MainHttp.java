import com.payline.payment.natixis.bean.business.payment.Payment;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.utils.Constants;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainHttp {

    private static NatixisHttpClient natixisHttpClient = NatixisHttpClient.getInstance();

    public static void main( String[] args ) throws IOException {
        /*
        String keyStorePath = System.getProperty("tmp.keyStore");
        String keyStoreType = System.getProperty("tmp.keyStoreType");
        char[] keyStorePassword = System.getProperty("tmp.keyStorePassword").toCharArray();
        KeyStore ref = KeyStore.getInstance(keyStoreType);
        ref.load(new FileInputStream(keyStorePath), keyStorePassword);
        */

        PartnerConfiguration partnerConfiguration = initPartnerConfiguration();
        RequestConfiguration requestConfiguration = new RequestConfiguration(initContractConfiguration(), MockUtils.anEnvironment(), partnerConfiguration);

        try {
            natixisHttpClient.init( partnerConfiguration );
            natixisHttpClient.paymentInit( initPayment(), MockUtils.aPsuInformation(), requestConfiguration );
            //natixisHttpClient.paymentStatus("0000000556-156352853200013807958897", requestConfiguration);
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    private static ContractConfiguration initContractConfiguration(){
        ContractConfiguration contractConfiguration = new ContractConfiguration("Natixis", new HashMap<>());

        contractConfiguration.getContractProperties().put(Constants.ContractConfigurationKeys.CLIENT_ID, new ContractProperty( System.getProperty("project.clientId") ));
        contractConfiguration.getContractProperties().put(Constants.ContractConfigurationKeys.CLIENT_SECRET, new ContractProperty( System.getProperty("project.clientSecret") ));

        return contractConfiguration;
    }

    private static PartnerConfiguration initPartnerConfiguration() throws IOException {
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_AUTH_BASE_URL, "https://np-auth.api.qua.natixis.com/api");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.API_PAYMENT_BASE_URL, "https://np.api.qua.natixis.com/hub-pisp/v1");
        partnerConfigurationMap.put(Constants.PartnerConfigurationKeys.SIGNATURE_KEYID, "sign-qua-monext");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE, new String(Files.readAllBytes(Paths.get(System.getProperty("project.certificateChainPath")))) );
        sensitiveConfigurationMap.put( Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY, new String(Files.readAllBytes(Paths.get(System.getProperty("project.pkPath")))) );

        return new PartnerConfiguration( partnerConfigurationMap, sensitiveConfigurationMap );
    }

    private static Payment initPayment(){
        return MockUtils.aPayment();
    }

    private static Date add( Date to, int days ){
        Calendar cal = Calendar.getInstance();
        cal.setTime( to );
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
}
