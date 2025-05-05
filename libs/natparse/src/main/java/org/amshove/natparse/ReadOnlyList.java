package org.amshove.natparse;

import javax.annotation.Nonnull;
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
	@SuppressWarnings("rawtypes")
	private static final ReadOnlyList EMPTY = ReadOnlyList.from(Collections.emptyList());

	private final List<T> collection;

	private ReadOnlyList(List<T> collection, boolean copy)
	{
		if (copy)
		{
			this.collection = new ArrayList<>(collection);
		}
		else
		{
			this.collection = collection;
		}
	}

	public static <T> ReadOnlyList<T> from(List<T> collection)
	{
		if (collection == null)
		{
			return empty();
		}
		return new ReadOnlyList<>(collection, true);
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
		if (items.length == 0)
		{
			return empty();
		}

		var includedItems = new ArrayList<T>(items.length);
		Collections.addAll(includedItems, items);
		return ReadOnlyList.from(includedItems);
	}

	@SafeVarargs
	public static <T> ReadOnlyList<T> ofExcludingNull(T... items)
	{
		if (items.length == 0)
		{
			return empty();
		}

		var includedItems = new ArrayList<T>(items.length);
		for (T item : items)
		{
			if (item != null)
			{
				includedItems.add(item);
			}
		}
		return ReadOnlyList.from(includedItems);
	}

	/**
	 * Creates a new ReadOnlyList in reversed order.
	 */
	public ReadOnlyList<T> reverse()
	{
		var newList = new ArrayList<T>();
		for (var i = collection.size() - 1; i >= 0; i--)
		{
			newList.add(collection.get(i));
		}
		return new ReadOnlyList<>(newList, false);
	}

	@Override
	public @Nonnull Iterator<T> iterator()
	{
		return collection.iterator();
	}

	public Stream<T> stream()
	{
		return collection.stream();
	}

	public boolean contains(T value)
	{
		return collection.contains(value);
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

	public int indexOf(T element)
	{
		return collection.indexOf(element);
	}

	public ReadOnlyList<T> subList(int fromIndex, int toIndex)
	{
		return new ReadOnlyList<>(collection.subList(fromIndex, toIndex), false);
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
			return (left, right) ->
			{
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
