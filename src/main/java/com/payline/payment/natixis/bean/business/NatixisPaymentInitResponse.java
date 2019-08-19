package com.payline.payment.natixis.bean.business;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NatixisPaymentInitResponse {

    private static final Logger LOGGER = LogManager.getLogger(NatixisPaymentInitResponse.class);

    private String paymentId;
    private String contentApprovalUrl;

    private NatixisPaymentInitResponse(String paymentId, String contentApprovalUrl) {
        this.paymentId = paymentId;
        this.contentApprovalUrl = contentApprovalUrl;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getContentApprovalUrl() {
        return contentApprovalUrl;
    }

    public static NatixisPaymentInitResponse fromStringResponse(StringResponse response ){
        // Extract paymentId from 'Location' header
        String locationHeader = response.getHeader("Location");
        if( locationHeader == null ){
            throw new PluginException("Plugin error: no 'Location' header in response", FailureCause.PARTNER_UNKNOWN_ERROR);
        }
        Pattern p = Pattern.compile("[0-9-]+$");
        Matcher m = p.matcher( locationHeader );
        if( !m.find() ){
            throw new PluginException("Plugin error: unable to extract paymentId");
        }
        String paymentId = m.group(0);

        // Extract consent approval URL from the response JSON body
        String contentApprovalUrl;
        try {
            JsonObject body = new JsonParser().parse(response.getContent()).getAsJsonObject();
            contentApprovalUrl = body.getAsJsonObject("_links")
                    .getAsJsonObject("consentApproval")
                    .get("href").getAsString();
        }
        catch( RuntimeException e ){
            LOGGER.error("Unable to parse the response body to extract the redirection URL: {}",
                            System.lineSeparator() + response.getContent() );
            throw new PluginException("Plugin error: unable to extract redirection URL");
        }

        return new NatixisPaymentInitResponse(paymentId, contentApprovalUrl);
    }

}
