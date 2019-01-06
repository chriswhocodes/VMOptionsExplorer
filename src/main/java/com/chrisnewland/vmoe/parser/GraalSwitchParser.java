/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.chrisnewland.vmoe.SwitchInfo;

public class GraalSwitchParser extends AbstractSwitchParser
{
	@Override
	public Map<String, SwitchInfo> process(File vmPath) throws IOException
	{
		switchMap = new TreeMap<>();
		
		List<String> lines = Files.readAllLines(vmPath.toPath());

		StringBuilder builder = new StringBuilder();

		SwitchInfo info = null;

		int descriptionIndent = 0;

		for (String line : lines)
		{
			String trimmed = line.trim();

			if (isGraalNewSwitch(trimmed))
			{
				String[] parts = trimmed.split("\\s+");

				String type = ParseUtil.getBetween(parts[3], "[", "]");
				String name = parts[0];

				if (name.indexOf('.') != -1)
				{
					name = name.substring(name.indexOf('.') + 1);
				}

				String defaultValue = parts[2];

				if (info != null && builder.length() > 0)
				{
					String description = builder.toString();

					info.setDescription("<pre>" + description + "</pre>");
					builder.setLength(0);
				}

				info = new SwitchInfo(name);
				info.setType(type);
				info.setDefaultValue(defaultValue);

				// System.out.println(info.toString());

				switchMap.put(info.getKey(), info);
			}
			else if (isGraalSection(line))
			{
				// System.out.println("Section : " + trimmed);
			}
			else
			{
				// System.out.println("line : " + line);

				if (builder.length() == 0)
				{
					descriptionIndent = 0;

					for (int i = 0; i < line.length(); i++)
					{
						if (line.charAt(i) == ' ')
						{
							descriptionIndent++;
						}
						else
						{
							break;
						}
					}
				}

				if (line.length() > descriptionIndent)
				{
					line = line.substring(descriptionIndent);
				}

				builder.append(line + "\n");
			}
		}

		if (info != null && builder.length() > 0)
		{
			String description = builder.toString();

			info.setDescription("<pre>" + description + "</pre>");
			builder.setLength(0);
		}
		
		return switchMap;

	}

	private boolean isGraalNewSwitch(String line)
	{
		return line.endsWith("]") && line.contains("[") && line.contains(".") && line.contains("=");
	}

	private boolean isGraalSection(String line)
	{
		return line.startsWith("[") && line.endsWith("]");
	}
}