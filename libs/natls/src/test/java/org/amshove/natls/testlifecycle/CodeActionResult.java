package org.amshove.natls.testlifecycle;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;

public record CodeActionResult(String savedSource, List<CodeAction> codeActions)
{}
