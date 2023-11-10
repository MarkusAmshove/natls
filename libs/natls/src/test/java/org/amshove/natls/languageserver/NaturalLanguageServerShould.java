package org.amshove.natls.languageserver;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.eclipse.lsp4j.SetTraceParams;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NaturalLanguageServerShould extends EmptyProjectTest
{
	@Test
	void beAbleToSetTheLoggingLevelToInfo()
	{
		changeLevelToSevere();
		assertThat(getCurrentLevel()).isEqualTo(Level.SEVERE);
		getContext().server().setTrace(new SetTraceParams("off"));
		assertThat(getCurrentLevel()).isEqualTo(Level.INFO);
	}

	@Test
	void beAbleToSetTheLoggingLevelToFine()
	{
		changeLevelToSevere();
		assertThat(getCurrentLevel()).isEqualTo(Level.SEVERE);
		getContext().server().setTrace(new SetTraceParams("verbose"));
		assertThat(getCurrentLevel()).isEqualTo(Level.FINE);
	}

	private Level getCurrentLevel()
	{
		return Logger.getLogger("").getLevel();
	}

	private void changeLevelToSevere()
	{
		var rootLogger = Logger.getLogger("");
		rootLogger.setLevel(Level.SEVERE);
		for (var handler : rootLogger.getHandlers())
		{
			handler.setLevel(Level.SEVERE);
		}
	}
}
