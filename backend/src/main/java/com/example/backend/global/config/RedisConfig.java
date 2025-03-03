package com.example.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.backend.social.reaction.like.dto.LikeInfo;

@Configuration
public class RedisConfig {

	@Primary
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean
	public RedisTemplate<String, LikeInfo> likeInfoRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, LikeInfo> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		// 간단한 접근법: JDK 직렬화 사용
		// LikeInfo 클래스에는 반드시 Serializable을 구현해야 함
		JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer(getClass().getClassLoader());
		template.setValueSerializer(jdkSerializer);

		template.afterPropertiesSet();
		return template;
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		// 두 템플릿 간의 일관성을 위해 JDK 직렬화 사용
		JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer(getClass().getClassLoader());
		template.setValueSerializer(jdkSerializer);

		template.afterPropertiesSet();
		return template;
	}
}
