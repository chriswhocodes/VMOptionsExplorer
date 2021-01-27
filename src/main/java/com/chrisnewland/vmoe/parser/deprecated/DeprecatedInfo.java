/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.deprecated;

import com.chrisnewland.vmoe.parser.ParseUtil;

public class DeprecatedInfo
{
	private String optionName;
	private String deprecatedInJDK;
	private String obsoletedInJDK;
	private String expiredInJDK;

	public String getOptionName()
	{
		return optionName;
	}

	public String getDeprecatedInJDK()
	{
		return deprecatedInJDK;
	}

	public String getObsoletedInJDK()
	{
		return obsoletedInJDK;
	}

	public String getExpiredInJDK()
	{
		return expiredInJDK;
	}

	private DeprecatedInfo()
	{
	}

	// { "MaxGCMinorPauseMillis", JDK_Version::jdk(8), JDK_Version::undefined(),
	// JDK_Version::undefined() },

	public static DeprecatedInfo parse(String line)
	{
		DeprecatedInfo deprecatedInfo = new DeprecatedInfo();

		String betweenBraces = ParseUtil.getBetween(line, "{", "}");

		String[] parts = betweenBraces.split(",");

		if (parts.length != 4)
		{
			throw new RuntimeException("Couldn't parse deprecation line: '" + betweenBraces + "'");
		}

		deprecatedInfo.optionName = parts[0].replace("\"", "").trim();

		deprecatedInfo.deprecatedInJDK = ParseUtil.getBetween(parts[1], "(", ")");

		deprecatedInfo.obsoletedInJDK = ParseUtil.getBetween(parts[2], "(", ")");

		deprecatedInfo.expiredInJDK = ParseUtil.getBetween(parts[3], "(", ")");

		return deprecatedInfo;
	}

	@Override public String toString()
	{
		return "DeprecatedInfo [optionName='" + optionName + "', deprecatedInJDK=" + deprecatedInJDK + ", obsoletedInJDK="
				+ obsoletedInJDK + ", expiredInJDK=" + expiredInJDK + "]";
	}
}
