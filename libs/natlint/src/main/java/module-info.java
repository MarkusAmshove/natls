module natls.natlint {
	exports org.amshove.natlint.linter;
	exports org.amshove.natlint.api;
	exports org.amshove.natlint.analyzers;

	opens org.amshove.natlint.cli;

	requires natls.natparse;
	requires info.picocli;
	requires com.google.common;
}