package com.xuyouyu.harness.service;

import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

final class TaskRunServiceTestFactory {
    private TaskRunServiceTestFactory() {
    }

    static TaskRunService create() {
        return create("jdbc:h2:mem:run-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    }

    static TaskRunService create(String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        return new TaskRunService(new JdbcTemplate(dataSource));
    }
}
