/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedInfo;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;

import java.util.Collections;
import java.util.List;

public class GraalDeltaTable extends AbstractDeltaTable
{
	public GraalDeltaTable(VMData earlierVM, VMData laterVM)
	{
		super(earlierVM, laterVM);
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<hr>");

		builder.append("<h2 class=\"deltaH2\">")
			   .append("Options present only in ")
			   .append(laterVM.getJdkName())
			   .append("</h2>");

		builder.append("<hr>");

		builder.append("<div class=\"dtwrapper\">");

		if (!removed.isEmpty())
		{
			builder.append("<div class=\"wrap\">");
			builder.append("<h3 class=\"deltaH3\">Present only in ").append(earlierVM.getJdkName()).append("</h3>");
			appendTableAdded(builder, removed);
			builder.append("</div>");
		}

		builder.append("<div class=\"wrap\">");
		builder.append("<h3 class=\"deltaH3\">Present only in ").append(laterVM.getJdkName()).append("</h3>");
		appendTableAdded(builder, added);
		builder.append("</div>");

		builder.append("</div>");

		builder.append("<div class=\"dtclear\"></div>");

		return builder.toString();
	}

	private void appendTableAdded(StringBuilder builder, List<SwitchInfo> options)
	{
		builder.append("<table class=\"deltatable added\">");
		builder.append("<tr><th>Name</th></tr>");

		Collections.sort(options);

		for (SwitchInfo addedSwitch : options)
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

			builder.append("</tr>\n");

		}
		builder.append("</table>");
	}
}
