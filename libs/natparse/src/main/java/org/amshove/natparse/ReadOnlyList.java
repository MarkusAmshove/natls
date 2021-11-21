package org.amshove.natparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class ReadOnlyList<T> implements Iterable<T>
{
	public static ReadOnlyList EMPTY = ReadOnlyList.from(new ArrayList<>());

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

	public T last()
	{
		return collection.get(size() - 1);
	}

	public T first()
	{
		return collection.get(0);
	}

	public T get(int index)
	{
		return collection.get(index);
	}

	public static <T> Collector<T, ?, ReadOnlyList<T>> collector()
	{
		return new ReadOnlyListCollector<>();
	}

	private static class ReadOnlyListCollector<T> implements Collector<T, ArrayList<T>, ReadOnlyList<T>>
	{

		@Override
		public Supplier<ArrayList<T>> supplier()
		{
			return ArrayList::new;
		}

		@Override
		public BiConsumer<ArrayList<T>, T> accumulator()
		{
			return ArrayList::add;
		}

		@Override
		public BinaryOperator<ArrayList<T>> combiner()
		{
			return (left, right) -> {
				left.addAll(right);
				return left;
			};
		}

		@Override
		public Function<ArrayList<T>, ReadOnlyList<T>> finisher()
		{
			return ReadOnlyList::from;
		}

		@Override
		public Set<Characteristics> characteristics()
		{
			return Set.of();
		}
	}
}
