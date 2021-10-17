package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.parsing.ddm.text.LinewiseTextScanner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SqlFieldParserShould
{
	@Test
	void parseTheLengthWithLengthOnNextLine()
	{
        var source = "  1 AC LONG-VARCHAR                      A       N   VARCHAR2(700)"
			+ "\n		LE=700";

        var field = parse(source);
		assertThat(field.length()).isEqualTo(700);
		assertThat(field.remark()).isEqualTo("VARCHAR2(700)");
	}

	@Test
	void parseDynamicSqlLengthForClobs()
	{
        var source = """
			1 AC DYANMIC-CLOB-FIELD                A       N D CLOB(4000)
			     SQLTYPE=CLOB
			     DY""".indent(2);

        var field = parse(source);
		assertThat(field.length()).isEqualTo(9999);
	}

	DdmField parse(String source)
	{
		return new SqlFieldParser().parse(new LinewiseTextScanner(source.split("\n")));
	}
}
