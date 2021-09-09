package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.parsing.ddm.text.LinewiseTextScanner;

class SqlFieldParser extends FieldParser
{
	@Override
	protected double parseLength(LinewiseTextScanner scanner)
	{
		try
		{
			// In most cases, the length is defined just as in ADABAS-DDms
			return super.parseLength(scanner);
		}
		catch (NaturalParseException e)
		{
			// Otherwise, it is in the next line with `LE=`, `DY`, etc
			return parseSqlLength(scanner);
		}
	}

	private double parseSqlLength(LinewiseTextScanner scanner)
	{
		scanner.advance(); // Skip over field line
		while (!containsSqlLengthInformation(scanner.peek()))
		{
			scanner.advance();
		}

		String lengthinformation = scanner.peek();
		if (lengthinformation.contains("LE="))
		{
			return Double.parseDouble(lengthinformation.replace("LE=", "").trim());
		}

		scanner.advance(); // skip over CLOB comment
		return 9999; // DYNAMIC, e.g. CLOB
	}

	private static boolean containsSqlLengthInformation(String line)
	{
		return line.contains("LE=") || line.contains("DY");
	}
}
