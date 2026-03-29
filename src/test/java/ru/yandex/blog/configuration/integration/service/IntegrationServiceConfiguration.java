package ru.yandex.blog.configuration.integration.service;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"ru.yandex.blog.repository", "ru.yandex.blog.service"})
@EnableAutoConfiguration
public class IntegrationServiceConfiguration {

}
