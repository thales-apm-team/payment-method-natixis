package com.payline.payment.natixis.service.impl;

import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.TransactionManagerService;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class TransactionManagerServiceImpl implements TransactionManagerService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionManagerServiceImpl.class);

    @Override
    public Map<String, String> readAdditionalData(String s, String s1) {
        // TODO
        return null;
    }

}
