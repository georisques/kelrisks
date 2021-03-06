package fr.gouv.beta.fabnum.kelrisks;

import fr.gouv.beta.fabnum.commun.metier.util.SecurityHelper;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import reactor.util.annotation.NonNull;

import javax.net.ssl.SSLException;

import org.geolatte.geom.json.GeolatteGeomModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration of the business, persistence and security layers.
 */
@SpringBootApplication(scanBasePackages = {"fr.gouv.beta.fabnum"})
@EnableConfigurationProperties
@EnableJpaAuditing
public class Application extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        
        return application.sources(Application.class);
    }
    
    public static void main(String[] args) {
        
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        
        return new WebMvcConfigurer() {
    
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
        
                //registry.addMapping("/**/api/**");// bug avec spring boot 2.4.0
                registry.addMapping("/api/**");
            }
        };
    }
    
    @Bean
    public GeolatteGeomModule geolatteGeomModule() {
        
        return new GeolatteGeomModule();
    }
    
    @Bean
    public WebClient webClient(final ClientHttpConnector clientHttpConnector, final ExchangeStrategies exchangeStrategies) {
        
        return WebClient.builder()
                       .clientConnector(clientHttpConnector)
                       .exchangeStrategies(exchangeStrategies)
                       .build();
    }
    
    @Bean
    public ExchangeStrategies exchangeStrategies(@Value("${webclient.maxInMemorySizeInMB}") final int mb) {
        
        return ExchangeStrategies.builder()
                       .codecs(configurer -> configurer
                                                     .defaultCodecs()
                                                     .maxInMemorySize(mb * 1024 * 1024))
                       .build();
    }
    
    @Bean
    public ClientHttpConnector clientHttpConnector(@Value("${webclient.enable-keep-alive}") final boolean keepAlive,
                                                   @Value("${webclient.read-timeout-in-seconds}") final int readTimeout,
                                                   @Value("${webclient.write-timeout-in-seconds}") final int writeTimeout) throws SSLException {
        // FIXME ignore SSL error https://stackoverflow.com/a/53147631
    	SslContext context = SslContextBuilder.forClient()
    		    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    		    .build();
    	HttpClient httpClient = HttpClient.from(TcpClient.create()
                .option(ChannelOption.SO_KEEPALIVE, keepAlive)
                .doOnConnected(connection -> connection
                                                     .addHandlerLast(new ReadTimeoutHandler(readTimeout))
                                                     .addHandlerLast(new WriteTimeoutHandler(writeTimeout)))).compress(false).secure(t -> t.sslContext(context));
    
        return new ReactorClientHttpConnector(httpClient);
    }
    
    @Bean(name = "securityHelperEncoder")
    public SecurityHelper securityHelperEncoder(@Value("${kelrisks.app.security.passphrase}") final String passphrase) {
        
        return new SecurityHelper(passphrase);
    }
}
