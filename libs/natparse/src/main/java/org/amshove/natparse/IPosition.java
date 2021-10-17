package org.amshove.natparse;

public interface IPosition
{
	int offset();
	int offsetInLine();
	int currentLine();
	int length();
}
