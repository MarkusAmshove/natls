package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SyntaxTreeShould
{
	private SyntaxToken createToken(SyntaxKind kind, int offset, int lineOffset, int line, String source)
	{
		return new SyntaxToken(kind, offset, lineOffset, line, source, Paths.get("TEST.NSA"));
	}

	@Test
	void returnAStreamOfDescendantsByType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new TokenNode(null), new VariableNode());

		var descendants = tree.directDescendantsOfType(TokenNode.class).toList();
		assertThat(descendants).hasSize(1);
		assertThat(descendants.get(0).getClass()).isEqualTo(TokenNode.class);
	}

	@Test
	void findATokenWithSpecificKind()
	{
		var tree = SyntaxTree.create(
			new TokenNode(createToken(SyntaxKind.ADD, 0, 0, 0, "")),
			new TokenNode(createToken(SyntaxKind.DEFINE, 0, 0, 0, "")),
			new TokenNode(createToken(SyntaxKind.END_DEFINE, 0, 0, 0, ""))
		);

		var tokenNode = tree.findDescendantToken(SyntaxKind.DEFINE);
		assertThat(tokenNode).isNotNull();
		assertThat(tokenNode.token().kind()).isEqualTo(SyntaxKind.DEFINE);
	}

	@Test
	void returnNullWhenLookingForATokenKindThatIsNotContained()
	{
		var tree = SyntaxTree.create(
			new TokenNode(createToken(SyntaxKind.ADD, 0, 0, 0, "")),
			new TokenNode(createToken(SyntaxKind.END_DEFINE, 0, 0, 0, ""))
		);

		var tokenNode = tree.findDescendantToken(SyntaxKind.DEFINE);
		assertThat(tokenNode).isNull();
	}

	@Test
	void findASingleDescendantNodeOfType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new TokenNode(null), new VariableNode());

		var tokenNode = tree.findDescendantOfType(TokenNode.class);
		assertThat(tokenNode).isNotNull();
		assertThat(tokenNode.getClass()).isEqualTo(TokenNode.class);
	}

	@Test
	void returnNullWhenNotFindingADescendantNodeOfType()
	{
		var tree = SyntaxTree.create(new BaseSyntaxNode(), new VariableNode());

		var tokenNode = tree.findDescendantOfType(TokenNode.class);
		assertThat(tokenNode).isNull();
	}
}
