package com.payline.payment.natixis.service.impl;

import com.payline.payment.natixis.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);

    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // TODO

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        // TODO
        return null;
    }

    @Override
    public ReleaseInformation getReleaseInformation(){
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate( LocalDate.parse(releaseProperties.get("release.date"), DateTimeFormatter.ofPattern("dd/MM/yyyy")) )
                .withVersion( releaseProperties.get("release.version") )
                .build();
    }

    @Override
    public String getName(Locale locale) {
        // TODO
        return null;
    }
}
