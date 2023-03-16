package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;

class DefineWorkfileHoverShould extends HoveringTest
{
	@Test
	void hoverWorkfileTypes()
	{
		assertHover("""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE WORK FILE 1 'path.txt' T${}$YPE 'ASCII'
			END
			""", """
Specifies the type of the work file.

*Possible types:*
- `DEFAULT` - Determines the file type from the file extension.
- `TRANSFER`- Data connection for ENTIRE CONNECTION.
- `SAG` - Binary format.
- `ASCII` - Text files terminated by a carriage return (windows) and line feed.
- `ASCII-COMPRESSED` - ASCII but all whitespace removed.
- `ENTIRECONNECTION` - Data connection for ENTIRE CONNECTION.
- `UNFORMATTED` - Unformatted file, no format information is implied.
- `PORTABLE` - Portable file type between endian types.
- `CSV` - CSV file where each record is written to its own line.""");
	}

	@Test
	void hoverWorkfileAttributes()
	{
		assertHover("""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE WORK FILE 1 'path.txt' ATTRIB${}$UTES 'APPEND'
			END
			""", """
Specifies file attributes for the defined work file.

Multiple attributes can be specified by separating them by comma or whitespace. There are 4 categories of attributes that can be applied. *If there are two attributes of the same category specified, only the last one is applied.*

*Appending:*
- `NOAPPEND `- File is written from the start. Default value
- `APPEND `- Content is appended to the given file.

*Keep/Delete:*
- `KEEP `- Keep the file when on CLOSE. Default value
- `DELETE `- Delete the work file on CLOSE.

*Byte Order Mark:*
- `NOBOM `- Don't add a BOM. Default value
- `BOM `- Add a BOM in front of the content.

*Carriage return:*
- `REMOVECR` - Remove carriage return characters on ASCII files. Default value
- `KEEPCR` - Keep carriage return characters.""");
	}
}
