/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import java.util.Collections;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedInfo;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;

public class HotSpotDeltaTable extends AbstractDeltaTable
{
	public HotSpotDeltaTable(VMData earlierVM, VMData laterVM)
	{
		super(earlierVM, laterVM);
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<hr>");

		builder.append("<h2 class=\"deltaH2\" id=\"")
			   .append(laterVM.getJdkName())
			   .append("\">")
			   .append("Differences between ")
			   .append(earlierVM.getJdkName())
			   .append(" and ")
			   .append(laterVM.getJdkName())
			   .append("</h2>");

		builder.append("<hr>");

		builder.append("<div class=\"dtwrapper\">");

		builder.append("<div class=\"wrap\">");
		builder.append("<h3 class=\"deltaH3\">Removed in ").append(laterVM.getJdkName()).append("</h3>");
		appendTableRemoved(builder);
		builder.append("</div>");

		builder.append("<div class=\"wrap\">");
		builder.append("<h3 class=\"deltaH3\">Added in ").append(laterVM.getJdkName()).append("</h3>");
		appendTableAdded(builder);
		builder.append("</div>");

		builder.append("</div>");

		builder.append("<div class=\"dtclear\"></div>");

		return builder.toString();
	}

	private void appendTableRemoved(StringBuilder builder)
	{
		builder.append("<table class=\"deltatable removed\">");

		String glossaryLink = "<a class=\"glossaryLink\" href=\"#glossary\">";

		builder.append("<tr>");
		builder.append("<th>Name</th>");
		builder.append("<th>Availability</th>");
		builder.append("<th>").append(glossaryLink).append("Deprecated").append("</a>").append("</th>");
		builder.append("<th>").append(glossaryLink).append("Obsoleted").append("</a>").append("</th>");
		builder.append("<th>").append(glossaryLink).append("Expired").append("</a>").append("</th>");
		builder.append("</tr>");

		Collections.sort(removed);

		for (SwitchInfo removedSwitch : removed)
		{
			builder.append("<tr>");
			builder.append("<td>");

			String name = removedSwitch.getName();

			builder.append("<a href=\"")
				   .append(earlierVM.getHTMLFilename())
				   .append("?s=")
				   .append(name)
				   .append("\">")
				   .append(name)
				   .append("</a>");

			builder.append("</td>");

			builder.append("<td>");
			builder.append(removedSwitch.getAvailability());
			builder.append("</td>");

			DeprecatedInfo deprecatedInfo = DeprecatedParser.getDeprecatedInfo(name);

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : getJDKStringOrEmpty(deprecatedInfo.getDeprecatedInJDK()));
			builder.append("</td>");

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : getJDKStringOrEmpty(deprecatedInfo.getObsoletedInJDK()));
			builder.append("</td>");

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : getJDKStringOrEmpty(deprecatedInfo.getExpiredInJDK()));
			builder.append("</td>");

			builder.append("</tr>\n");
		}

		builder.append("</table>");
	}

	private String getJDKStringOrEmpty(String value)
	{
		if (value != null && !value.isEmpty())
		{
			return "JDK" + value;
		}
		else
		{
			return "";
		}
	}

	private void appendTableAdded(StringBuilder builder)
	{
		builder.append("<table class=\"deltatable added\">");
		builder.append("<tr><th>Name</th><th>Availability</th></tr>");

		Collections.sort(added);

		for (SwitchInfo addedSwitch : added)
		{
			builder.append("<tr>");
			builder.append("<td>");

			String name = addedSwitch.getName();

			builder.append("<a href=\"")
				   .append(laterVM.getHTMLFilename())
				   .append("?s=")
				   .append(name)
				   .append("\">")
				   .append(name)
				   .append("</a>");

			builder.append("</td>");

			builder.append("<td>");
			builder.append(addedSwitch.getAvailability());
			builder.append("</td>");

			builder.append("</tr>\n");

		}
		builder.append("</table>");
	}
}
