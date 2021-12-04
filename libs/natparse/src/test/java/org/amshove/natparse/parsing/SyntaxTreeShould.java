package org.amshove.natparse.parsing;

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
}
