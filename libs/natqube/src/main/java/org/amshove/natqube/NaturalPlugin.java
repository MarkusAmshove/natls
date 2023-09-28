package org.amshove.natqube;

import org.amshove.natqube.measures.AggregateFileTypeMeasure;
import org.amshove.natqube.rules.NaturalQualityProfile;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.amshove.natqube.sensor.NatlintSensor;
import org.sonar.api.Plugin;

public class NaturalPlugin implements Plugin
{
	@Override
	public void define(Context context)
	{
		context.addExtension(Natural.class);
		context.addExtension(NaturalMetrics.class);
		context.addExtension(NaturalProperties.class);
		context.addExtensions(NaturalProperties.getProperties());
		context.addExtension(NatlintSensor.class);
		context.addExtension(NaturalQualityProfile.class);
		context.addExtension(NaturalRuleRepository.class);
		context.addExtension(AggregateFileTypeMeasure.class);
	}
}
