package org.amshove.natparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class ReadOnlyList<T> implements Iterable<T>
{
	private final ArrayList<T> collection;

	private ReadOnlyList(Collection<T> collection)
	{
		this.collection = new ArrayList<>(collection);
	}

	public static <T> ReadOnlyList<T> from(Collection<T> collection)
	{
		return new ReadOnlyList<>(collection);
	}

	@Override
	public Iterator<T> iterator()
	{
		return collection.iterator();
	}

	public Stream<T> stream()
	{
		return collection.stream();
	}

	public int size()
	{
		return collection.size();
	}
}
