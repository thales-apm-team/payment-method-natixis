package com.payline.payment.natixis.exception;

import com.payline.pmapi.bean.common.FailureCause;

/* This is an example of how to override PluginException for a recurring use.
TODO: Remove it it's useless in the end. Or add a real Javadoc if it's useful.
 */
public class HttpException extends PluginException {

    public HttpException( String message ) {
        super( message, FailureCause.COMMUNICATION_ERROR );
    }

    public HttpException( String message, Exception cause ) {
        super( message, FailureCause.COMMUNICATION_ERROR, cause );
    }

}
