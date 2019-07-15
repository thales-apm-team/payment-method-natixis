package com.payline.payment.natixis.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionManagerServiceImplTest {

    @InjectMocks
    private TransactionManagerServiceImpl service;

    @BeforeEach
    void setup(){
        service = new TransactionManagerServiceImpl();
        MockitoAnnotations.initMocks( this );
    }

    @Test
    void readAdditionalData(){
        // when: calling method readAdditionalData with any string inputs
        Map<String, String> result = service.readAdditionalData("", "");

        // then: the result must not be null
        assertNotNull( result );
    }

}
