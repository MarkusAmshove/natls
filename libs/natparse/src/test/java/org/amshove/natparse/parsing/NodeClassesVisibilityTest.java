package org.amshove.natparse.parsing;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.amshove.natparse.natural.ISyntaxTree;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class NodeClassesVisibilityTest
{
	@Test
	void allNodeClassesShouldBePackagePrivate()
	{
		var classes = new ClassFileImporter().importPackagesOf(BaseSyntaxNode.class);
		var rule = classes().that().areAssignableTo(ISyntaxTree.class).should().bePackagePrivate();
		rule.check(classes);
	}
}
