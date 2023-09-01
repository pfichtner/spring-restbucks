package org.springsource.restbucks;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springsource.restbucks.order.Order;

/**
 * Disables all HTTP methods for all entities managed by Spring-Data-Rest.
 * Actually this should be standard with Spring-Data-Rest, but unfortunately it
 * is not.
 * 
 * @author Peter Fichtner
 */
@Configuration
public class CustomRepositoryRestConfigurer implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		// disable all
		disableAll(config);
		// allow GET, PUT, PATCH, DELETE on order resources and GET, POST on the orders
		// collection
		config.getExposureConfiguration().forDomainType(Order.class) //
				.withItemExposure((metadata, httpMethods) -> httpMethods.enable(GET, PUT, PATCH, DELETE)) //
				.withCollectionExposure((metadata, httpMethods) -> httpMethods.enable(GET, POST));

	}

	private void disableAll(RepositoryRestConfiguration config) {
		HttpMethod[] all = HttpMethod.values();
		config.getExposureConfiguration().forDomainType(Object.class) //
				.withItemExposure((metadata, httpMethods) -> httpMethods.disable(all)) //
				.withCollectionExposure((metadata, httpMethods) -> httpMethods.disable(all));
	}

}
