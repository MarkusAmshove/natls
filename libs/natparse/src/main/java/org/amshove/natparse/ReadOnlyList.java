package org.amshove.natparse;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadOnlyList<T> implements Iterable<T>
{
	private static final ReadOnlyList EMPTY = ReadOnlyList.from(Collections.emptyList());

	private final ArrayList<T> collection;

	private ReadOnlyList(Collection<T> collection)
	{
		this.collection = new ArrayList<>(collection);
	}

	public static <T> ReadOnlyList<T> from(Collection<T> collection)
	{
		if(collection == null)
		{
			return empty();
		}
		return new ReadOnlyList<>(collection);
	}

	@SuppressWarnings("unchecked")
	public static <T> ReadOnlyList<T> empty()
	{
		return (ReadOnlyList<T>) EMPTY;
	}

	public static <T> ReadOnlyList<T> of(T item)
	{
		return ReadOnlyList.from(List.of(item));
	}

	@SafeVarargs
	public static <T> ReadOnlyList<T> of(T... items)
	{
		if(items.length == 0)
		{
			return empty();
		}

		var includedItems = new ArrayList<T>(items.length);
		Collections.addAll(includedItems, items);
		return ReadOnlyList.from(includedItems);
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

	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	public boolean hasItems()
	{
		return !isEmpty();
	}

	public List<T> toList()
	{
		return new ArrayList<>(collection);
	}

	@Override
	public String toString()
	{
		return "ReadOnlyList{" + collection.stream().map(T::toString).collect(Collectors.joining(",")) + '}';
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
