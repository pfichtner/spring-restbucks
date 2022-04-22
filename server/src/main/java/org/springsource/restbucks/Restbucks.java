/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks;

import static java.util.Collections.singletonList;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Central application class containing both general application and web
 * configuration as well as a main-method to bootstrap the application using
 * Spring Boot.
 *
 * @see #main(String[])
 * @see SpringApplication
 * @author Oliver Gierke
 */
@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class Restbucks {

	public static final String CURIE_NAMESPACE = "restbucks";

	@Bean 
	public CurieProvider curieProvider() {
		return new DefaultCurieProvider(CURIE_NAMESPACE, UriTemplate.of("/docs/{rel}.html"));
	}

	/**
	 * Bootstraps the application in standalone mode (i.e. java -jar).
	 *
	 * @param args
	 */
	public static void main(String... args) {
		SpringApplication.run(Restbucks.class, args);
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
				new CorsFilter(corsConfigurationSource()));
		bean.setOrder(HIGHEST_PRECEDENCE);
		return bean;
	}

	private static CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration());
		return source;
	}

	private static CorsConfiguration corsConfiguration() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(singletonList("*"));
		config.setAllowedMethods(singletonList("*"));
		config.setAllowedHeaders(singletonList("*"));
		return config;
	}

}
