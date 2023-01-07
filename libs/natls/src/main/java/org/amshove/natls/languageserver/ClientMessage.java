package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class ClientMessage
{
	private ClientMessage()
	{}

	public static MessageParams log(String message)
	{
		return new MessageParams(MessageType.Log, message);
	}

	public static MessageParams info(String message)
	{
		return new MessageParams(MessageType.Info, message);
	}

	public static MessageParams warn(String message)
	{
		return new MessageParams(MessageType.Warning, message);
	}

	public static MessageParams error(String message)
	{
		return new MessageParams(MessageType.Error, message);
	}
}
