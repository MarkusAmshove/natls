module natls.natparse {
	exports org.amshove.natparse;
	exports org.amshove.natparse.lexing;
	exports org.amshove.natparse.natural;
	exports org.amshove.natparse.natural.ddm;
	exports org.amshove.natparse.natural.project;
	exports org.amshove.natparse.parsing;
	exports org.amshove.natparse.infrastructure;
	exports org.amshove.natparse.parsing.project;
	exports org.amshove.natparse.natural.conditionals;
	exports org.amshove.natparse.parsing.ddm;
	exports org.amshove.natparse.natural.builtin;

	requires org.checkerframework.checker.qual;
	requires java.xml;
}