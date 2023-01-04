package org.amshove.natqube.rules;

import org.amshove.natqube.Natural;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NaturalRuleRepository implements RulesDefinition
{
	public static final String REPOSITORY = "natural-rule-repository";
	private final RulesDefinitionXmlLoader xmlLoader;

	public NaturalRuleRepository(RulesDefinitionXmlLoader xmlLoader)
	{
		this.xmlLoader = xmlLoader;
	}

	@Override
	public void define(Context context)
	{
		var repository = context.createRepository(REPOSITORY, Natural.KEY).setName("Natural Diagnostics");

		var rulesStream = getClass().getResourceAsStream("/rules.xml");
		Objects.requireNonNull(rulesStream);
		xmlLoader.load(repository, rulesStream, StandardCharsets.UTF_8);

		repository.rules().forEach(
			r -> r
				.setDebtRemediationFunction(r.debtRemediationFunctions().linear("5min"))
				.setActivatedByDefault(true)
		);

		repository.done();
	}
}
