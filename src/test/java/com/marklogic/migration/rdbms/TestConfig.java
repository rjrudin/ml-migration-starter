package com.marklogic.migration.rdbms;

import com.marklogic.junit.spring.BasicTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"file:gradle.properties", "file:gradle-local.properties"}, ignoreResourceNotFound = true)
public class TestConfig extends BasicTestConfig {
}