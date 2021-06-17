/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.vmoe.SwitchInfo.PREFIX_X;
import static com.chrisnewland.vmoe.SwitchInfo.PREFIX_XX;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;

public class OpenJ9SwitchParser extends AbstractSwitchParser
{
	@Override public SwitchInfoMap process(File vmPath) throws IOException
	{
		switchMap = new SwitchInfoMap();

		parseJVMInitHeader(new File(vmPath, "runtime/oti/jvminit.h"));

		parseNLSFile(new File(vmPath, "runtime/nls/exel/exelib.nls"));

		return switchMap;
	}

	private void parseJVMInitHeader(File file) throws IOException
	{
		List<String> lines = Files.readAllLines(file.toPath());

		for (String line : lines)
		{
			String trimmed = line.trim();

			String prefix;

			if (isJVMInitSwitch(trimmed))
			{
				trimmed = trimmed.replace("<", "&lt;").replace(">", "&gt;");

				String name = cleanName(ParseUtil.getBetween(trimmed, "\"", "\""));

				if (name.startsWith(PREFIX_XX))
				{
					prefix = PREFIX_XX;

					name = name.substring(prefix.length());

					if (name.charAt(0) == '+')
					{
						name = name.substring(1);

					}
					else if (name.charAt(0) == '-')
					{
						continue;
					}
				}
				else
				{
					prefix = PREFIX_X;
					name = name.substring(prefix.length());

				}

				SwitchInfo info = new SwitchInfo(prefix, name.trim());

				//System.out.println("init " + trimmed + "\n" + info);

				switchMap.put(info.getKey(), info);
			}
		}
	}

	private void parseNLSFile(File file) throws IOException
	{
		List<String> lines = Files.readAllLines(file.toPath());

		for (String line : lines)
		{
			String trimmed = line.trim();

			if (isNLSSwitch(trimmed))
			{
				trimmed = trimmed.replace("<", "&lt;").replace(">", "&gt;");

				// J9NLS_EXELIB_INTERNAL_HELP_1_7=\ -Xnojit disable the JIT

				String[] parts = trimmed.split("\\s+");

				StringBuilder switchBuilder = new StringBuilder();
				StringBuilder descriptionBuilder = new StringBuilder();

				boolean seenFirstSwitch = false;
				boolean inDescription = false;

				String prefix = null;

				// remove +/- and change verb to "control"

				for (String part : parts)
				{
					if (part.startsWith(PREFIX_XX) && !inDescription)
					{
						prefix = PREFIX_XX;
						switchBuilder.append(part.substring(PREFIX_XX.length())).append(" ");
						seenFirstSwitch = true;
					}
					else if (part.startsWith(PREFIX_X) && !inDescription)
					{
						prefix = PREFIX_X;
						switchBuilder.append(part.substring(PREFIX_X.length())).append(" ");
						seenFirstSwitch = true;
					}
					else if (seenFirstSwitch)
					{
						descriptionBuilder.append(part).append(" ");
						inDescription = true;
					}
				}

				String name = cleanName(switchBuilder.toString().trim());

				if (PREFIX_XX.equals(prefix))
				{
					if (name.charAt(0) == '+')
					{
						name = name.substring(1);

					}
					else if (name.charAt(0) == '-')
					{
						continue;
					}
				}

				SwitchInfo info = new SwitchInfo(prefix, name);
				info.setDescription(descriptionBuilder.toString().trim());

				switchMap.put(info.getKey(), info);

				//System.out.println("nls " + trimmed + "\n" + info);

				cleanDupsEnding(name, "&lt;x&gt;", switchMap);

				cleanDupsEnding(name, ":&lt;path&gt;", switchMap);
			}
		}
	}

	private void cleanDupsEnding(String name, String ending, Map<String, SwitchInfo> switchMap)
	{
		if (name.endsWith(ending))
		{
			removeSwitch(name.substring(0, name.length() - ending.length()), switchMap);
		}
	}

	private String cleanName(String name)
	{
		if (name.endsWith(":") || name.endsWith("="))
		{
			name = name.substring(0, name.length() - 1);
		}

		return name.trim();
	}

	private boolean isNLSSwitch(String line)
	{
		return line.contains("J9NLS_EXELIB_INTERNAL_HELP_") && line.contains("-X");
	}

	private boolean isJVMInitSwitch(String line)
	{
		return line.contains("#define VMOPT_");
	}
}
