/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.commandline;

public class AnalysedSwitchResult
{
	private KeyValue keyValue;

	private String reportHTML;

	private SwitchStatus status;

	private String analysis;

	public String getReportHTML()
	{
		return reportHTML;
	}

	public String getKeyValueHTML(String targetUrl, String id)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<div class=\"summary_switch ")
			   .append(status.getCssClass())
			   .append("\"><a href=\"")
			   .append(targetUrl == null ? "" : targetUrl);

		if (!id.isEmpty())
		{
			builder.append('#').append(id);
		}

		builder.append("\" title=\"").append(analysis).append("\"").append(">").append(keyValue.toStringForHTML()).append("</a></div> ");

		return builder.toString();
	}

	public AnalysedSwitchResult(KeyValue keyValue, String html, SwitchStatus status, String analysis)
	{
		this.keyValue = keyValue;
		this.reportHTML = html;
		this.status = status;
		this.analysis = analysis;
	}

	public SwitchStatus getStatus()
	{
		return status;
	}

	public String getAnalysis()
	{
		return analysis;
	}
}