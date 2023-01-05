package org.amshove.natlint.editorconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class EditorConfigTest
{
	private final EditorConfigParser sut = new EditorConfigParser();

	@ParameterizedTest
	@ValueSource(strings =
	{
		"*", "*.NSN", "*.{NSN,NSP,NSL}"
	})
	void parseASection(String pattern)
	{
		var config = sut.parse("[%s]".formatted(pattern));
		assertSectionExists(config, pattern);
	}

	@Test
	void parseAPropertyValue()
	{
		var config = sut.parse("""
			[*]
			natls.NL001.severity=none
						""");

		assertProperty(config, "*", "natls.NL001.severity", "none");
	}

	@Test
	void parsePropertiesWithDifferentSpacing()
	{
		var config = sut.parse("""
			[*]
			natls.NL001.severity= none
			natls.NL002.severity =error
			natls.NL003.severity = warning
						""");

		assertProperty(config, "*", "natls.NL001.severity", "none");
		assertProperty(config, "*", "natls.NL002.severity", "error");
		assertProperty(config, "*", "natls.NL003.severity", "warning");
	}

	@Test
	void getAPropertyMatchingAFile()
	{
		var config = sut.parse("""
			[*.NSN]
			natls.NL010.severity=none
						""");

		var value = config.getProperty(Path.of("folder", "SUB.NSN"), "natls.NL010.severity", "default");
		assertThat(value).isEqualTo("none");
	}

	@Test
	void getAPropertiesDefaultValueIfNoRuleIsSpecified()
	{
		var config = sut.parse("""
			[*.NSN]
			natls.NL010.severity=none
						""");

		var value = config.getProperty(Path.of("SUB.NSN"), "natls.NLßß1.severity", "hi");
		assertThat(value).isEqualTo("hi");
	}

	@Test
	void giveTheLatestDeclaredPropertyTheHighestPriority()
	{
		var config = sut.parse("""
			[*]
			prop=error

			[*.NSN]
			prop=warn
						""");

		var subProperty = config.getProperty(Path.of("folder", "SUB.NSN"), "prop", "default");
		assertThat(subProperty).isEqualTo("warn");

		var progProperty = config.getProperty(Path.of("folder", "PROG.NSP"), "prop", "default");
		assertThat(progProperty).isEqualTo("error");
	}

	private EditorConfigSection assertSectionExists(EditorConfig config, String section)
	{
		assertThat(config.sections())
			.as("Section [%s] does not exist".formatted(section))
			.anyMatch(s -> s.filePattern().equals(section));

		return config.sections().stream().filter(s -> s.filePattern().equals(section)).findFirst().get();
	}

	private void assertProperty(EditorConfig config, String sectionName, String property, String value)
	{
		var section = assertSectionExists(config, sectionName);

		assertThat(section.properties())
			.as("Property with name %s does not exist".formatted(property))
			.anyMatch(s -> s.name().equals(property));

		var theProperty = section.properties().stream().filter(s -> s.name().equals(property)).findFirst().get();

		assertThat(theProperty.value())
			.as("Value of property %s doesn't match".formatted(property))
			.isEqualTo(value);
	}
}
