package org.amshove.natparse.parsing;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class NodeClassesArchitectureTests
{
	@Test
	void allNodeClassesShouldBePackagePrivate()
	{
		var classes = new ClassFileImporter().importPackagesOf(BaseSyntaxNode.class);
		var rule = classes().that().areAssignableTo(ISyntaxTree.class).and().areNotInterfaces().should().bePackagePrivate();
		rule.check(classes);
	}

	@Test
	void allNodeClassesShouldExtendBaseSyntaxNode()
	{
		var classes = new ClassFileImporter().importPackagesOf(BaseSyntaxNode.class);
		var rule = classes().that().areAssignableTo(ISyntaxNode.class).and().areNotInterfaces().should().beAssignableTo(BaseSyntaxNode.class);
		rule.check(classes);
	}
}
