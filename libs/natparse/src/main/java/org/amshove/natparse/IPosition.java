package org.amshove.natparse;

import java.nio.file.Path;

public interface IPosition
{
	int offset();
	int offsetInLine();
	int line();
	int length();
	Path filePath();
}
