package org.amshove.natls.hover;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.ISyntaxNode;

public record HoverContext(ISyntaxNode nodeToHover, LanguageServerFile file)
{
}
