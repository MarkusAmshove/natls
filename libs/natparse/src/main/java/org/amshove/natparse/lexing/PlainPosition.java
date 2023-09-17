package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;

import java.nio.file.Path;

public record PlainPosition(int offset, int offsetInLine, int line, int length, Path filePath) implements IPosition
{}
