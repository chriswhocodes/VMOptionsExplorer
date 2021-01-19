/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SwitchInfoMap extends TreeMap<String, SwitchInfo>
{
	public Set<String> getSwitchNames()
	{
		Set<String> result = new TreeSet<>();

		for (SwitchInfo switchInfo : values())
		{
			result.add(switchInfo.getName());
		}

		return result;
	}

	public SwitchInfo put(String key, SwitchInfo value)
	{
		return null;
	}
}