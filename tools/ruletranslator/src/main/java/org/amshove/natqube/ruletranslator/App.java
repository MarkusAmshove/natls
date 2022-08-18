package org.amshove.natqube.ruletranslator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class App
{
	public static void main(String[] args) throws IOException
	{
		var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<rules>\n";
		xml += RuleRepository.getRules().stream().map(SonarRule::toXml).collect(Collectors.joining("\n"));
		xml += "\n</rules>";
		Files.writeString(Paths.get(args[0]), xml, StandardCharsets.UTF_8);
	}
}
