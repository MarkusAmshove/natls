package org.amshove.natqube.ruletranslator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AppShould
{
	@Test
	void writeTheRulesToAFile(@TempDir Path directory) throws IOException
	{
		System.setOut(new PrintStream(new ByteArrayOutputStream())); // ignore stdout
		var file = directory.resolve("rules.xml");
		var markdownDirectory = directory.resolve("website");
		App.main(new String[]
		{
			file.toString(),
			markdownDirectory.toString()
		});
		assertThat(Files.readString(file)).isNotBlank();
		assertThat(Files.list(markdownDirectory).toList().size()).isGreaterThan(0);
	}
}
