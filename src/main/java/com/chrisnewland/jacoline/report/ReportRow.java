/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.report;

public class ReportRow
{
	private String[] columns;

	public ReportRow(int columnCount)
	{
		this.columns = new String[columnCount];
	}

	public void setColumn(int column, String value)
	{
		this.columns[column] = value;
	}

	public String[] getColumns()
	{
		return  columns;
	}

	@Override public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (String col : columns)
		{
			builder.append('[').append(col).append(']');
		}

		return builder.toString();
	}
}