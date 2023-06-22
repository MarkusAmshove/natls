package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IMoveStatementNode;
import org.amshove.natparse.natural.IOperandNode;

class MoveStatementNode extends StatementNode implements IMoveStatementNode
{
	private final List<IOperandNode> targets = new ArrayList<>();

	private IOperandNode operand;
	private SyntaxKind moveKind;
	private SyntaxKind byKind;
	private SyntaxKind direction;
	private boolean isRounded;
	private boolean isEdited;
	private boolean isNormalized;
	private boolean isEncoded;
	private boolean isAll;

	@Override
	public ReadOnlyList<IOperandNode> targets()
	{
		return ReadOnlyList.from(targets);
	}

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public SyntaxKind moveKind()
	{
		return moveKind;
	}

	@Override
	public SyntaxKind byKind()
	{
		return byKind != null ? byKind : SyntaxKind.NAME;
	}

	@Override
	public SyntaxKind direction()
	{
		return direction;
	}

	@Override
	public boolean isRounded()
	{
		return isRounded;
	}

	@Override
	public boolean isEdited()
	{
		return isEdited;
	}

	@Override
	public boolean isNormalized()
	{
		return isNormalized;
	}

	@Override
	public boolean isEncoded()
	{
		return isEncoded;
	}

	@Override
	public boolean isAll()
	{
		return isAll;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void addTarget(IOperandNode target)
	{
		targets.add(target);
	}

	void setMoveKind(SyntaxKind moveKind)
	{
		this.moveKind = moveKind;
	}

	void setByKind(SyntaxKind byKind)
	{
		this.byKind = byKind;
	}

	void setDirection(SyntaxKind direction)
	{
		this.direction = direction;
	}

	void setRounded(boolean rounded)
	{
		isRounded = rounded;
	}

	void setEdited(boolean edited)
	{
		isEdited = edited;
	}

	void setNormalized(boolean normalized)
	{
		isNormalized = normalized;
	}

	void setEncoded(boolean encoded)
	{
		isEncoded = encoded;
	}

	void setAll(boolean all)
	{
		isAll = all;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.from(targets);
	}
}
