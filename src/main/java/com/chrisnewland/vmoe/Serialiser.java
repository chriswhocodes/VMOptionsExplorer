/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Serialiser
{
	public static void serialise(Path pathToSerialisationFile, Collection<SwitchInfo> switchInfoSet) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		for (SwitchInfo switchInfo : switchInfoSet)
		{
			builder.append(switchInfo.serialise()).append("\n");
		}

		System.out.println("Serialised to " + pathToSerialisationFile.toString());

		Files.write(pathToSerialisationFile, builder.toString().getBytes(StandardCharsets.UTF_8));
	}
}
