package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.progress.NullProgressMonitor;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.IRedefinitionNode;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.lang.System.err;

public class DiagramGenerator
{
	private ActualFilesystem filesystem;
	private final StringBuilder mermaidDiagram = new StringBuilder();
	private final StringBuilder plantumlDiagram = new StringBuilder();

	public void run(Path packagePath)
	{
		this.filesystem = new ActualFilesystem();
		if(!packagePath.toFile().exists())
		{
			err.printf("Path %s not found!%n", packagePath);
			return;
		}

		var maybeProjectFile = locateProjectFile(packagePath);
		if(maybeProjectFile.isEmpty())
		{
			err.println("Project file could not be located");
			return;
		}

		var projectFile = maybeProjectFile.get();
		var service = new NaturalLanguageService();
		service.indexProject(projectFile.getParent(), new NullProgressMonitor());

		var relevantFileTypes = List.of(NaturalFileType.SUBROUTINE, NaturalFileType.COPYCODE, NaturalFileType.FUNCTION, NaturalFileType.PROGRAM, NaturalFileType.SUBPROGRAM);
		var filesPerPackage = new HashMap<String, List<Path>>();
		filesystem.streamFilesRecursively(packagePath)
			.forEach(filepath -> {
				var filename = filepath.getFileName().toString();
				if(filepath.getParent().getFileName().toString().startsWith("Test"))
				{
					return;
				}
				if(filename.startsWith("TC") || filename.startsWith("TS") || filename.startsWith("ASS"))
				{
					return;
				}
				if(!relevantFileTypes.contains(service.findNaturalFile(filepath).getNaturalFile().getFiletype()))
				{
					return;
				}
				filesPerPackage.computeIfAbsent(filepath.getParent().getFileName().toString(), p -> new ArrayList<>()).add(filepath);
			});

		appendMermaid("flowchart");
		appendPlant("@startuml");
		appendPlantLegend();
		filesPerPackage.forEach((key, value) -> {
			appendMermaid("    subgraph " + key);
			appendPlant("package " + key + " {");
			value.forEach(p -> {
				appendMermaid("        " + withoutExtension(p));
				var naturalFile = service.findNaturalFile(p);
				appendPlant(plantumlType(naturalFile) + " " + withoutExtension(p));
				appendPlantParameter(naturalFile);
			});
			appendMermaid("    end");
			appendPlant("}");
		});

		filesPerPackage.values().stream().flatMap(List::stream)
				.forEach(p -> {
					var naturalFile = service.findNaturalFile(p);
					naturalFile.getOutgoingReferences().forEach(called -> {
						if(called.getPath().startsWith(packagePath) && filesPerPackage.values().stream().anyMatch(l -> l.contains(called.getPath())))
						{
							appendMermaid("    " + withoutExtension(p) + "-->" + withoutExtension(called.getPath()));
							appendPlant("" + withoutExtension(p) + " --> " + withoutExtension(called.getPath()));
						}
					});
				});

		appendPlant("@enduml");
		try
		{
			Files.writeString(Path.of("D:", "mermaid.txt"), mermaidDiagram.toString());
			Files.writeString(Path.of("D:", "plant.txt"), plantumlDiagram.toString());
		}
		catch (IOException e)
		{
		}
	}

	private void appendPlantLegend()
	{
		appendPlant("""
			legend
|= symbol |= meaning  |
|  C | (Sub-)Program |
|  @ | Subroutine / Function |
|  I | Copycode |
|  Arrow up     | Using PDA |
|  Square     | Parameter variable |
endlegend
			""");
	}

	private String plantumlType(LanguageServerFile f)
	{
		return switch(f.getNaturalFile().getFiletype()) {

			case SUBPROGRAM, PROGRAM -> "class";
			case SUBROUTINE, FUNCTION -> "annotation";
			case COPYCODE -> "interface";
			default -> throw new RuntimeException("Unsupported file type " + f.getNaturalFile().getFiletype());
		};
	}

	private void appendPlantParameter(LanguageServerFile naturalFile)
	{
		var module = naturalFile.module();
		if(module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			hasDefineData.defineData().parameterUsings().forEach(u -> appendPlant(withoutExtension(naturalFile.getPath()) + " : ~" + u.target().symbolName()));
			hasDefineData.defineData().variables().stream()
				.filter(v -> v.level() == 1)
				.filter(v -> !(v instanceof IRedefinitionNode))
				.filter(v -> v.scope() == VariableScope.PARAMETER && v.position().filePath().equals(naturalFile.getPath()))
				.forEach(v -> appendPlant(withoutExtension(naturalFile.getPath()) + " : -" + v.name()));
		}
	}

	private void appendPlant(String s)
	{
		plantumlDiagram.append(s).append("\n");
	}

	private String withoutExtension(Path path)
	{
		return path.getFileName().toString().split("\\.")[0];
	}

	private Optional<Path> locateProjectFile(Path searchStart)
	{
		if(searchStart.getRoot().equals(searchStart))
		{
			return Optional.empty();
		}

		var maybeFile = filesystem.findNaturalProjectFile(searchStart);
		if(maybeFile.isPresent())
		{
			return maybeFile;
		}

		return locateProjectFile(searchStart.getParent());
	}

	private void appendMermaid(String line)
	{
		mermaidDiagram.append(line).append("\n");
	}
}
