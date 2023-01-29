package org.amshove.natls.explore;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.TokenList;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExploreController
{
	public Button loadFileButton;
	public Button parseButton;
	public CodeArea tokenArea;
	public CodeArea codeArea;
	public TreeView nodeView;
	public SplitPane codePane;

	public void initialize()
	{
		VBox.setVgrow(codePane, Priority.ALWAYS);
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		tokenArea.setParagraphGraphicFactory(LineNumberFactory.get(tokenArea));
	}

	public void onParseButton()
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

	public void codeAreaKeyPressed(KeyEvent keyEvent)
	{
		if(keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.ENTER)
		{
			onParseButton();
		}
	}

	public void onLoadFileButton(ActionEvent event)
	{
		var stage = ((Node)event.getSource()).getScene().getWindow();
		var fileChooser = new FileChooser();
		var file = fileChooser.showOpenDialog(stage);
		try
		{
			var source = Files.readString(file.toPath());
			codeArea.clear();
			codeArea.appendText(source);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void codeAreaMouseClicked(MouseEvent mouseEvent)
	{
		tokenArea.scrollToPixel(codeArea.getEstimatedScrollX(), codeArea.getEstimatedScrollY());
	}
}
