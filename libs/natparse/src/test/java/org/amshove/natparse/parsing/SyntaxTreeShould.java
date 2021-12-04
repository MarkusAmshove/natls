package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SyntaxTreeShould
{
	@Test
	void returnAStreamOfDescendantsByType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new TokenNode(null), new VariableNode());

		var descendants = tree.descendantsOfType(TokenNode.class).toList();
		assertThat(descendants).hasSize(1);
		assertThat(descendants.get(0).getClass()).isEqualTo(TokenNode.class);
	}

	@Test
	void findATokenWithSpecificKind()
	{
		var tree = SyntaxTree.create(
			new TokenNode(new SyntaxToken(SyntaxKind.ADD, 0, 0, 0, "")),
			new TokenNode(new SyntaxToken(SyntaxKind.DEFINE, 0, 0, 0, "")),
			new TokenNode(new SyntaxToken(SyntaxKind.END_DEFINE, 0, 0, 0, ""))
		);

		var tokenNode = tree.findDirectChildSyntaxToken(SyntaxKind.DEFINE);
		assertThat(tokenNode).isNotNull();
		assertThat(tokenNode.token().kind()).isEqualTo(SyntaxKind.DEFINE);
	}

	@Test
	void returnNullWhenLookingForATokenKindThatIsNotContained()
	{
		var tree = SyntaxTree.create(
			new TokenNode(new SyntaxToken(SyntaxKind.ADD, 0, 0, 0, "")),
			new TokenNode(new SyntaxToken(SyntaxKind.END_DEFINE, 0, 0, 0, ""))
		);

		var tokenNode = tree.findDirectChildSyntaxToken(SyntaxKind.DEFINE);
		assertThat(tokenNode).isNull();
	}

	@Test
	void findASingleDescendantNodeOfType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new TokenNode(null), new VariableNode());

		var tokenNode = tree.findDirectChildOfType(TokenNode.class);
		assertThat(tokenNode).isNotNull();
		assertThat(tokenNode.getClass()).isEqualTo(TokenNode.class);
	}

	@Test
	void returnNullWhenNotFindingADescendantNodeOfType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new VariableNode());

		var tokenNode = tree.findDirectChildOfType(TokenNode.class);
		assertThat(tokenNode).isNull();
	}
}
