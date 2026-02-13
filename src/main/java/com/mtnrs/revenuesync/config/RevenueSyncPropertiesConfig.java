package com.mtnrs.revenuesync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        StripeConfig.class
})
public class RevenueSyncPropertiesConfig {}
