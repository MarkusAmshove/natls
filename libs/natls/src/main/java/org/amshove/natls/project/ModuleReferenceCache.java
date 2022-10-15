package org.amshove.natls.project;

import org.amshove.natparse.IPosition;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cache of module references base on the {@linkplain ModuleReferenceParser}.</br>
 * This is built during indexing phase of a project.</br>
 * Entries will be evicted one a module gets parsed, as the referencing will then be done
 * by the actual {@linkplain org.amshove.natparse.parsing.NaturalParser}
 */
// This class is super perf sensitive. It adds a lot to the start time if not tuned correctly.
public class ModuleReferenceCache
{
	private static final Map<LanguageServerFile, Set<IPosition>> cache = new HashMap<>();
	private static final Set<Path> evictedEntries = new HashSet<>();

	static void addEntry(LanguageServerFile calledFile, IPosition callingPosition)
	{
		var callerPositions = cache.computeIfAbsent(calledFile, k -> new HashSet<>());
		callerPositions.add(callingPosition);
	}

	/**
	 * Retrieve the cached calling sites of the given file.
	 */
	public static Set<IPosition> retrieveCachedPositions(LanguageServerFile calledFile)
	{
		if(!cache.containsKey(calledFile))
		{
			return Set.of();
		}

		return Set.copyOf(cache.get(calledFile));
	}

	/**
	 * Evicts all cache entries pointing to the given file.</br>
	 */
	public static synchronized void evictMyReferences(LanguageServerFile callingFile)
	{
		if(evictedEntries.contains(callingFile.getPath()))
		{
			return;
		}

		for (var outgoingReference : callingFile.getOutgoingReferences())
		{
			if(!cache.containsKey(outgoingReference))
			{
				continue;
			}

			cache.get(outgoingReference).removeIf(pos -> pos.filePath().equals(callingFile.getPath()));
		}

		evictedEntries.add(callingFile.getPath());
	}

	private ModuleReferenceCache() {}
}
