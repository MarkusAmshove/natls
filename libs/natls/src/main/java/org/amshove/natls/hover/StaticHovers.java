package org.amshove.natls.hover;

import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.eclipse.lsp4j.Hover;

class StaticHovers
{
	static final Hover WORKFILE_TYPE_HOVER;
	static final Hover WORKFILE_ATTRIBUTE_HOVER;
	static final Hover MASK_HOVER;

	static
	{
		{
			var contentBuilder = MarkupContentBuilderFactory.newBuilder();
			contentBuilder.appendParagraph("Specifies the type of the work file.");
			contentBuilder.appendSection(
				"Possible types", b -> b
					.appendBullet("`DEFAULT` - Determines the file type from the file extension.")
					.appendBullet("`TRANSFER`- Data connection for ENTIRE CONNECTION.")
					.appendBullet("`SAG` - Binary format.")
					.appendBullet("`ASCII` - Text files terminated by a carriage return (windows) and line feed.")
					.appendBullet("`ASCII-COMPRESSED` - ASCII but all whitespace removed.")
					.appendBullet("`ENTIRECONNECTION` - Data connection for ENTIRE CONNECTION.")
					.appendBullet("`UNFORMATTED` - Unformatted file, no format information is implied.")
					.appendBullet("`PORTABLE` - Portable file type between endian types.")
					.appendBullet("`CSV` - CSV file where each record is written to its own line.")
			);
			WORKFILE_TYPE_HOVER = new Hover(contentBuilder.build());
		}

		{
			var contentBuilder = MarkupContentBuilderFactory.newBuilder();
			contentBuilder.appendParagraph("Specifies file attributes for the defined work file.");
			contentBuilder.append("Multiple attributes can be specified by separating them by comma or whitespace. ");
			contentBuilder.append("There are 4 categories of attributes that can be applied. ");
			contentBuilder.appendItalic("If there are two attributes of the same category specified, only the last one is applied.");
			contentBuilder.appendNewline();
			contentBuilder.appendSection(
				"Appending", b -> b
					.appendBullet("`NOAPPEND `- File is written from the start. Default value")
					.appendBullet("`APPEND `- Content is appended to the given file.")
			);
			contentBuilder.appendSection(
				"Keep/Delete", b -> b
					.appendBullet("`KEEP `- Keep the file when on CLOSE. Default value")
					.appendBullet("`DELETE `- Delete the work file on CLOSE.")
			);
			contentBuilder.appendSection(
				"Byte Order Mark", b -> b
					.appendBullet("`NOBOM `- Don't add a BOM. Default value")
					.appendBullet("`BOM `- Add a BOM in front of the content.")
			);
			contentBuilder.appendSection(
				"Carriage return", b -> b
					.appendBullet("`REMOVECR` - Remove carriage return characters on ASCII files. Default value")
					.appendBullet("`KEEPCR` - Keep carriage return characters.")
			);

			WORKFILE_ATTRIBUTE_HOVER = new Hover(contentBuilder.build());
		}

		{
			var contentBuilder = MarkupContentBuilderFactory.newBuilder();
			contentBuilder.appendParagraph("Tests if the value matches a mask/pattern");
			contentBuilder.appendSection(
				"Possible characters in MASK definitions", b -> b
					.appendNewline()
					.appendStrong(". ? _")
					.append(
						"  These characters (dot, question mark, underscore) indicate a single position that will not be checked"
					)
					.appendNewline()
					.appendStrong("* %")
					.append("  Both symbols indicate any number of positions that will not be checked")
					.appendNewline()
					.appendStrong("/")
					.append("  End of value (or the end before blanks), comparable to $ in regular expressions")
					.appendNewline()
					.appendStrong("X")
					.append("  Same character as the one at the same position of the operand after the mask definition")
					.appendNewline()
					.appendStrong("A")
					.append("  Alphabetical character, upper or lower case")
					.appendNewline()
					.appendStrong("L")
					.append("  Alphabetical character, lower case only (a-z)")
					.appendNewline()
					.appendStrong("U")
					.append("  Alphabetical character, upper case only (A-Z)")
					.appendNewline()
					.appendStrong("'c'")
					.append("  Literal value of ").appendStrong("c").append(". Can multiple characters")
					.appendNewline()
					.appendStrong("C")
					.append("  Alphabetical character, numeric character or blank")
					.appendNewline()
					.appendStrong("N")
					.append("  Single digit")
					.appendNewline()
					.appendStrong("n...")
					.append("  Digit range ").appendInlineCode("0-n")
					.appendNewline()
					.appendStrong("n1-n2 n1:n2")
					.append("  Numeric range ").appendInlineCode("n1-n2")
					.appendNewline()
					.appendStrong("DD")
					.append("  Valid day number, 01-31, dependent on the values of MM and YY if specified")
					.appendNewline()
					.appendStrong("MM")
					.append("  Valid month number, 01-12")
					.appendNewline()
					.appendStrong("YY")
					.append("  Valid year, 00-99")
					.appendNewline()
					.appendStrong("YYYY")
					.append("  Valid year, 0000-2699")
					.appendNewline()
					.appendStrong("JJJ")
					.append("  Julian day, 001-366, dependent on YY or YYYY")
					.appendNewline()
					.appendStrong("P")
					.append("  Displayable character (U, L, N or S)")
					.appendStrong("S")
					.append("  Special character according to the active character set")
					.appendStrong("H")
					.append("  Hexadecimal content, A-F and 0-9 ")
					.appendNewline()
					.appendStrong("Z")
					.append("  Character whose left half-byte or whose right half-byte is hexadecimal")
					.appendNewline()
			);

			MASK_HOVER = new Hover(contentBuilder.build());
		}
	}
}
