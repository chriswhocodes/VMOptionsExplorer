/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chrisnewland.vmoe.parser.deprecated.DeprecatedInfo;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;

public class DeltaTable
{
	private List<SwitchInfo> added = new ArrayList<>();
	private List<SwitchInfo> removed = new ArrayList<>();

	private VMData earlierVM;
	private VMData laterVM;

	public DeltaTable(VMData earlierVM, VMData laterVM)
	{
		this.earlierVM = earlierVM;
		this.laterVM = laterVM;
	}

	public void recordAddition(SwitchInfo switchInfo)
	{
		added.add(switchInfo);
	}

	public void recordRemoval(SwitchInfo switchInfo)
	{
		removed.add(switchInfo);
	}

	public int getAdditionCount()
	{
		return added.size();
	}

	public int getRemovalCount()
	{
		return removed.size();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		Collections.sort(added);

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
		Collections.sort(removed);

		builder.append("<table class=\"deltatable removed\">");

		String glossaryLink = "<a class=\"glossaryLink\" href=\"#glossary\">";

		builder.append("<tr>");
		builder.append("<th>Name</th>");
		builder.append("<th>Availability</th>");
		builder.append("<th>").append(glossaryLink).append("Deprecated").append("</a>").append("</th>");
		builder.append("<th>").append(glossaryLink).append("Obsoleted").append("</a>").append("</th>");
		builder.append("<th>").append(glossaryLink).append("Expired").append("</a>").append("</th>");
		builder.append("</tr>");

		for (SwitchInfo removedSwitch : removed)
		{
			builder.append("<tr>");
			builder.append("<td>");

			String name = removedSwitch.getName();

			builder.append("<a href=\"").append(earlierVM.getHTMLFilename()).append("?s=").append(name).append("\">").append(name)
					.append("</a>");

			builder.append("</td>");

			builder.append("<td>");
			builder.append(removedSwitch.getAvailability());
			builder.append("</td>");

			DeprecatedInfo deprecatedInfo = DeprecatedParser.getDeprecatedInfo(name);

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : deprecatedInfo.getDeprecatedInJDK());
			builder.append("</td>");

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : deprecatedInfo.getObsoletedInJDK());
			builder.append("</td>");

			builder.append("<td>");
			builder.append(deprecatedInfo == null ? "" : deprecatedInfo.getExpiredInJDK());
			builder.append("</td>");

			builder.append("</tr>\n");
		}

		builder.append("</table>");
	}

	private void appendTableAdded(StringBuilder builder)
	{
		builder.append("<table class=\"deltatable added\">");
		builder.append("<tr><th>Name</th><th>Availability</th></tr>");

		for (SwitchInfo addedSwitch : added)
		{
			builder.append("<tr>");
			builder.append("<td>");

			String name = addedSwitch.getName();

			builder.append("<a href=\"").append(laterVM.getHTMLFilename()).append("?s=").append(name).append("\">").append(name)
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
