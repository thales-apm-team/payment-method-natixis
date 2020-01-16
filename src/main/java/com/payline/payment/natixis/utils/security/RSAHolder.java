package com.payline.payment.natixis.utils.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Holder for RSA security item : a chain of certificates and the private key associated to the client certificate.
 */
public class RSAHolder {

    private static final String CERT_SEPARATOR_BEGIN = "-----BEGIN CERTIFICATE-----";
    private static final String CERT_SEPARATOR_END = "-----END CERTIFICATE-----";

    private static final String PK_BEGIN = "-----BEGIN PRIVATE KEY-----";
    private static final String PK_END = "-----END PRIVATE KEY-----";

    private static final String PK_ALIAS = "pk";
    private static final char[] PK_PASSWORD = UUID.randomUUID().toString().toCharArray();

    private KeyStore keyStore;

    private RSAHolder(KeyStore keyStore ){
        this.keyStore = keyStore;
    }

    /**
     * Retrieve the client certificate, i.e. the first of the chain.
     * @return The client certificate
     * @throws KeyStoreException should not happen (the keystore will always be initialized at this point)
     */
    public Certificate getClientCertificate() throws KeyStoreException {
        return this.keyStore.getCertificate("0");
    }

    /**
     * Get the embedded keystore.
     * @return the keystore
     */
    public KeyStore getKeyStore(){
        return this.keyStore;
    }

    /**
     * Retrieve the client private key.
     * @return the private key
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException should not happen (the keystore will always be initialized at this point)
     */
    public Key getPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return this.keyStore.getKey(PK_ALIAS, PK_PASSWORD);
    }

    /**
     * Get the private key password. Useful when one needs to pass the full keystore to another object that will need
     * to use the private key.
     * @return the private key password
     */
    public char[] getPrivateKeyPassword() {
        return PK_PASSWORD;
    }


    public static final class RSAHolderBuilder {
        private Base64.Decoder decoder = Base64.getDecoder();

        private List<Certificate> chain;
        private PrivateKey privateKey;

        /**
         * Parse a PEM encoded string containing one or more certificates.
         *
         * @param pem The PEM string
         * @return the current builder
         * @throws CertificateException when an error occurs while rebuilding the certificate from a PEM string
         */
        public RSAHolderBuilder parseChain( String pem ) throws CertificateException {
            if( pem == null ){
                throw new IllegalStateException("PEM chain must not be null");
            }

            // Init the chain list
            this.chain = new ArrayList<>();

            // Split the chain into individual PEM certificates
            String rawChain = pem.replaceAll(CERT_SEPARATOR_BEGIN+"[\\r\\n]*", "")
                    .replaceAll(CERT_SEPARATOR_END+"[\\r\\n]*$", "")
                    .replaceAll("[\\r\\n]+", "");
            String[] strChain = rawChain.split( CERT_SEPARATOR_END );

            // Rebuild each certificate from its PEM format and add it to the chain
            for( String pemCert : strChain ){
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certificateFactory.generateCertificate(new ByteArrayInputStream(decoder.decode(pemCert)));
                this.chain.add( cert );
            }

            return this;
        }

        public RSAHolderBuilder withChain( List<Certificate> chain ){
            this.chain = chain;
            return this;
        }

        /**
         * Parse a PEM encoded string containing a private key.
         *
         * @param pem the PEM string
         * @return the current builder
         * @throws InvalidKeySpecException if the private key is not encoded with the PKCS #8 standard
         * @throws NoSuchAlgorithmException should not happen as "RSA" has been tested as a valid algorithm
         */
        public RSAHolderBuilder parsePrivateKey( String pem ) throws InvalidKeySpecException, NoSuchAlgorithmException {
            if( pem == null ){
                throw new IllegalStateException("PEM private key must not be null");
            }

            String pemKey = pem.replace(PK_BEGIN, "")
                    .replace(PK_END, "")
                    .replaceAll("[\\r\\n]+", "");

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoder.decode(pemKey));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            this.privateKey = factory.generatePrivate(spec);

            return this;
        }

        public RSAHolderBuilder withPrivateKey( PrivateKey privateKey ){
            this.privateKey = privateKey;
            return this;
        }

        /**
         * Build the RSAHolder with the embedded private key and chain of certificates.
         *
         * @return An instance of {@link RSAHolder}
         * @throws KeyStoreException should not happen ("JKS" has been tested as a valid algorithm, and the keystore is always initialized before trying to add a certificate or a private key to it)
         * @throws CertificateException should not happen (keystore is not loaded, it's created from scratch)
         * @throws NoSuchAlgorithmException should not happen (keystore is not loaded, it's created from scratch)
         * @throws IOException should not happen (keystore is not loaded, it's created from scratch)
         */
        public RSAHolder build() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
            if( chain == null || chain.isEmpty() ){
                throw new IllegalStateException("RSAHolder must have a certificate chain when built");
            }
            if( privateKey == null ){
                throw new IllegalStateException("RSAHolder must have a private key when built");
            }

            // Create an empty keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null);

            // Add each certificate to the keystore
            for( int i=0; i<chain.size(); i++ ){
                keyStore.setCertificateEntry(Integer.toString(i), chain.get(i));
            }

            // Add private key to the keystore
            keyStore.setKeyEntry(PK_ALIAS, privateKey, PK_PASSWORD, chain.toArray(new Certificate[0]) );

            return new RSAHolder( keyStore );
        }
    }
}
