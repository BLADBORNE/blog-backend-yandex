package ru.blog.backend.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
    "ru.blog.backend.repository",
    "ru.blog.backend.service"
})
@Import(value = {
    DataSourceConfiguration.class,
    TransactionalConfiguration.class
})
public class RootConfiguration {

}
