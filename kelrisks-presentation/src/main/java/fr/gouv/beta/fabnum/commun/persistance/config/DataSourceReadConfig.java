package fr.gouv.beta.fabnum.commun.persistance.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		entityManagerFactoryRef = "db1EntityManagerFactory", 
				transactionManagerRef = "db1TransactionManager",
				basePackages = {
		"fr.gouv.beta.fabnum.kelrisks.persistance.referentiel" })
public class DataSourceReadConfig {
	
	@Primary
	@Bean(name = "db1DataSourceProperties")
	@ConfigurationProperties("database-read.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Primary
	@Bean(name = "db1DataSource")
	@ConfigurationProperties("database-read.datasource.configuration")
	public DataSource dataSource(@Qualifier("db1DataSourceProperties") DataSourceProperties db1DataSourceProperties, Environment env) {
		HikariDataSource ds = db1DataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class)
				.build();
		ds.setPoolName("pool-database-read");
		ds.setMaximumPoolSize(env.getProperty("database-read.datasource.maximum-pool-size", Integer.class));
		ds.setMinimumIdle(env.getProperty("database-read.datasource.minimum-idle", Integer.class));
		//semble faire saturer le pool BDD "rapidement"
		ds.setIdleTimeout(env.getProperty("database-read.datasource.idle-timeout", Long.class));
		ds.setConnectionTimeout(env.getProperty("database-read.datasource.connection-timeout", Long.class));

		ds.setReadOnly(true);
		return ds;
	}
	
	@Primary
	@Bean(name = "db1EntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			EntityManagerFactoryBuilder builder, @Qualifier("db1DataSource") DataSource db1DataSource) {
		return builder
				.dataSource(db1DataSource)
				.packages("fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities")
				.persistenceUnit("db-read")
				.build();
	}
	
	@Primary
	@Bean(name = "db1TransactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("db1EntityManagerFactory") EntityManagerFactory db1EntityManagerFactory) {
		return new JpaTransactionManager(db1EntityManagerFactory);
	}
	
}
