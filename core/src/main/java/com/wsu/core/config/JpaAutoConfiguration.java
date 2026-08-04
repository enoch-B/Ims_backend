package com.wsu.core.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@ConditionalOnClass
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@PropertySource("classpath:core.properties")
public class JpaAutoConfiguration {

    private static final String[] ENTITY_PACKAGES = {"com.wsu.core.model.entities"};

    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;

    @Value("${db.host}")
    private String databaseHost;
    @Value("${db.port}")
    private String databasePort;
    @Value("${db.name}")
    private String databaseName;
    @Value("${db.user}")
    private String databaseUser;
    @Value("${db.schema}")
    private String databaseSchema;
    @Value("${db.password}")
    private String databasePassword;

    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "spring.jpa.properties.hibernate.dialect";

    // hikari config
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private Integer maxPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle}")
    private Integer minIdleSize;
    @Value("${spring.datasource.hikari.idle-timeout}")
    private Integer idleTimeout;
    @Value("${spring.datasource.hikari.pool-name}")
    private String poolName;
    @Value("${spring.datasource.hikari.max-lifetime}")
    private Integer maxLifetime;
    @Value("${spring.datasource.hikari.connection-timeout}")
    private Integer connectionTimeout;


    /**
     * Creates and configures the HikariCP datasource bean.
     *
     * @return DataSource
     */
    @Bean
    DataSource dataSource() {

        log.info("--> jpa auto config...");

        final String url = String.format("jdbc:postgresql://%s:%s/%s", databaseHost, databasePort, databaseName);

        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(driverClassName);
        dataSourceConfig.setJdbcUrl(url);
        dataSourceConfig.setSchema(databaseSchema);
        dataSourceConfig.setUsername(databaseUser);
        dataSourceConfig.setPassword(databasePassword);
        dataSourceConfig.setMaximumPoolSize(this.maxPoolSize);
        dataSourceConfig.setMinimumIdle(this.minIdleSize);
        dataSourceConfig.setIdleTimeout(this.idleTimeout);
        dataSourceConfig.setPoolName(this.poolName);
        dataSourceConfig.setMaxLifetime(this.maxLifetime);
        dataSourceConfig.setConnectionTimeout(this.connectionTimeout);


        return new HikariDataSource(dataSourceConfig);
    }

    /**
     * Creates the bean that creates the JPA entity manager factory.
     *
     * @param dataSource The datasource that provides the database connections.
     * @param env        The runtime environment of  our application.
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);

        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan(ENTITY_PACKAGES);

        Properties jpaProperties = new Properties();

        //Configures the used database dialect. This allows To Hibernate to create SQL
        //that is optimized for the used database.
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_DIALECT, "org.hibernate.dialect.PostgreSQLDialect");


        // additional properties
        if (additionalProperties() != null) {
            entityManagerFactoryBean.setJpaProperties(additionalProperties());
        } else {
            entityManagerFactoryBean.setJpaProperties(jpaProperties);
        }

        // base packages to scan
        entityManagerFactoryBean.setPackagesToScan("com.wsu");

        return entityManagerFactoryBean;
    }


    @ConditionalOnResource(resources = "classpath:core.properties")
    Properties additionalProperties() {

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.show_sql", "false");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "none");
        hibernateProperties.setProperty("hibernate.default_schema", databaseSchema);


        return hibernateProperties;

    }

    /**
     * Creates the transaction manager bean that integrates the used JPA provider with the
     * Spring transaction mechanism.
     *
     * @param entityManagerFactory The used JPA entity manager factory.
     * @return JpaTransactionManager
     */
    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }


}