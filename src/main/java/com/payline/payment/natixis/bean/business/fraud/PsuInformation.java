package com.payline.payment.natixis.bean.business.fraud;

import java.util.Date;

public class PsuInformation {

    /**
     * Date and time of the most relevant PSU’s terminal request to the PISP.
     */
    private Date lastLogin;

    /**
     * IP Address of the PSU terminal when connecting to the PISP.
     */
    private String ipAddress;

    /**
     * IP Port of the PSU terminal when connecting to the PISP.
     */
    private Integer ipPort;

    /**
     * HTTP Method used for the most relevant PSU’s terminal request to the PISP.
     */
    private String httpMethod;

    /**
     * "User-Agent" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerUserAgent;

    /**
     * "Referer" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerReferer;

    /**
     * "Accept" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerAccept;

    /**
     * "Accept-Charset" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerAcceptCharset;

    /**
     * "Accept-Encoding" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerAcceptEncoding;

    /**
     * "Accept-Language" header field sent by the PSU terminal when connecting to the PISP.
     */
    private String headerAcceptLanguage;

    /**
     * The forwarded Geo Location of the corresponding HTTP request between PSU and TPP.
     */
    private String geoLocation;

    /**
     * UUID (Universally Unique Identifier) for a device, which is used by the PSU, if available.
     */
    private String deviceId;

    private PsuInformation(PsuInformationBuilder builder) {
        this.lastLogin = builder.lastLogin;
        this.ipAddress = builder.ipAddress;
        this.ipPort = builder.ipPort;
        this.httpMethod = builder.httpMethod;
        this.headerUserAgent = builder.headerUserAgent;
        this.headerReferer = builder.headerReferer;
        this.headerAccept = builder.headerAccept;
        this.headerAcceptCharset = builder.headerAcceptCharset;
        this.headerAcceptEncoding = builder.headerAcceptEncoding;
        this.headerAcceptLanguage = builder.headerAcceptLanguage;
        this.geoLocation = builder.geoLocation;
        this.deviceId = builder.deviceId;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getIpPort() {
        return ipPort;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getHeaderUserAgent() {
        return headerUserAgent;
    }

    public String getHeaderReferer() {
        return headerReferer;
    }

    public String getHeaderAccept() {
        return headerAccept;
    }

    public String getHeaderAcceptCharset() {
        return headerAcceptCharset;
    }

    public String getHeaderAcceptEncoding() {
        return headerAcceptEncoding;
    }

    public String getHeaderAcceptLanguage() {
        return headerAcceptLanguage;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public String getDeviceId() {
        return deviceId;
    }


    public static final class PsuInformationBuilder {
        private Date lastLogin;
        private String ipAddress;
        private Integer ipPort;
        private String httpMethod;
        private String headerUserAgent;
        private String headerReferer;
        private String headerAccept;
        private String headerAcceptCharset;
        private String headerAcceptEncoding;
        private String headerAcceptLanguage;
        private String geoLocation;
        private String deviceId;

        public PsuInformationBuilder withLastLogin(Date lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public PsuInformationBuilder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public PsuInformationBuilder withIpPort(Integer ipPort) {
            this.ipPort = ipPort;
            return this;
        }

        public PsuInformationBuilder withHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public PsuInformationBuilder withHeaderUserAgent(String headerUserAgent) {
            this.headerUserAgent = headerUserAgent;
            return this;
        }

        public PsuInformationBuilder withHeaderReferer(String headerReferer) {
            this.headerReferer = headerReferer;
            return this;
        }

        public PsuInformationBuilder withHeaderAccept(String headerAccept) {
            this.headerAccept = headerAccept;
            return this;
        }

        public PsuInformationBuilder withHeaderAcceptCharset(String headerAcceptCharset) {
            this.headerAcceptCharset = headerAcceptCharset;
            return this;
        }

        public PsuInformationBuilder withHeaderAcceptEncoding(String headerAcceptEncoding) {
            this.headerAcceptEncoding = headerAcceptEncoding;
            return this;
        }

        public PsuInformationBuilder withHeaderAcceptLanguage(String headerAcceptLanguage) {
            this.headerAcceptLanguage = headerAcceptLanguage;
            return this;
        }

        public PsuInformationBuilder withGeoLocation(String geoLocation) {
            this.geoLocation = geoLocation;
            return this;
        }

        public PsuInformationBuilder withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public PsuInformation build() {
            return new PsuInformation( this );
        }
    }
}
