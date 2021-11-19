package org.amshove.natparse;

public interface IPosition
{
	int offset();
	int offsetInLine();
	int line();
	int length();
}
