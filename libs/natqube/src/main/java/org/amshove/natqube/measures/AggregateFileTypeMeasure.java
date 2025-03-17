package org.amshove.natqube.measures;

import org.amshove.natqube.NaturalMetrics;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;

public class AggregateFileTypeMeasure implements MeasureComputer
{
	private static final String[] COMPUTED_METRIC_KEYS = new String[]
	{
		NaturalMetrics.NUMBER_OF_DDMS.getKey(),
		NaturalMetrics.NUMBER_OF_SUBPROGRAMS.getKey(),
		NaturalMetrics.NUMBER_OF_PROGRAMS.getKey(),
		NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES.getKey(),
		NaturalMetrics.NUMBER_OF_HELPROUTINES.getKey(),
		NaturalMetrics.NUMBER_OF_GDAS.getKey(),
		NaturalMetrics.NUMBER_OF_LDAS.getKey(),
		NaturalMetrics.NUMBER_OF_PDAS.getKey(),
		NaturalMetrics.NUMBER_OF_MAPS.getKey(),
		NaturalMetrics.NUMBER_OF_COPYCODES.getKey(),
		NaturalMetrics.NUMBER_OF_FUNCTIONS.getKey(),
		NaturalMetrics.NUMBER_OF_TEXT_FILES.getKey(),
		NaturalMetrics.NUMBER_OF_CLASSES.getKey()
	};

	@Override
	public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext)
	{
		return defContext
			.newDefinitionBuilder()
			.setOutputMetrics(COMPUTED_METRIC_KEYS)
			.build();
	}

	@Override
	public void compute(MeasureComputerContext context)
	{
		if (context.getComponent().getType() != Component.Type.FILE)
		{
			for (String computedMetricKey : COMPUTED_METRIC_KEYS)
			{
				int sum = 0;

				for (var measure : context.getChildrenMeasures(computedMetricKey))
				{
					if (measure.getIntValue() > 0)
					{
						sum += measure.getIntValue();
					}
				}

				context.addMeasure(computedMetricKey, sum);
			}
		}
	}
}
