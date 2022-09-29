package org.amshove.testhelpers;

import org.amshove.natparse.natural.project.NaturalProject;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows test method parameter to be annotated with {@link ProjectName} which will
 * resolve the project using {@link TestProjectLoader}.
 * Project directory will be copied to the systems temporary folder and deleted after the test,
 * if it is not run in CI.
 */
public class NaturalProjectResourceResolver implements ParameterResolver
{
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(NaturalProjectResourceResolver.class);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		return parameterContext.getParameter().getType() == NaturalProject.class && parameterContext.findAnnotation(ProjectName.class).isPresent();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		try
		{
			var projectName = parameterContext.getParameter().getAnnotation(ProjectName.class).value();
			var tempDir = new AutoDeleteTempDirectory(projectName);
			extensionContext.getStore(NAMESPACE).put("tempdir", tempDir);
			return TestProjectLoader.loadProjectFromResources(tempDir.path, projectName);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public static class AutoDeleteTempDirectory implements ExtensionContext.Store.CloseableResource
	{
		private final Path path;

		@Override
		public void close() throws Throwable
		{
			if(System.getenv().containsKey("GITHUB_ACTIONS"))
			{
				return;
			}

			try(var walk = Files.walk(path))
			{
				walk
					.map(Path::toFile)
					.forEach(File::delete);
			}
		}

		public Path getPath()
		{
			return path;
		}

		public AutoDeleteTempDirectory(String directoryName) throws IOException
		{
			path = Files.createTempDirectory(directoryName);
		}
	}
}
