/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;

public class ZingSwitchParser extends AbstractSwitchParser
{

	enum OptionPart
	{
		TYPE, NAME, DEFAULT, AVAILABILITY
	}

	@Override
	public SwitchInfoMap process(File vmPath) throws IOException
	{
		switchMap = new SwitchInfoMap();

		List<String> lines = Files.readAllLines(vmPath.toPath());

		for (String line : lines)
		{
			String trimmed = line.trim();

			// uintx ARTADebugFlags = 0 {product}
			if (isValidSwitch(trimmed))
			{
				String name = "";
				String type = "";
				String defaultValue = "";
				String availability = "";

				OptionPart currentPart = OptionPart.TYPE;

				int lineLength = trimmed.length();

				int partStart = 0;

				for (int i = 0; i < lineLength; i++)
				{
					char c = trimmed.charAt(i);

					switch (currentPart)
					{
					case TYPE:
						if (c == ' ')
						{
							type = trimmed.substring(partStart, i).trim();
							partStart = i + 1;
							currentPart = OptionPart.NAME;
						}
						break;

					case NAME:
						if (c == '=')
						{
							name = trimmed.substring(partStart, i).trim();
							partStart = i + 1;
							currentPart = OptionPart.DEFAULT;
						}
						break;

					case DEFAULT:
						if (c == '{')
						{
							defaultValue = trimmed.substring(partStart, i).trim();
							partStart = i + 1;
							currentPart = OptionPart.AVAILABILITY;
						}
						break;

					case AVAILABILITY:
						if (c == '}')
						{
							availability = trimmed.substring(partStart, i).trim();
						}
						break;

					default:
						break;
					}
				}

				SwitchInfo info = new SwitchInfo(PREFIX_XX, name);

				info.setType(type);
				info.setDefaultValue(defaultValue);
				info.setAvailability(availability);

				// System.out.println(info.toString());

				switchMap.put(info.getKey(), info);
			}
		}

		return switchMap;
	}

	private boolean isValidSwitch(String line)
	{
		return line.contains("=") && line.contains("{") && line.contains("}");
	}
}