package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IPerformNode;

sealed class PerformNode extends StatementNode implements IPerformNode permits ExternalPerformNode, InternalPerformNode
{
}
