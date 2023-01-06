package org.amshove.natls.catalog;

public record CatalogResult(String library, String module, int line, int column, String text)
{}
