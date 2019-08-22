package com.payline.payment.natixis.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @BeforeEach
    void setup(){
        service = new PaymentServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @MethodSource("amounts")
    void formatAmount(BigInteger input, String expectedOutput ){
        assertEquals( expectedOutput, service.formatAmount( input ));
    }
    static Stream<Arguments> amounts(){
        return Stream.of(
                Arguments.of( BigInteger.valueOf( 80 ), "0.80" ),
                Arguments.of( BigInteger.valueOf( 100 ), "1.00" ),
                Arguments.of( BigInteger.valueOf( 1599 ), "15.99" )
        );
    }
}
