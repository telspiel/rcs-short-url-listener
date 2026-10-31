
package com.vts.shorturlistener.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

	@Configuration
	public class SenderConfig {

//	    @Bean(name = "redisTemplateForShortUrlMap")
//	    public RedisTemplate<String, Map<String, String>> redisTemplateForShortUrlMap(
//	            RedisConnectionFactory connectionFactory) {
//
//	        RedisTemplate<String, Map<String, String>> template = new RedisTemplate<>();
//
//	        template.setConnectionFactory(connectionFactory);
//	        template.setKeySerializer(new StringRedisSerializer());
//	        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//	        template.afterPropertiesSet();
//
//	        return template;
//	    }
	}

