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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.chrisnewland.vmoe.SwitchInfo.PREFIX_X;

public class XUsageParser extends AbstractSwitchParser
{
	@Override public SwitchInfoMap process(File xUsageFile) throws IOException
	{
		switchMap = new SwitchInfoMap();

		Path usagePath = xUsageFile.toPath();

		List<String> lines = Files.readAllLines(usagePath);

		if (usagePath.toString().endsWith(".properties"))
		{
			lines = preProcess(lines);
		}

		StringBuilder descriptionBuilder = new StringBuilder();

		SwitchInfo info = null;

		for (String line : lines)
		{
			String trimmed = line.trim();

			//System.out.println("line:" + trimmed);

			if (isNewSwitch(trimmed))
			{
				if (info != null)
				{
					if (descriptionBuilder.length() > 0)
					{
						String description = descriptionBuilder.toString();

						//		System.out.println("Setting description on previous info '" + description + "'");

						info.setDescription(description);
						descriptionBuilder.setLength(0);
					}

					//	System.out.println("COMPLETED: " + info.toString());
				}

				//System.out.println("starting new switch");

				int firstSpace = trimmed.indexOf(' ');

				if (firstSpace == -1)
				{
					firstSpace = trimmed.length();
				}

				int firstColon = trimmed.indexOf(':');

				int firstOpenAngle = trimmed.indexOf('<');

				int firstCloseAngle = trimmed.indexOf('>');

				String name = null;

				String type = null;

				if (firstOpenAngle != -1)
				{
					if (firstColon != -1)
					{
						// part in angle brackets forms start of description
						// -Xloggc:<file>    log GC status to a file with time stamps
						name = trimmed.substring(2, firstColon);
					}
					else
					{
						// part in angle brackets forms start of description
						// -Xms<size>        set initial Java heap size
						name = trimmed.substring(2, firstOpenAngle);
					}

					type = trimmed.substring(firstOpenAngle, firstCloseAngle + 1);

					descriptionBuilder.append(trimmed.substring(firstCloseAngle + 1).trim());
				}
				else if (firstColon != -1)
				{
					// option forms part of name
					// -Xshare:off	      do not attempt to use shared class data
					name = trimmed.substring(2, firstSpace);
					descriptionBuilder.append(trimmed.substring(firstSpace).trim());
				}
				else
				{
					// -Xmixed           mixed mode execution (default)
					name = trimmed.substring(2, firstSpace);
					descriptionBuilder.append(trimmed.substring(firstSpace).trim());
				}

				info = new SwitchInfo(PREFIX_X, name.trim());

				info.setType(type);

				info.setDefinedIn(xUsageFile.getName());

				switchMap.put(info.getKey(), info);
			}
			else
			{
				//	System.out.println("Adding '" + line + "' to description");

				descriptionBuilder.append("\n").append(line.trim());
			}
		}

		// final entry
		if (info != null && descriptionBuilder.length() > 0)
		{
			String description = descriptionBuilder.toString();

			//System.out.println("Setting description on info '" + description + "'");

			info.setDescription("<pre>" + description + "</pre>");
			descriptionBuilder.setLength(0);
		}

		return switchMap;
	}

	private boolean isNewSwitch(String line)
	{
		return line.startsWith(PREFIX_X);
	}

	private List<String> preProcess(List<String> lines)
	{
		List<String> result = new ArrayList<>();

		boolean started = false;

		for (String line : lines)
		{
			line = line.trim();

			if (line.contains("java.launcher.X.usage="))
			{
				started = true;
				continue;
			}
			else if (line.contains("--add-reads"))
			{
				break;
			}

			if (started)
			{
				if (!line.startsWith("\\"))
				{
					break;
				}
				else
				{
					line = line.substring(1);
					line = line.replace("{0}", File.pathSeparator).trim();
					line = line.replace("\\n\\n\\", "");
					line = line.replace("\\n\\", "");

					result.add(line);

					//System.out.println("Keep: '" + line + "'");
				}
			}
		}

		return result;
	}
}