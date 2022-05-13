package org.springsource.restbucks;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.order.web.OrderProjection;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "org.springsource.restbucks", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	ArchRule portsAndAdapters = onionArchitecture() //
			.withOptionalLayers(true) //
			.domainModels("..restbucks..") //
			.adapter("web", "..web") //
			.ignoreDependency(OrderRepository.class, OrderProjection.class) //
	;

}
