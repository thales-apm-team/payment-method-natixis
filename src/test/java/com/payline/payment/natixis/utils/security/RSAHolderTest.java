package com.payline.payment.natixis.utils.security;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.utils.Constants;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class RSAHolderTest {

    private static String pemCertificate;
    private static String pemPk;

    @BeforeAll
    static void setup() {
        PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        pemCertificate = partnerConfiguration.getProperty(Constants.PartnerConfigurationKeys.CLIENT_CERTIFICATE);
        pemPk = partnerConfiguration.getProperty(Constants.PartnerConfigurationKeys.CLIENT_PRIVATE_KEY);
    }

    @Test
    void builder_uniqueClientCertificate() throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
        // given: a valid chain containing only one certificate and private key
        String chain = pemCertificate;
        String pk = pemPk;

        // when: building a RSAHolder
        RSAHolder rsaHolder = new RSAHolder.RSAHolderBuilder()
                .parseChain(chain)
                .parsePrivateKey(pk)
                .build();

        // then: the keystore contains the corresponding entries
        assertNotNull(rsaHolder.getKeyStore());
        assertEquals(2, rsaHolder.getKeyStore().size());
        assertNotNull(rsaHolder.getPrivateKey());
        assertNotNull(rsaHolder.getClientCertificate());
        assertNotNull(rsaHolder.getPrivateKeyPassword());
    }

    @Test
    void builder_multipleCertChain() throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, KeyStoreException {
        // given: a valid chain containing two certificates and private key
        String chain = pemCertificate + System.lineSeparator() + pemCertificate;
        String pk = pemPk;

        // when: building a RSAHolder
        RSAHolder rsaHolder = new RSAHolder.RSAHolderBuilder()
                .parseChain(chain)
                .parsePrivateKey(pk)
                .build();

        // then: the keystore contains 2 certificates and 1 private key, so 3 entries
        assertNotNull(rsaHolder.getKeyStore());
        assertEquals(3, rsaHolder.getKeyStore().size());
    }

    @Test
    void builder_nullChain() {
        // given: a null certificate
        String chain = null;

        // when: trying to parse a null chain, then an exception is thrown
        assertThrows( IllegalStateException.class, () -> new RSAHolder.RSAHolderBuilder()
                .parseChain( chain )
                .build() );
    }

    @Test
    void builder_nullPk() {
        // given: a null private key
        String pk = null;

        // when: trying to parse a null chain, then an exception is thrown
        assertThrows( IllegalStateException.class, () -> new RSAHolder.RSAHolderBuilder()
                .parsePrivateKey( pk )
                .build() );
    }

    @Test
    void builder_buildWithoutChain() {
        // given: a valid chain containing only one certificate and private key
        String chain = pemCertificate;
        String pk = pemPk;

        // when: building a RSAHolder, omitting the chain, then an exception is thrown
        assertThrows( IllegalStateException.class, () -> new RSAHolder.RSAHolderBuilder()
                .parsePrivateKey(pk)
                .build() );
    }

    @Test
    void builder_buildWithoutPk() {
        // given: a valid chain containing only one certificate and private key
        String chain = pemCertificate;
        String pk = pemPk;

        // when: building a RSAHolder, omitting the chain, then an exception is thrown
        assertThrows( IllegalStateException.class, () -> new RSAHolder.RSAHolderBuilder()
                .parseChain(chain)
                .build() );
    }
}