package org.amshove.natqube.ruletranslator;

import java.util.List;

public record SonarRule(String key, String name, String description, String priority, List<String> tags, String type)
{
}
