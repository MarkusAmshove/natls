package org.amshove.natparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiValueMap<TKey, TValue>
{
	private final HashMap<TKey, List<TValue>> backingMap = new HashMap<>();

	public void put(TKey key, TValue value)
	{
		backingMap.computeIfAbsent(key, _k -> new ArrayList<>()).add(value);
	}

	public List<TValue> get(TKey key)
	{
		return backingMap.get(key);
	}

	public boolean containsKey(TKey key)
	{
		return backingMap.containsKey(key);
	}

	public Set<TKey> keys()
	{
		return backingMap.keySet();
	}
}
