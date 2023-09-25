package org.amshove.natls.workspace;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natls.project.ModuleReferenceCache;
import org.amshove.natls.project.ParseStrategy;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.FileRename;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.*;

public class RenameFileHandler
{
	public WorkspaceEdit handleFileRename(List<FileRename> renames, LanguageServerProject project)
	{
		var edit = new WorkspaceEdit();
		var fileChanges = new HashMap<String, List<TextEdit>>();

		for (var rename : renames)
		{
			var newNameRaw = LspUtil.uriToPath(rename.getNewUri()).getFileName().toString().split("\\.")[0];

			var oldPath = LspUtil.uriToPath(rename.getOldUri());
			var oldFile = project.findFile(oldPath);

			if (oldFile == null && project.findFile(LspUtil.uriToPath(rename.getNewUri())) != null)
			{
				// old file doesn't exist but new one does.
				// it is already handled, because we're retriggered after RenameFile operation (e.g. renaming function from function name)
				continue;
			}

			if (Objects.requireNonNull(oldFile).getType() == NaturalFileType.SUBROUTINE)
			{
				// Rename of file doesn't change the referable name of external subroutines
				continue;
			}

			if (newNameRaw.toUpperCase().equals(oldFile.getReferableName()))
			{
				// The file has just been moved, the name remains the same
				continue;
			}

			oldFile.parse(ParseStrategy.WITH_CALLERS);
			var oldModule = oldFile.module();

			var newName = switch (oldModule.file().getFiletype())
			{
				case SUBPROGRAM, PROGRAM -> "'%s'".formatted(newNameRaw);
				default -> newNameRaw;
			};

			fileChanges.putAll(computeCallerRenames(newName, oldFile, oldModule.callers()));

			if (oldFile.getType() == NaturalFileType.FUNCTION && oldModule instanceof IFunction func)
			{
				var changes = fileChanges.computeIfAbsent(oldModule.file().getPath().toUri().toString(), u -> new ArrayList<>());

				var textEdit = new TextEdit();
				textEdit.setNewText(newName);
				textEdit.setRange(LspUtil.toRange(func.functionName()));
				changes.add(textEdit);

				var funcNameVariable = func.defineData().findVariable(oldFile.getReferableName());
				if (funcNameVariable != null)
				{
					for (var reference : funcNameVariable.references())
					{
						var refEdit = new TextEdit();
						refEdit.setNewText(newName);
						refEdit.setRange(LspUtil.toRange(reference.referencingToken()));
						changes.add(refEdit);
					}
				}
			}

			// This is here and not in didRename because the client changes the files after this call.
			// The changed files then can't reference the new file if `renameFile` wasn't run.
			// Moving this to `didRename`, which sounds fine, can't take up the references to the file anymore,
			// because they're gone.
			// They're gone because the changes in the referencing files cause a reparse.
			project.renameFile(rename.getOldUri(), rename.getNewUri());
		}

		edit.setChanges(fileChanges);

		return edit;
	}

	public static Map<String, List<TextEdit>> computeCallerRenames(String newName, LanguageServerFile oldFileOfModule, ReadOnlyList<IModuleReferencingNode> callers)
	{
		var callerChanges = new HashMap<String, List<TextEdit>>();
		for (var caller : callers)
		{
			var changes = callerChanges.computeIfAbsent(caller.referencingToken().filePath().toUri().toString(), u -> new ArrayList<>());
			var textEdit = new TextEdit();
			textEdit.setNewText(newName);
			textEdit.setRange(LspUtil.toRange(caller.referencingToken()));
			changes.add(textEdit);
		}

		for (var cachedPosition : ModuleReferenceCache.retrieveCachedPositions(oldFileOfModule))
		{
			var changes = callerChanges.computeIfAbsent(cachedPosition.filePath().toUri().toString(), u -> new ArrayList<>());
			var textEdit = new TextEdit();
			textEdit.setNewText(newName);
			textEdit.setRange(LspUtil.toRange(cachedPosition));
			changes.add(textEdit);
		}

		return callerChanges;
	}
}
