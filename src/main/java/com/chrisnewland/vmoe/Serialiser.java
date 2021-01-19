/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import com.chrisnewland.vmoe.parser.delta.IDeltaTable;
import com.chrisnewland.vmoe.parser.intrinsic.Intrinsic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Serialiser
{
	private Map<String, Long> filenameSHA1Map = new TreeMap<>();

	public void serialiseSwitchInfo(Path pathToSerialisationFile, Collection<SwitchInfo> switchInfoSet) throws Exception
	{
		StringBuilder builder = new StringBuilder();

		builder.append("{ \"switches\" : [ ");

		long hashCode = 0;

		for (SwitchInfo switchInfo : switchInfoSet)
		{
			builder.append(switchInfo.serialise()).append(",\n");

			hashCode += switchInfo.hashCode();
		}

		builder.deleteCharAt(builder.length() - 2);

		builder.append("] }");

		writeFile(pathToSerialisationFile, builder.toString(), hashCode);
	}

	public void serialiseIntrinsics(Path pathToSerialisationFile, Collection<Intrinsic> instrinsics) throws Exception
	{
		StringBuilder builder = new StringBuilder();

		builder.append("{ \"intrinsics\" : [ ");

		long hashCode = 0;

		for (Intrinsic instrinsic : instrinsics)
		{
			builder.append(instrinsic.serialise()).append(",\n");

			hashCode += instrinsic.hashCode();
		}

		builder.deleteCharAt(builder.length() - 2);

		builder.append("] }");

		writeFile(pathToSerialisationFile, builder.toString(), hashCode);
	}

	public void serialiseDiffs(Path pathToSerialisationFile, IDeltaTable deltaTable) throws Exception
	{
		writeFile(pathToSerialisationFile, deltaTable.toJSON(), deltaTable.hashCode());
	}

	private void writeFile(Path outputPath, String content, long hashCode) throws Exception
	{
		byte[] bytesUTF8 = content.getBytes(StandardCharsets.UTF_8);

		Files.write(outputPath, bytesUTF8);

		StringBuilder relativePathBuilder = new StringBuilder();

		int parts = outputPath.getNameCount();

		relativePathBuilder.append(outputPath.getName(parts - 3))
						   .append('/')
						   .append(outputPath.getName(parts - 2))
						   .append('/')
						   .append(outputPath.getName(parts - 1));

		filenameSHA1Map.put(relativePathBuilder.toString(), hashCode);

		System.out.println("Serialised to " + outputPath.toString());
	}

	public void saveHashes(Path serialisationDir) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, Long> entry : filenameSHA1Map.entrySet())
		{
			builder.append(entry.getKey()).append('=').append(entry.getValue()).append("\n");
		}

		Files.write(serialisationDir.resolve(Paths.get("hashes").toString()), builder.toString().getBytes(StandardCharsets.UTF_8));
	}
}