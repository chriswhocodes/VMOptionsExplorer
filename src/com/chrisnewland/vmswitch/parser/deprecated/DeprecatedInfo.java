package com.chrisnewland.vmswitch.parser.deprecated;

import com.chrisnewland.vmswitch.parser.ParseUtil;

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

	public void setOptionName(String optionName)
	{
		this.optionName = optionName;
	}

	public String getDeprecatedInJDK()
	{
		return deprecatedInJDK;
	}

	public void setDeprecatedInJDK(String deprecatedInJDK)
	{
		this.deprecatedInJDK = deprecatedInJDK;
	}

	public String getObsoletedInJDK()
	{
		return obsoletedInJDK;
	}

	public void setObsoletedInJDK(String obsoletedInJDK)
	{
		this.obsoletedInJDK = obsoletedInJDK;
	}

	public String getExpiredInJDK()
	{
		return expiredInJDK;
	}

	public void setExpiredInJDK(String expiredInJDK)
	{
		this.expiredInJDK = expiredInJDK;
	}

	private DeprecatedInfo()
	{
	}

	// { "MaxGCMinorPauseMillis", JDK_Version::jdk(8), JDK_Version::undefined(),
	// JDK_Version::undefined() },

	public static DeprecatedInfo parse(String line)
	{
		DeprecatedInfo info = new DeprecatedInfo();

		String betweenBraces = ParseUtil.getBetween(line, "{", "}");

		String[] parts = betweenBraces.split(",");

		if (parts.length != 4)
		{
			throw new RuntimeException("Couldn't parse deprecation line: '" + betweenBraces + "'");
		}

		info.optionName = parts[0].replace("\"", "").trim();

		info.deprecatedInJDK = ParseUtil.getBetween(parts[1], "(", ")");

		info.obsoletedInJDK = ParseUtil.getBetween(parts[2], "(", ")");

		info.expiredInJDK = ParseUtil.getBetween(parts[3], "(", ")");

		return info;
	}

	@Override
	public String toString()
	{
		return "DeprecatedInfo [optionName='" + optionName + "', deprecatedInJDK=" + deprecatedInJDK + ", obsoletedInJDK="
				+ obsoletedInJDK + ", expiredInJDK=" + expiredInJDK + "]";
	}

	public String toHTMLStringVertical()
	{
		StringBuilder builder = new StringBuilder();

		if (deprecatedInJDK.length() > 0)
		{
			builder.append("<span style=\"white-space:nowrap\">Deprecated in JDK").append(deprecatedInJDK).append("</span>");
		}
		if (obsoletedInJDK.length() > 0)
		{
			if (builder.length() > 0)
			{
				builder.append("<br>");
			}
			builder.append("<span style=\"white-space:nowrap\">Obsoleted in JDK").append(obsoletedInJDK).append("</span>");
		}
		if (expiredInJDK.length() > 0)
		{
			if (builder.length() > 0)
			{
				builder.append("<br>");
			}
			builder.append("<span style=\"white-space:nowrap\">Expired in JDK").append(expiredInJDK).append("</span>");
		}

		return builder.toString();
	}

	public String toHTMLStringHorizontal()
	{
		StringBuilder builder = new StringBuilder();

		if (deprecatedInJDK.length() > 0)
		{
			builder.append("Deprecated in JDK").append(deprecatedInJDK);
		}
		if (obsoletedInJDK.length() > 0)
		{
			if (builder.length() > 0)
			{
				builder.append(", ");
			}
			builder.append("Obsoleted in JDK").append(obsoletedInJDK);
		}
		if (expiredInJDK.length() > 0)
		{
			if (builder.length() > 0)
			{
				builder.append(", ");
			}
			builder.append("Expired in JDK").append(expiredInJDK);
		}

		return builder.toString();
	}
}
