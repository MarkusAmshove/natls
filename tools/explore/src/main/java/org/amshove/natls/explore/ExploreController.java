package org.amshove.natls.explore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.TokenList;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyledTextArea;

import java.nio.file.Path;

public class ExploreController
{
	@FXML
	public Button loadFileButton;
	@FXML
	public Button parseButton;
	@FXML
	public CodeArea tokenArea;
	@FXML
	public CodeArea codeArea;
	@FXML
	public TreeView nodeView;

	public void initialize()
	{
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		tokenArea.setParagraphGraphicFactory(LineNumberFactory.get(tokenArea));
	}

	public void onParseButton(ActionEvent actionEvent)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(codeArea.getText(), Path.of("MODULE.NSN"));
		renderTokens(tokens);
	}

	private void renderTokens(TokenList tokens)
	{
		tokenArea.clear();
		var currentLine = 0;
		while(!tokens.isAtEnd())
		{
			var token = tokens.peek();
			if(token.diagnosticPosition().line() > currentLine)
			{
				while(currentLine != token.diagnosticPosition().line())
				{
					currentLine++;
					tokenArea.appendText("\n");
				}
			}
			tokenArea.appendText("%s[%s]".formatted(token.source(), token.kind()));
			tokens.advance();
		}
	}
}
