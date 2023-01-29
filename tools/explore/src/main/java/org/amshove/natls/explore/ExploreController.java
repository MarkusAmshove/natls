package org.amshove.natls.explore;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.StatementListParser;
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
	public TreeView<NodeItem> nodeView;
	public SplitPane codePane;

	public void initialize()
	{
		VBox.setVgrow(codePane, Priority.ALWAYS);
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		tokenArea.setParagraphGraphicFactory(LineNumberFactory.get(tokenArea));
		nodeView.getSelectionModel().selectedItemProperty().addListener(this::syntaxNodeSelected);
	}

	@SuppressWarnings("unchecked")
	private void syntaxNodeSelected(ObservableValue<?> observable, Object oldValue, Object newValue)
	{
		var item = (TreeItem<NodeItem>) newValue;

		if(item == null)
		{
			return;
		}

		var node = (ISyntaxNode) item.getValue().node;
		if(node == null)
		{
			return;
		}

		codeArea.moveTo(node.diagnosticPosition().offset());
		var endOffset = node.diagnosticPosition().totalEndOffset();
		if(node instanceof IIncludeNode)
		{
			endOffset = node.descendants().get(node.descendants().size() - 2).diagnosticPosition().totalEndOffset();
		}
		else if(node instanceof ITokenNode)
		{
			endOffset = node.diagnosticPosition().totalEndOffset();
		}
		else
		{
			if(node.descendants().last() instanceof IStatementListNode)
			{
				return;
			}

			endOffset = node.descendants().last().diagnosticPosition().totalEndOffset();
		}

		codeArea.moveTo(node.diagnosticPosition().offset());
		codeArea.requestFollowCaret();
		codeArea.selectRange(node.diagnosticPosition().offset(), endOffset);
		tokenArea.scrollToPixel(codeArea.getEstimatedScrollX(), codeArea.getEstimatedScrollY());
	}

	public void onParseButton()
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(codeArea.getText(), Path.of("MODULE.NSN"));
		renderTokens(tokens);

		renderNodes(tokens);
	}

	private void renderNodes(TokenList tokens)
	{
		nodeView.setRoot(null);
		var root = new TreeItem<>(new NodeItem(null));
		nodeView.setRoot(root);

		tokens.rollback();
		if(tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA)
		{
			var defineDataParser = new DefineDataParser(null);
			var ddm = defineDataParser.parse(tokens).result();
			addNodesRecursive(ddm, root);
		}

		var statementParser = new StatementListParser(null);
		var statements = statementParser.parse(tokens);
		for (var statement : statements.result().statements())
		{
			addNodesRecursive(statement, root);
		}

	}

	private void addNodesRecursive(ISyntaxTree node, TreeItem<NodeItem> parent)
	{
		var root = new TreeItem<>(new NodeItem(node));

		for (var descendant : node.descendants())
		{
			addNodesRecursive(descendant, root);
		}

		parent.getChildren().add(root);
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

	private record NodeItem(ISyntaxTree node)
	{
		@Override
		public String toString()
		{
			var tokenSource = "";
			if(node != null)
			{
				tokenSource = node.directDescendantsOfType(ITokenNode.class).findFirst().map(tn -> tn.token().source()).orElse("?");
			}
			if(node instanceof ITokenNode tokenNode)
			{
				tokenSource = tokenNode.token().source();
			}
			return node != null ? "%s (%s)".formatted(node.getClass().getSimpleName(), tokenSource) : "root";
		}
	}
}
