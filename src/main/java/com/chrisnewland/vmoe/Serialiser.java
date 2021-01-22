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
import java.security.MessageDigest;
import java.util.*;

public class Serialiser
{
	private Map<String, String> filenameSHA1Map = new TreeMap<>();

	public void serialiseSwitchInfo(Path pathToSerialisationFile, Collection<SwitchInfo> switchInfoSet) throws Exception
	{
		StringBuilder builder = new StringBuilder();

		builder.append("{ \"switches\" : [ ");

		for (SwitchInfo switchInfo : switchInfoSet)
		{
			builder.append(switchInfo.serialise()).append(",\n");
		}

		builder.deleteCharAt(builder.length() - 2);

		builder.append("] }");

		writeFile(pathToSerialisationFile, builder.toString());
	}

	public void serialiseIntrinsics(Path pathToSerialisationFile, Collection<Intrinsic> intrinsics) throws Exception
	{
		StringBuilder builder = new StringBuilder();

		builder.append("{ \"intrinsics\" : [ ");

		for (Intrinsic intrinsic : intrinsics)
		{
			builder.append(intrinsic.serialise()).append(",\n");
		}

		builder.deleteCharAt(builder.length() - 2);

		builder.append("] }");

		writeFile(pathToSerialisationFile, builder.toString());
	}

	public void serialiseDiffs(Path pathToSerialisationFile, IDeltaTable deltaTable) throws Exception
	{
		writeFile(pathToSerialisationFile, deltaTable.toJSON());
	}

	private void writeFile(Path outputPath, String content) throws Exception
	{
		byte[] bytesUTF8 = content.getBytes(StandardCharsets.UTF_8);

		Files.write(outputPath, bytesUTF8);

		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.update(bytesUTF8);
		byte[] digestBytes = digest.digest();
		String hashString = javax.xml.bind.DatatypeConverter.printHexBinary(digestBytes);

		StringBuilder relativePathBuilder = new StringBuilder();

		int parts = outputPath.getNameCount();

		relativePathBuilder.append(outputPath.getName(parts - 3))
						   .append('/')
						   .append(outputPath.getName(parts - 2))
						   .append('/')
						   .append(outputPath.getName(parts - 1));

		filenameSHA1Map.put(relativePathBuilder.toString(), hashString);

		System.out.println("Serialised to " + outputPath.toString());
	}

	public void saveHashes(Path serialisationDir) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, String> entry : filenameSHA1Map.entrySet())
		{
			builder.append(entry.getKey()).append('=').append(entry.getValue()).append("\n");
		}

		Files.write(serialisationDir.resolve(Paths.get("hashes").toString()), builder.toString().getBytes(StandardCharsets.UTF_8));
	}
}