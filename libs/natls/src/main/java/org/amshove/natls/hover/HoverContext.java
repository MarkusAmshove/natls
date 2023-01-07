package org.amshove.natls.hover;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;

public record HoverContext(ISyntaxNode nodeToHover, SyntaxToken tokenToHover, LanguageServerFile file)
{}
