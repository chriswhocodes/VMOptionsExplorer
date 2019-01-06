/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.deprecated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeprecatedParser
{
	private static Map<String, DeprecatedInfo> deprecatedMap = new HashMap<>();

	private static final String ARGUMENTS_FILENAME = "src/hotspot/share/runtime/arguments.cpp";

	private static final String STOP_MARKER = "TEST_VERIFY_SPECIAL_JVM_FLAGS";

	private static boolean isDeprecationLine(String line)
	{
		return line.contains("{") && line.contains("}") && line.contains("JDK_Version::");
	}

	public static void parseFile(Path openJDKRoot) throws IOException
	{
		System.out.println("Parsing deprecation info for " + openJDKRoot);

		List<String> lines = Files.readAllLines(Paths.get(openJDKRoot.toString(), ARGUMENTS_FILENAME));

		for (String line : lines)
		{
			if (line.contains(STOP_MARKER))
			{
				break;
			}

			if (isDeprecationLine(line))
			{
				//System.out.println(line);

				DeprecatedInfo info = DeprecatedInfo.parse(line);

				//System.out.println(info.toString());

				deprecatedMap.put(info.getOptionName(), info);
			}
		}
	}

	public static DeprecatedInfo getDeprecatedInfo(String optionName)
	{
		return deprecatedMap.get(optionName);
	}
}