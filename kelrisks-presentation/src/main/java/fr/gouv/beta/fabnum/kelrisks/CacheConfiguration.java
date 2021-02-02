package fr.gouv.beta.fabnum.kelrisks;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.google.common.cache.CacheBuilder;

import fr.gouv.beta.fabnum.kelrisks.facade.dto.referentiel.ParcelleDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

	// Si Redis est HS au demarrage de l'application, pas de client Redis dispo...
	// https://github.com/redisson/redisson/issues/1047
	// useReplicatedServers
	// https://medium.com/better-programming/how-to-build-a-fault-tolerant-redis-network-with-spring-boot-and-docker-17e16ad898ed
	@Bean(destroyMethod = "shutdown")
	public RedissonClient redisson(Environment env) {
		RedissonClient client = null;
		try {
			Config config = new Config();
			config.useSingleServer().setAddress("redis://"+env.getProperty("REDIS_CACHE_ADDRESS")+":"+env.getProperty("REDIS_CACHE_PORT"));
			client = Redisson.create(config);
		} catch (Exception e) {
			log.error("Fail create Redisson client", e);
		}

		return client;
	}

	// https://dzone.com/articles/quickstart-how-to-use-spring-cache-on-redis
	// https://dzone.com/articles/overview-of-redisson-the-redis-java-client
	// https://www.baeldung.com/redis-redisson
	// https://github.com/redisson/redisson/wiki/14.-Integration-with-frameworks
	// $ docker run -d --name redis -p 6379:6379 redis
	@Primary
    @Bean("cacheManagerAvis")
    public CacheManager cacheManagerAvis(RedissonClient redissonClient, Environment env) {
    	if(redissonClient != null) {
            Map<String, CacheConfig> config = new HashMap<>();
            // create "v" spring cache with ttl = 2 minutes and maxIdleTime = 1 minutes
            Integer ttl = env.getProperty("application.cache.avis.ttl", Integer.class);
            Integer maxIdelTime = env.getProperty("application.cache.avis.max-idle-time", Integer.class);
            
            config.put("avis", new CacheConfig(ttl, maxIdelTime));

            return new RedissonSpringCacheManager(redissonClient, config);
    	} else {
    		// fall back if Redis HS
    		return new ConcurrentMapCacheManager("avis") {
    			  @Override
    			  protected Cache createConcurrentMapCache(final String name) {
    			    return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(2,
    			        TimeUnit.MINUTES).maximumSize(2L).build().asMap(), false);
    			  }
    		};
    	}
    }

	@Override
	public CacheErrorHandler errorHandler() {
		return new CustomCacheErrorHandler();
	}

	@Bean("cacheManagerStats")
	public CacheManager cacheManagerStats() {
		return new ConcurrentMapCacheManager("stats") {
			  @Override
			  protected Cache createConcurrentMapCache(final String name) {
			    return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(1,
			        TimeUnit.MINUTES).maximumSize(1L).build().asMap(), false);
			  }
		};
	}


	@Bean("customAvisKeyGenerator")
	public KeyGenerator keyGenerator() {

		return new CustomAvisKeyGenerator();
	}

	public static class CustomAvisKeyGenerator implements KeyGenerator {

		@Override
		public Object generate(Object target, Method method, Object... params) {

			return target.getClass().getSimpleName() + "." + method.getName() + "_" + ((List<ParcelleDTO>) params[0])
					.stream().map(parcelleDTO -> parcelleDTO.getId().toString()).collect(Collectors.joining(","));
		}
	}
}
