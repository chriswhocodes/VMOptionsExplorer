/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch.parser;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.chrisnewland.vmswitch.SwitchInfo;

public abstract class AbstractSwitchParser implements ISwitchParser
{
	protected Map<String, SwitchInfo> switchMap;

	protected List<String> explodeLine(String line)
	{
		List<String> result = new ArrayList<>();

		final int length = line.length();

		StringBuilder builder = new StringBuilder();

		boolean inQuotes = false;

		for (int i = 0; i < length; i++)
		{
			char c = line.charAt(i);

			if (inQuotes)
			{
				if (c == '"')
				{
					inQuotes = false;
					String part = builder.toString().trim();
					// System.out.println("part: " + part);
					if (part.length() > 0)
					{
						result.add(part);
					}
					builder.delete(0, builder.length());
				}
				else
				{
					builder.append(c);
				}
			}
			else if (c == '"')
			{
				inQuotes = true;
			}
			else if (c == ',')
			{
				String part = builder.toString().trim();
				// System.out.println("part: " + part);
				if (part.length() > 0)
				{
					result.add(part);
				}
				builder.delete(0, builder.length());
			}
			else
			{
				builder.append(c);
			}
		}

		if (builder.length() > 0)
		{
			String part = builder.toString().trim();
			// System.out.println("part: " + part);
			if (part.length() > 0)
			{
				result.add(part);
			}
			builder.delete(0, builder.length());
		}

		return result;
	}

	protected void setFieldsFromPath(SwitchInfo info, File file)
	{
		Path path = file.toPath();

		final int pathParts = path.getNameCount();

		for (int i = 0; i < pathParts - 1; i++)
		{
			String part = path.getName(i).toFile().getName();
			String nextPart = path.getName(i + 1).toFile().getName();

			switch (part)
			{
			case "os":
				info.setOs(nextPart);
				break;
			case "cpu":
				info.setCpu(nextPart);
				break;
			case "vm":
				if (nextPart.startsWith("c1"))
				{
					info.setComponent("c1");
				}
				else if (nextPart.startsWith("c2") || nextPart.startsWith("opto"))
				{
					info.setComponent("c2");
				}
				else if (nextPart.startsWith("shark"))
				{
					info.setComponent("shark");
				}
				else if ("gc_implementation".equals(nextPart))
				{
					info.setComponent("gc");
				}
				else if (!nextPart.contains(".hpp"))
				{
					info.setComponent(nextPart);
				}
				break;
			case "os_cpu":
				String[] nextParts = nextPart.split("_");
				info.setOs(nextParts[0]);
				info.setCpu(nextParts[1]);
				break;
			}
		}
	}

	protected String getBetween(String input, String start, String end)
	{
		String result = null;

		int startIndex = input.indexOf(start);

		if (startIndex != -1)
		{
			int endIndex = input.lastIndexOf(end);

			if (endIndex != -1)
			{

				result = input.substring(startIndex + start.length(), endIndex);
			}
		}

		return result;
	}

	protected void removeSwitch(String name, Map<String, SwitchInfo> switchMap)
	{
		Iterator<String> iter = switchMap.keySet().iterator();

		while (iter.hasNext())
		{
			SwitchInfo info = switchMap.get(iter.next());

			if (name.equals(info.getName()))
			{
				iter.remove();
			}
		}
	}
}
