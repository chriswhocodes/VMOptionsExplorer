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

public class OpenJ9SwitchParser extends AbstractSwitchParser
{
	private static final String SWITCH_START = "-X";

	@Override
	public SwitchInfoMap process(File vmPath) throws IOException
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

			if (isJVMInitSwitch(trimmed))
			{
				trimmed = trimmed.replace("<", "&lt;").replace(">", "&gt;");
				
				String name = cleanName(ParseUtil.getBetween(trimmed, "\"", "\""));

				SwitchInfo info = new SwitchInfo(PREFIX_XX, name.trim());

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

				for (String part : parts)
				{
					if (part.startsWith(SWITCH_START) && !inDescription)
					{
						switchBuilder.append(part).append(" ");
						seenFirstSwitch = true;
					}
					else if (seenFirstSwitch)
					{
						descriptionBuilder.append(part).append(" ");
						inDescription = true;
					}
				}

				String name = cleanName(switchBuilder.toString().trim());

				SwitchInfo info = new SwitchInfo(PREFIX_XX, name);
				info.setDescription("<pre>" + descriptionBuilder.toString().trim() + "</pre>");

				switchMap.put(info.getKey(), info);

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

		return name;
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
