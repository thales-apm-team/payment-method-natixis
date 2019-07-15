package com.payline.payment.natixis.bean.business;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NatixisPaymentInitResponse {

    private static final Logger LOGGER = LogManager.getLogger(NatixisPaymentInitResponse.class);

    private URL contentApprovalUrl;
    private String paymentId;
    private String statusCode;

    public NatixisPaymentInitResponse(URL contentApprovalUrl, String paymentId, String statusCode) {
        this.contentApprovalUrl = contentApprovalUrl;
        this.paymentId = paymentId;
        this.statusCode = statusCode;
    }

    public URL getContentApprovalUrl() {
        return contentApprovalUrl;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public static NatixisPaymentInitResponse fromStringResponse(StringResponse response ){
        String statusCode = response.getStatusCode() + " " + response.getStatusMessage();

        // Extract paymentId from 'Location' header
        String locationHeader = response.getHeader("Location");
        if( locationHeader == null ){
            throw new PluginException("Plugin error: no 'Location' header in response", FailureCause.PARTNER_UNKNOWN_ERROR);
        }
        Pattern p = Pattern.compile("/([a-z0-9-]+)$");
        Matcher m = p.matcher( locationHeader );
        if( !m.find() ){
            throw new PluginException("Plugin error: unable to extract paymentId");
        }
        String paymentId = m.group(1);

        // Extract consent approval URL from the response JSON body
        URL contentApprovalUrl;
        try {
            JsonObject body = new JsonParser().parse(response.getContent()).getAsJsonObject();
            String strUrl = body.getAsJsonObject("_links")
                    .getAsJsonObject("consentApproval")
                    .get("href").getAsString();
            contentApprovalUrl = new URL( strUrl );
        }
        catch (MalformedURLException e) {
            throw new PluginException("Plugin error: malformed redirection url", FailureCause.PARTNER_UNKNOWN_ERROR);
        }
        catch( RuntimeException e ){
            LOGGER.error("Unable to parse the response body to extract the redirection URL: {}",
                            System.lineSeparator() + response.getContent() );
            throw new PluginException("Plugin error: unable to extract redirection URL");
        }

        return new NatixisPaymentInitResponse(contentApprovalUrl, paymentId, statusCode);
    }

}
