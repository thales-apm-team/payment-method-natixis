package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

class ConfigurationServiceImplTest {

    @Mock
    private ReleaseProperties releaseProperties;

    @InjectMocks
    private ConfigurationServiceImpl service;

    @BeforeEach
    void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getReleaseInformation_nominal(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn( version ).when( releaseProperties ).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        doReturn( formatter.format( cal.getTime() ) ).when( releaseProperties ).get("release.date");

        // then: when calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

}
