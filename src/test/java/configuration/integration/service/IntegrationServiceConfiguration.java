package configuration.integration.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({IntegrationPostServiceConfiguration.class, IntegrationPostFileServiceConfiguration.class})
public class IntegrationServiceConfiguration {


}
