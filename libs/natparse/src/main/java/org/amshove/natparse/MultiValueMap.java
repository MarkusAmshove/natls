package org.amshove.natparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiValueMap<KEY, VALUE>
{
	private final HashMap<KEY, List<VALUE>> backingMap = new HashMap<>();

	public void put(KEY key, VALUE value)
	{
		backingMap.computeIfAbsent(key, __ -> new ArrayList<>()).add(value);
	}

	public List<VALUE> get(KEY key)
	{
		return backingMap.get(key);
	}

	public boolean containsKey(KEY key)
	{
		return backingMap.containsKey(key);
	}

	public Set<KEY> keys()
	{
		return backingMap.keySet();
	}
}
