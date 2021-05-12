/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.chrisnewland.vmoe.SwitchInfo.PREFIX_XX;

public class GraalNativeImageSwitchParser extends AbstractSwitchParser
{
	private static final String TYPE_BOOLEAN = "bool";

	private static final char SWITCH_HOSTED = 'H';
	private static final char SWITCH_RUNTIME = 'R';

	private static final String ELLIPSIS = "...";
	private static final String DEFAULT_IN_DESCRIPTION= "Default:";

	@Override
	public SwitchInfoMap process(File vmPath) throws IOException
	{
		switchMap = new SwitchInfoMap();

		List<String> lines = Files.readAllLines(vmPath.toPath());

		StringBuilder builder = new StringBuilder();

		SwitchInfo info = null;

		for (String line : lines)
		{
			String trimmed = line.trim();

			String defaultValue = null;

			//System.out.println("line:" + trimmed);

			if (isGraalNewSwitch(trimmed))
			{
				if (info != null)
				{
					if (builder.length() > 0)
					{
						String description = builder.toString();

						int defaultPos = description.indexOf(DEFAULT_IN_DESCRIPTION);

						if (defaultPos != -1)
						{
							String defaultPart = description.substring(defaultPos+DEFAULT_IN_DESCRIPTION.length());

							description = description.substring(0, defaultPos).trim();

							if (TYPE_BOOLEAN.equals(info.getType()))
							{
								defaultValue = defaultPart.contains("enabled") ? "true" : "false";
							}
							else
							{
								defaultValue = defaultPart.trim();
								info.setType(getType(defaultValue));
							}

							info.setDefaultValue(defaultValue);
						}

						//System.out.println("Setting description on previous info '" + description + "'");

						info.setDescription(description);
						builder.setLength(0);
					}

					//System.out.println("COMPLETED: " + info.toString());
				}

				//System.out.println("starting new switch");

				String[] parts = trimmed.split("\\s+");

				String name = parts[0];

				char hostedOrRuntime = name.charAt(1);

				int colonPos = name.indexOf(':');

				String type = null;

				if (colonPos != -1)
				{
					name = name.substring(colonPos + 1);

					if (name.length() > 0 && '±' == name.charAt(0))
					{
						name = name.substring(1);
						type = TYPE_BOOLEAN;
					}
				}

				//System.out.println("name " + name);

				int equalsPos = name.indexOf('=');

				if (equalsPos != -1)
				{
					defaultValue = name.substring(equalsPos + 1);
					name = name.substring(0, equalsPos);

					//System.out.println("name " + name);
					//System.out.println("defaultValue " + defaultValue);
				}

				for (int i = 1; i < parts.length; i++)
				{
					builder.append(parts[i]).append(' ');
				}

				if (ELLIPSIS.equalsIgnoreCase(defaultValue))
				{
					defaultValue = null;
				}

				// native-image puts it's runtime path into the output
				if (defaultValue != null && defaultValue.contains("VMOptionsExplorer"))
				{
					defaultValue = null;
				}

				info = new SwitchInfo(PREFIX_XX, name);
				info.setDefaultValue(defaultValue);

				if (type == null)
				{
					type = getType(defaultValue);
				}

				info.setType(type);

				if (hostedOrRuntime == SWITCH_RUNTIME)
				{
					info.setAvailability("Runtime");
				}
				else if (hostedOrRuntime == SWITCH_HOSTED)
				{
					info.setAvailability("Hosted");
				}

				switchMap.put(info.getKey(), info);
			}
			else
			{
				//System.out.println("Adding '" + line + "' to description");

				builder.append("\n").append(line.trim());
			}
		}

		// final entry
		if (info != null && builder.length() > 0)
		{
			String description = builder.toString();

			//System.out.println("Setting description on info '" + description + "'");

			info.setDescription(description);
			builder.setLength(0);
		}

		return switchMap;
	}

	private String getType(String value)
	{
		boolean alpha = false;
		boolean numeric = false;
		boolean dot = false;

		if (value != null)
		{
			for (int i = 0; i < value.length(); i++)
			{
				char c = value.charAt(i);

				if (Character.isAlphabetic(c))
				{
					alpha = true;
				}
				else if (Character.isDigit(c))
				{
					numeric = true;
				}
				else if (c == '.')
				{
					dot = true;
				}
			}
		}

		if (alpha)
		{
			return "String";
		}
		else if (numeric)
		{
			if (dot)
			{
				return "double";
			}
			else
			{
				return "int";
			}
		}
		else
		{
			return "";
		}
	}

	private boolean isGraalNewSwitch(String line)
	{
		return line.startsWith("-");
	}
}