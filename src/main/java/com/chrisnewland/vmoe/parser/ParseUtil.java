/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

public class ParseUtil
{
	public static String getBetween(String input, String start, String end)
	{
		String result = null;

		int startIndex = input.indexOf(start);

		if (startIndex != -1)
		{
			int endIndex = input.lastIndexOf(end);

			if (endIndex != -1)
			{

				result = input.substring(startIndex + start.length(), endIndex);
			}
		}

		return result;
	}

	public static String removeBetween(String input, String start, String end)
	{
		String result = input;

		int startIndex = input.indexOf(start);

		if (startIndex != -1)
		{
			int endIndex = input.lastIndexOf(end);

			if (endIndex != -1)
			{
				result = input.substring(0, startIndex) + input.substring(endIndex + end.length());
			}
		}

		return result;
	}
}