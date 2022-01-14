package org.amshove.natls;

import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestProjectLoader
{
	/**
	 * Copies the project folder from test/resources to the destination.
	 * Make sure to use JUnit TempDir for destinationDirectory
	 * @param projectNameInResources relative path to the project from test/resources/org/amshove/natls/projects
	 */
	public static LanguageServerProject loadProjectFromResources(Path destinationDirectory, String projectNameInResources)
	{

		var packageSubfolder = TestProjectLoader.class.getPackageName().replace(".", "/");
		var workingDirectory = System.getProperty("user.dir");
		var resourceProjectPath = Paths.get(workingDirectory, "src", "test", "resources", packageSubfolder, "projects", projectNameInResources);

		copyProjectToTemporaryFolder(destinationDirectory, resourceProjectPath);

		var buildFileParser = new BuildFileProjectReader();
		var project = buildFileParser.getNaturalProject(destinationDirectory.resolve(".natural"));
		new NaturalProjectFileIndexer().indexProject(project);
		return LanguageServerProject.fromProject(project);
	}

	private static void copyProjectToTemporaryFolder(Path destinationDirectory, Path sourceDirectoryLocation)
	{
		try(var walk = Files.walk(sourceDirectoryLocation))
		{
				walk.forEach(source -> {
					var target = destinationDirectory.resolve(sourceDirectoryLocation.relativize(source));

					// Root folder
					if(target.equals(destinationDirectory))
					{
						return;
					}

					try
					{
						Files.copy(source, target);
					}
					catch (IOException e)
					{
						throw new UncheckedIOException(e);
					}
				});
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
