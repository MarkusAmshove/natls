package org.amshove.natls.explore;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.StatementListParser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
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
	public TextField loadPathBox;

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

	private void addNodesRecursive(ISyntaxNode node, TreeItem<NodeItem> parent)
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

	public void shortcutPressed(KeyEvent keyEvent)
	{
		if(keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.ENTER)
		{
			onParseButton();
		}
		if(keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.O)
		{
			loadFile(codeArea.getScene().getWindow());
		}
		if(keyEvent.isAltDown() && keyEvent.getCode() == KeyCode.DOWN)
		{
			navigateNodeDown();
		}
		if(keyEvent.isAltDown() && keyEvent.getCode() == KeyCode.UP)
		{
			navigateNodeUp();
		}
	}

	public void onLoadFileButton(ActionEvent event)
	{
		var stage = ((Node)event.getSource()).getScene().getWindow();
		loadFile(stage);
	}

	private void loadFile(Window window)
	{
		if(!loadPathBox.getText().isEmpty())
		{
			loadFileFromPath(Path.of(loadPathBox.getText()));
			loadPathBox.clear();
		}
		else
		{
			var fileChooser = new FileChooser();
			var lastFolder = loadLastFolder();
			if (lastFolder != null && !lastFolder.isEmpty())
			{
				fileChooser.setInitialDirectory(new File(lastFolder));
			}

			var file = fileChooser.showOpenDialog(window);
			if (file == null)
			{
				return;
			}

			loadFileFromPath(file.toPath());
		}
	}

	private void loadFileFromPath(Path path)
	{
		try
		{
			var source = Files.readString(path);
			codeArea.clear();
			codeArea.appendText(source);
			saveLastFolder(path.getParent());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void saveLastFolder(Path parent)
	{
		try
		{
			Files.writeString(getStorePath(), parent.toAbsolutePath().toString());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String loadLastFolder()
	{
		try
		{
			return Files.readString(getStorePath());
		}
		catch (IOException e)
		{
			return "";
		}
	}

	private Path getStorePath()
	{
		return Path.of(System.getProperty("java.io.tmpdir"), ".natexplore");
	}

	public void codeAreaMouseClicked(MouseEvent mouseEvent)
	{
		tokenArea.scrollToPixel(codeArea.getEstimatedScrollX(), codeArea.getEstimatedScrollY());
		var caretOffset = codeArea.getCaretPosition();
		selectNodeAtCodeOffset(nodeView.getRoot(), caretOffset);
	}

	private void selectNodeAtCodeOffset(TreeItem<NodeItem> treeNode, int nodeOffset)
	{
		var node = treeNode.getValue();
		try
		{
			if (node.node != null && node.node.diagnosticPosition().offset() <= nodeOffset && node.node.diagnosticPosition().totalEndOffset() >= nodeOffset)
			{
				nodeView.getSelectionModel().select(treeNode);
				expandNodeView(nodeView.getRoot());
				nodeView.scrollTo(nodeView.getRow(treeNode));
			}
		}
		catch (Exception e)
		{
			// ignore
		}

		for (var child : treeNode.getChildren())
		{
			selectNodeAtCodeOffset(child, nodeOffset);
		}
	}

	private void expandNodeView(TreeItem<NodeItem> node)
	{
		node.setExpanded(true);
		for(var child : node.getChildren())
		{
			expandNodeView(child);
		}
	}

	public void navigateNodeDown()
	{
		navigateNode(1);
	}

	public void navigateNodeUp()
	{
		navigateNode(-1);
	}

	private void navigateNode(int offset)
	{
		if(nodeView.getRoot() == null)
		{
			return;
		}

		expandNodeView(nodeView.getRoot());
		var currentIndex = nodeView.getSelectionModel().getSelectedIndex();
		int newIndex = currentIndex + offset;
		nodeView.getSelectionModel().select(newIndex);
		nodeView.scrollTo(newIndex);
	}

	private record NodeItem(ISyntaxNode node)
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
