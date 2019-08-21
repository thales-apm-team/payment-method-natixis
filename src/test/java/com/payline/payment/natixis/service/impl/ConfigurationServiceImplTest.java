package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.MockUtils;
import com.payline.payment.natixis.bean.configuration.RequestConfiguration;
import com.payline.payment.natixis.exception.PluginException;
import com.payline.payment.natixis.utils.http.NatixisHttpClient;
import com.payline.payment.natixis.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfigurationServiceImplTest {

    /* I18nService is not mocked here, on purpose, to validate the existence of all
    the messages related to this class, at least in the default locale */
    @Mock private NatixisHttpClient natixisHttpClient;
    @Mock private ReleaseProperties releaseProperties;


    @InjectMocks
    private ConfigurationServiceImpl service;

    @BeforeEach
    void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void check_nominal(){
        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();
        doReturn( MockUtils.anAuthorization() ).when( natixisHttpClient ).authorize( any(RequestConfiguration.class) );

        // when: checking the configuration
        Map<String, String> errors = service.check( checkRequest );

        // then: error map is empty
        assertTrue( errors.isEmpty() );
    }

    @Test
    void check_emptyAccountInfo(){
        // given: an empty accountInfo
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(new HashMap<>())
                .build();

        // when: checking the configuration
        Map<String, String> errors = service.check( checkRequest );

        // then: there is an error for each parameter, each error has a valid message and authorize method is never called
        assertEquals(service.getParameters( Locale.getDefault() ).size(), errors.size() );
        for( Map.Entry<String, String> error : errors.entrySet() ){
            assertNotNull( error.getValue() );
            assertFalse( error.getValue().contains("???") );
        }
        verify( natixisHttpClient, never() ).authorize( any( RequestConfiguration.class ) );
    }

    @Test
    void check_wrongAuthorization(){
        // given: the client ID or secret is wrong. The partner authorization API returns an error.
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();
        doThrow( PluginException.class ).when( natixisHttpClient ).authorize( any(RequestConfiguration.class) );

        // when: checking the configuration
        Map<String, String> errors = service.check( checkRequest );

        // then: no exception is thrown, but there are some errors
        assertTrue( errors.size() > 0 );
    }

    @Test
    void getName(){
        // when: calling the method getName
        String name = service.getName( Locale.getDefault() );

        // then: the method returns the name
        assertNotNull( name );
    }

    @Test
    void getParameters() {
        // when: retrieving the contract parameters
        List<AbstractParameter> parameters = service.getParameters( Locale.getDefault() );

        // then: each parameter has a unique key, a label and a description. List box parameters have at least 1 possible value.
        List<String> keys = new ArrayList<>();
        for( AbstractParameter param : parameters ){
            // 2 different parameters should not have the same key
            assertFalse( keys.contains( param.getKey() ) );
            keys.add( param.getKey() );

            // each parameter should have a label and a description
            assertNotNull( param.getLabel() );
            assertFalse( param.getLabel().contains("???") );
            assertNotNull( param.getDescription() );
            assertFalse( param.getDescription().contains("???") );

            // in case of a ListBoxParameter, it should have at least 1 value
            if( param instanceof ListBoxParameter ){
                assertFalse( ((ListBoxParameter) param).getList().isEmpty() );
            }
        }
    }

    @Test
    void getReleaseInformation(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn( version ).when( releaseProperties ).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        doReturn( formatter.format( cal.getTime() ) ).when( releaseProperties ).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

}
