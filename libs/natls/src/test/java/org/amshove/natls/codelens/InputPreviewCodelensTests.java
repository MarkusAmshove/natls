package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.config.LSConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class InputPreviewCodelensTests extends CodeLensTest
{
	@Test
	void codeLensShouldNotBeShownWhenFeatureIsDisabled()
	{
		var document = createOrSaveFile("LIBONE", "PROG.NSP", """
			DEFINE DATA LOCAL
			END-DEFINE
			INPUT 'Hi'
			END
			""");

		testCodeLens(document, lenses -> assertThat(lenses).noneMatch(l -> l.getCommand().getTitle().contains("Preview")));
	}

	@Test
	void codeLensShouldBeShownWhenFeatureIsEnabled()
	{
		var config = LSConfiguration.createDefault();
		config.getMaps().setEnablePreview(true);
		configureLSConfig(config);
		var document = createOrSaveFile("LIBONE", "PROG.NSP", """
			DEFINE DATA LOCAL
			END-DEFINE
			INPUT 'Hi'
			END
			""");

		testCodeLens(
			document, lenses -> assertThat(lenses).anyMatch(
				l -> l.getCommand().getTitle().contains("Open Preview") && l.getCommand().getCommand().equalsIgnoreCase(
					CustomCommands.CODELENS_PREVIEW_INPUT_STATEMENT
				)
			)
		);
	}

	@Test
	void codeLensShouldNotBeShownWhenFeatureIsDisabledLater()
	{
		var config = LSConfiguration.createDefault();
		config.getMaps().setEnablePreview(true);
		configureLSConfig(config);
		var document = createOrSaveFile("LIBONE", "PROG.NSP", """
			DEFINE DATA LOCAL
			END-DEFINE
			INPUT 'Hi'
			END
			""");

		testCodeLens(document, lenses -> assertThat(lenses).anyMatch(l -> l.getCommand().getTitle().contains("Open Preview")));

		var newConfig = LSConfiguration.createDefault();
		newConfig.getMaps().setEnablePreview(false);
		configureLSConfig(newConfig);
		testCodeLens(document, lenses -> assertThat(lenses).noneMatch(l -> l.getCommand().getTitle().contains("Open Preview")));
	}
}
