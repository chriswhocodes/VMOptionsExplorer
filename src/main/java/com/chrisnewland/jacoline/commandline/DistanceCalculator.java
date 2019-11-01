/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.commandline;

import java.util.Set;

public class DistanceCalculator
{
	private Set<String> dictionary;

	public DistanceCalculator(Set<String> dictionary)
	{
		this.dictionary = dictionary;

		System.out.println("Loaded " + dictionary.size() + " words");
	}

	public String findClosestWord(String input)
	{
		System.out.println("findClosestWord to " + input);

		String closest = null;

		if (input != null)
		{
			int minDistance = Integer.MAX_VALUE;

			for (String word : dictionary)
			{
				int distance = calculateDistance(input, word);

				if (distance < minDistance)
				{
					minDistance = distance;
					closest = word;

					if (distance == 0)
					{
						break;
					}
				}
			}

			// wrong by more than 50% then abandon
			if (minDistance > input.length() / 2)
			{
				closest = null;
			}
		}

		System.out.println("Closest word to " + input + " is " + closest);

		return closest;
	}

	public static int calculateDistance(String first, String second)
	{
		first = first.toLowerCase();
		second = second.toLowerCase();

		int lengthFirst = first.length();
		int lengthSecond = second.length();

		int[][] dp = new int[lengthFirst + 1][lengthSecond + 1];

		for (int i = 0; i <= lengthFirst; i++)
		{
			dp[i][0] = i;
		}

		for (int j = 0; j <= lengthSecond; j++)
		{
			dp[0][j] = j;
		}

		for (int i = 0; i < lengthFirst; i++)
		{
			char c1 = first.charAt(i);

			for (int j = 0; j < lengthSecond; j++)
			{
				char c2 = second.charAt(j);

				if (c1 == c2)
				{
					dp[i + 1][j + 1] = dp[i][j];
				}
				else
				{
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;

					int min = Math.min(replace, Math.min(insert, delete));

					dp[i + 1][j + 1] = min;
				}
			}
		}

		return dp[lengthFirst][lengthSecond];
	}
}