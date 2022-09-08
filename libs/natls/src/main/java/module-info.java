module natls.natls {
	exports org.amshove.natls.languageserver;
	requires natls.natparse;
	requires natls.natlint;
	requires java.xml;
	requires com.google.gson;
	requires org.eclipse.lsp4j;
	requires org.eclipse.lsp4j.generator;
	requires org.eclipse.lsp4j.jsonrpc;
}