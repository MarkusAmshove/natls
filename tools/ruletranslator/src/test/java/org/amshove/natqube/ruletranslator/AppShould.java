package org.amshove.natqube.ruletranslator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AppShould
{
	@Test
	void writeTheRulesToAFile(@TempDir Path directory) throws IOException
	{
		var file = directory.resolve("rules.xml");
		App.main(new String[] {
			file.toString()
		});
		assertThat(Files.readString(file)).isNotBlank();
	}
}
