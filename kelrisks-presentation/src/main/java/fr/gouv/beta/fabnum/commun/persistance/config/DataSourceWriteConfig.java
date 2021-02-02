package fr.gouv.beta.fabnum.commun.persistance.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
		entityManagerFactoryRef = "db2EntityManagerFactory", 
				transactionManagerRef = "db2TransactionManager",
				basePackages = {
		"fr.gouv.beta.fabnum.kelrisks.persistance.referentiel_write" })
public class DataSourceWriteConfig {
	
	@Bean(name = "db2DataSourceProperties")
	@ConfigurationProperties("database-write.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "db2DataSource")
	@ConfigurationProperties("database-write.datasource.configuration")
	public DataSource dataSource(@Qualifier("db2DataSourceProperties") DataSourceProperties db2DataSourceProperties, Environment env) {
		HikariDataSource ds = db2DataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class)
				.build();
		ds.setPoolName("pool-database-write");
		ds.setMaximumPoolSize(env.getProperty("database-write.datasource.maximum-pool-size",Integer.class));
		ds.setMinimumIdle(env.getProperty("database-write.datasource.minimum-idle", Integer.class));
		//		semble faire saturer le pool BDD "rapidement"
		ds.setIdleTimeout(env.getProperty("database-write.datasource.idle-timeout", Long.class)); 
		ds.setConnectionTimeout(env.getProperty("database-write.datasource.connection-timeout", Long.class));

		return ds;
	}
	
	@Bean(name = "db2EntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			EntityManagerFactoryBuilder builder, @Qualifier("db2DataSource") DataSource db2DataSource) {
		return builder
				.dataSource(db2DataSource)
				.packages("fr.gouv.beta.fabnum.kelrisks.transverse.referentiel.entities_write")
				.persistenceUnit("db-write")
				.build();
	}
	
	@Bean(name = "db2TransactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("db2EntityManagerFactory") EntityManagerFactory db2EntityManagerFactory) {
		return new JpaTransactionManager(db2EntityManagerFactory);
	}
	
}
