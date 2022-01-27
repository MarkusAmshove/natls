package org.amshove.natparse.natural;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.project.NaturalFile;

public interface INaturalModule
{
	String name();
	NaturalFile file();
	ReadOnlyList<IDiagnostic> diagnostics();
	ReadOnlyList<IModuleReferencingNode> callers();
	void removeCaller(IModuleReferencingNode node);
	void addCaller(IModuleReferencingNode caller);
}
