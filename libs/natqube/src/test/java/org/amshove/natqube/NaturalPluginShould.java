package org.amshove.natqube;

import org.amshove.natqube.measures.AggregateFileTypeMeasure;
import org.amshove.natqube.rules.NaturalQualityProfile;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.amshove.natqube.sensor.NatlintSensor;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.sonar.api.Plugin;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("unchecked")
class NaturalPluginShould
{
	@TestFactory
	Stream<DynamicTest> registerExtensions()
	{
		var context = new Plugin.Context(SonarRuntimeImpl.forSonarLint(Version.create(10, 0)));
		new NaturalPlugin().define(context);

		return Stream.of(
			Natural.class,
			NaturalMetrics.class,
			NatlintSensor.class,
			NaturalQualityProfile.class,
			NaturalRuleRepository.class,
			AggregateFileTypeMeasure.class
		)
			.map(
				extension -> DynamicTest.dynamicTest(
					"%s should be registered".formatted(extension), () -> assertThat(context.getExtensions())
						.as("Extension %s is not registered", extension)
						.anyMatch(
							e -> e.equals(extension)
						)
				)
			);
	}

	@Test
	void defineAllModuleTypeFileSuffixes()
	{
		assertThat(Natural.fileSuffixes()).hasSize(NaturalModuleType.values().length);
	}
}
