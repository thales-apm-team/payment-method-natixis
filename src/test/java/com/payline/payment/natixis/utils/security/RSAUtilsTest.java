package com.payline.payment.natixis.utils.security;

import com.payline.payment.natixis.exception.PluginException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.Provider;
import java.security.Security;

class RSAUtilsTest {
    private RSAUtils rsaUtils = RSAUtils.getInstance();

    @Test
    void encryptAndDecryptTest() {
        System.out.println(System.getProperty("java.version"));
        for (Provider provider : Security.getProviders())
            System.out.println(provider);


        String text = "I am a plain text message";

        String key = rsaUtils.generateKey();
        String enc = rsaUtils.encrypt(text, key);
        String dec = rsaUtils.decrypt(enc, key);

        Assertions.assertEquals(text, dec);
    }

    @Test
    void encryptFailureTest() {
        Assertions.assertThrows(PluginException.class, () -> rsaUtils.encrypt("message", "badKey"));
    }

    @Test
    void decryptFailureTest() {
        Assertions.assertThrows(PluginException.class, () -> rsaUtils.decrypt("message", "badKey"));
    }
}