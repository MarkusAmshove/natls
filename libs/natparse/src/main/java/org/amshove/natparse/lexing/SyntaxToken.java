package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;
import org.checkerframework.dataflow.qual.Pure;

import java.nio.file.Path;
import java.util.Optional;

import static org.amshove.natparse.lexing.SyntaxKind.IDENTIFIER;

public class SyntaxToken implements IPosition
{

	private final SyntaxKind kind;
	private SyntaxKind originalKind;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final String source;
	private final Path filePath;
	private String identifierName = null;
	private IPosition diagnosticPosition;

	public SyntaxKind kind()
	{
		return kind;
	}

	public int offset()
	{
		return offset;
	}

	public int offsetInLine()
	{
		return offsetInLine;
	}

	public int line()
	{
		return line;
	}

	public String source()
	{
		return source;
	}

	public int length()
	{
		return source.length();
	}

	public Path filePath()
	{
		return filePath;
	}

	/**
	 * Returns the position which can be used for Diagnostics. The return value only differs from the tokens actual
	 * Position if the token is used e.g. via INCLUDE.
	 */
	public IPosition diagnosticPosition()
	{
		if (diagnosticPosition == null)
		{
			return this;
		}

		return diagnosticPosition;
	}

	public void setDiagnosticPosition(IPosition diagnosticPosition)
	{
		this.diagnosticPosition = diagnosticPosition;
	}

	// TODO: Introduce `LiteralToken`?
	public int intValue()
	{
		if (kind.isSystemVariable())
		{
			// TODO(system-variables): Check actual lengths
			return 8;
		}
		return Integer.parseInt(source());
	}

	public String stringValue()
	{
		if (source.startsWith("H'"))
		{
			var hexLiteral = source.split("'")[1];
			var stringLiteral = new StringBuilder(hexLiteral.length() / 2);
			for (var i = 0; i < hexLiteral.length(); i += 2)
			{
				var hexPart = i + 2 > hexLiteral.length()
					? hexLiteral.charAt(i) + "0" // just to prevent an Exception. The lexer raises a diagnostic for this
					: hexLiteral.substring(i, i + 2);
				stringLiteral.append((char) Integer.parseInt(hexPart, 16));
			}

			return stringLiteral.toString();
		}
		return source.substring(1, source.length() - 1).replace("''", "'");
	}

	/**
	 * Returns the token source as symbol name (all uppercase).
	 */
	public String symbolName()
	{
		if (identifierName != null)
		{
			return identifierName;
		}
		identifierName = source.toUpperCase();
		return identifierName;
	}

	/**
	 * Returns the token source as symbol name (all uppercase) trimmed to the given length.</br>
	 * This is useful to compare e.g. subroutine names which only have 32 significant characters.</br>
	 * The resulting name will not contain trailing space.
	 */
	public String trimmedSymbolName(int maxLength)
	{
		var name = symbolName();
		if (name.length() < maxLength)
		{
			return name;
		}

		return name.substring(0, maxLength).trim();
	}

	public SyntaxToken(SyntaxKind kind, int offset, int lineOffset, int line, String source, Path filePath)
	{
		this.kind = kind;
		this.offset = offset;
		this.offsetInLine = lineOffset;
		this.line = line;
		this.source = source;
		this.filePath = filePath;
	}

	@Pure
	public SyntaxToken withKind(SyntaxKind newKind)
	{
		var newToken = new SyntaxToken(
			newKind,
			offset,
			offsetInLine,
			line,
			source,
			filePath
		);
		newToken.setDiagnosticPosition(diagnosticPosition);
		if (kind != newKind)
		{
			newToken.originalKind = kind;
		}
		return newToken;
	}

	@Pure
	public SyntaxToken combine(SyntaxToken other, SyntaxKind kind)
	{
		return new SyntaxToken(
			kind,
			offset,
			offsetInLine,
			line,
			source + other.source,
			filePath
		);
	}

	/**
	 * If the Token was constructed from a different token, with just the kind adjusted, then this will return the
	 * original {@link SyntaxKind} it originated from.<br/>
	 * This might be the case for e.g. keywords that are used as identifiers.
	 */
	public Optional<SyntaxKind> originalKind()
	{
		return Optional.ofNullable(originalKind);
	}

	@Override
	public String toString()
	{
		return String.format(
			"T[Kind=%s; Source='%s'; Offset=%d; Length=%d; Line=%d; LineOffset=%d]",
			kind,
			source,
			offset,
			length(),
			line,
			offsetInLine
		);
	}

	public boolean isQualified()
	{
		return kind == IDENTIFIER && symbolName().contains(".");
	}
}
