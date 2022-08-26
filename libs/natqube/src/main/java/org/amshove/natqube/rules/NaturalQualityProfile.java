package org.amshove.natqube.rules;

import org.amshove.natqube.Natural;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class NaturalQualityProfile implements BuiltInQualityProfilesDefinition
{
	private final RuleFinder ruleFinder;

	public NaturalQualityProfile(RuleFinder ruleFinder)
	{
		this.ruleFinder = ruleFinder;
	}

	@Override
	public void define(Context context)
	{
		var naturalRules = ruleFinder.findAll(RuleQuery.create().withRepositoryKey(NaturalRuleRepository.REPOSITORY));

		var qualityProfile = context.createBuiltInQualityProfile("Natural Quality Profile", Natural.KEY);

		naturalRules.forEach(r -> qualityProfile.activateRule(r.getRepositoryKey(), r.getKey()));

		qualityProfile.done();
	}
}
