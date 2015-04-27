package org.jeffklein.turfwars.codes.backend.config;

import org.jeffklein.turfwars.codes.client.TurfWarsApiClient;
import org.jeffklein.turfwars.codes.dataaccess.config.HibernateConfiguration;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TODO: javadoc needed
 *
 * @author jeffklein
 */
@Configuration
@EnableScheduling
@Import(HibernateConfiguration.class)
public class SpringConfiguration {
    @Bean
    public TurfWarsApiClient getTurfWarsApiClient() {
        return new TurfWarsApiClient();
    }

    @Bean
    public TempCodeService tempCodeService() {
        return new TempCodeServiceImpl();
    }
}
