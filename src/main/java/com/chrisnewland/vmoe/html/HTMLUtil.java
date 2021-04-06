package com.chrisnewland.vmoe.html;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMType;

public class HTMLUtil
{
	public static String renderDeprecation(SwitchInfo switchInfo)
	{
		StringBuilder builder = new StringBuilder();

		if (switchInfo.getDeprecated() != null)
		{
			builder.append("<span style=\"white-space:nowrap\">Deprecated in JDK")
				   .append(switchInfo.getDeprecated())
				   .append("</span>");
		}

		if (switchInfo.getObsoleted() != null)
		{
			if (builder.length() > 0)
			{
				builder.append("<br>");
			}
			builder.append("<span style=\"white-space:nowrap\">Obsoleted in JDK")
				   .append(switchInfo.getObsoleted())
				   .append("</span>");
		}

		if (switchInfo.getExpired() != null)
		{
			if (builder.length() > 0)
			{
				builder.append("<br>");
			}
			builder.append("<span style=\"white-space:nowrap\">Expired in JDK").append(switchInfo.getExpired()).append("</span>");
		}

		return builder.toString();
	}

	public static String getHeaderRow(VMType vmType)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");
		builder.append("<th>").append("Name").append("</th>");

		boolean isHotSpotBased = (vmType == VMType.HOTSPOT || vmType == VMType.SAPMACHINE || vmType == VMType.CORRETTO
				|| vmType == VMType.MICROSOFT);

		if (isHotSpotBased)
		{
			builder.append("<th>").append("Since").append("</th>");
			builder.append("<th>").append("Deprecated").append("</th>");
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append("<th>").append("Type").append("</th>");
		}

		if (isHotSpotBased)
		{
			builder.append("<th>").append("OS").append("</th>");
			builder.append("<th>").append("CPU").append("</th>");
			builder.append("<th>").append("Component").append("</th>");
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append("<th>").append("Default").append("</th>");
		}

		if (isHotSpotBased || vmType == VMType.GRAAL_NATIVE_8 || vmType == VMType.GRAAL_NATIVE_11)
		{
			builder.append("<th>").append("Availability").append("</th>");
		}

		if (vmType != VMType.ZING && vmType != VMType.ZULU)
		{
			builder.append("<th>").append("Description").append("</th>");
		}

		if (isHotSpotBased)
		{
			builder.append("<th>").append("Defined in").append("</th>");
		}

		builder.append("</tr>");

		return builder.toString();
	}

	public static String renderSwitchInfoRow(VMType vmType, SwitchInfo switchInfo)
	{
		boolean isHotSpotBased = (vmType == VMType.HOTSPOT || vmType == VMType.SAPMACHINE || vmType == VMType.CORRETTO
				|| vmType == VMType.MICROSOFT);

		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");

		builder.append(getRow(switchInfo.getName()));

		if (isHotSpotBased)
		{
			builder.append(getRow(switchInfo.getSince()));
			builder.append(getRow(renderDeprecation(switchInfo)));
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append(getRow(escapeHTMLEntities(switchInfo.getType())));
		}

		if (isHotSpotBased)
		{
			builder.append(getRow(switchInfo.getOs()));
			builder.append(getRow(switchInfo.getCpu()));
			builder.append(getRow(switchInfo.getComponent()));
		}

		if (vmType != VMType.OPENJ9)
		{
			if (switchInfo.getDefaultValue() != null)
			{
				builder.append(getRow(switchInfo.getDefaultValue() + ((switchInfo.getRange() == null) ?
						"" :
						"<br>" + switchInfo.getRange())));
			}
			else
			{
				builder.append(getRow(""));
			}
		}

		if (isHotSpotBased || vmType == VMType.GRAAL_NATIVE_8 || vmType == VMType.GRAAL_NATIVE_11)
		{
			builder.append(getRow(switchInfo.getAvailability()));
		}

		if (vmType != VMType.ZING && vmType != VMType.ZULU)
		{
			String descriptionComment = "";

			if (switchInfo.getDescription() != null)
			{
				descriptionComment += switchInfo.getDescription();

				if (switchInfo.getComment() != null)
				{
					descriptionComment += "<br>" + switchInfo.getComment();
				}
			}
			else if (switchInfo.getComment() != null)
			{
				descriptionComment += switchInfo.getComment();
			}

			builder.append(getRow(escapeHTMLEntities(descriptionComment)));
		}

		if (isHotSpotBased)
		{
			builder.append(getRow(switchInfo.getDefinedIn()));
		}

		builder.append("</tr>");

		return builder.toString();
	}

	public static String escapeHTMLEntities(String raw)
	{
		if (raw == null)
		{
			return "";
		}

		return raw.toString()
				  .replace("<br>", "SAFE_BR")
				  .replace("<pre>", "SAFE_PRE_OPEN")
				  .replace("</pre>", "SAFE_PRE_CLOSE")
				  .replace("&", "&amp;")
				  .replace("<", "&lt;")
				  .replace(">", "&gt;")
				  .replace("\"", "&quot;")
				  .replace("SAFE_BR", "<br>")
				  .replace("SAFE_PRE_OPEN", "<pre>")
				  .replace("SAFE_PRE_CLOSE", "</pre>");
	}

	private static String getRow(String value)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<td>").append(value == null ? "" : value).append("</td>");

		return builder.toString();
	}

	/*
	public static String toHTMLStringHorizontal()
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
	}*/
}
