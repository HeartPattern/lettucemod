package com.redis.spring.lettucemod;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;

import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.support.ConnectionPoolSupport;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisModulesAutoConfiguration {

	@SuppressWarnings("deprecation")
	@Bean
	RedisURI redisURI(RedisProperties properties) {
		RedisURI redisURI = RedisURI.create(properties.getHost(), properties.getPort());
		if (properties.getPassword() != null) {
			redisURI.setPassword(properties.getPassword().toCharArray());
		}
		if (properties.getUsername() != null) {
			redisURI.setUsername(properties.getUsername());
		}
		Duration timeout = properties.getTimeout();
		if (timeout != null) {
			redisURI.setTimeout(timeout);
		}
		return redisURI;
	}

	@Bean(destroyMethod = "shutdown")
	ClientResources clientResources() {
		return DefaultClientResources.create();
	}

	@Bean(destroyMethod = "shutdown")
	RedisModulesClient client(RedisURI redisURI, ClientResources clientResources) {
		return RedisModulesClient.create(clientResources, redisURI);
	}

	@Bean(name = "redisModulesConnection", destroyMethod = "close")
	StatefulRedisModulesConnection<String, String> connection(RedisModulesClient redisModulesClient) {
		return redisModulesClient.connect();
	}

	@Bean(name = "redisModulesConnectionPoolConfig")
	GenericObjectPoolConfig<StatefulRedisModulesConnection<String, String>> poolConfig(
			RedisProperties redisProperties) {
		return configure(redisProperties, new GenericObjectPoolConfig<>());
	}

	public <K, V> GenericObjectPoolConfig<StatefulRedisModulesConnection<K, V>> configure(
			RedisProperties redisProperties, GenericObjectPoolConfig<StatefulRedisModulesConnection<K, V>> config) {
		config.setJmxEnabled(false);
		Pool poolProps = redisProperties.getLettuce().getPool();
		if (poolProps != null) {
			config.setMaxTotal(poolProps.getMaxActive());
			config.setMaxIdle(poolProps.getMaxIdle());
			config.setMinIdle(poolProps.getMinIdle());
			if (poolProps.getMaxWait() != null) {
				config.setMaxWait(poolProps.getMaxWait());
			}
		}
		return config;
	}

	@Bean(name = "redisModulesConnectionPool", destroyMethod = "close")
	GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool(
			GenericObjectPoolConfig<StatefulRedisModulesConnection<String, String>> config, RedisModulesClient client) {
		return ConnectionPoolSupport.createGenericObjectPool(client::connect, config);
	}

}
