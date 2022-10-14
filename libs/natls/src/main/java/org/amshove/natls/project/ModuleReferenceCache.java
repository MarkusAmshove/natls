package org.amshove.natls.project;

/***
 * Cache of module references base on the {@linkplain ModuleReferenceParser}.</br>
 * This is built during indexing phase of a project.</br>
 * Entries will be evicted one a module gets parsed, as the referencing will then be done
 * by the actual {@linkplain org.amshove.natparse.parsing.NaturalParser}
 */
public class ModuleReferenceCache
{
}
