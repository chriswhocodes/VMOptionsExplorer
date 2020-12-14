/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.intrinsic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.vmoe.Serialiser;
import com.chrisnewland.vmoe.parser.ParseUtil;

public class IntrinsicParser
{
	private Map<String, String> mapTemplate;
	private Map<String, String> mapClass;
	private Map<String, String> mapName;
	private Map<String, String> mapSignature;
	private Map<String, String> mapAlias;

	private Map<String, String> seenInJDK = new HashMap<>();

	private static final String TEMPLATE = "template";
	private static final String DO_INTRINSIC = "do_intrinsic";
	private static final String DO_NAME = "do_name";
	private static final String DO_CLASS = "do_class";
	private static final String DO_SIGNATURE = "do_signature";
	private static final String DO_ALIAS = "do_alias";

	private List<Intrinsic> instrinsics;

	private String graalVersion;

	private Path serialisationPath;

	public IntrinsicParser(Path serialisationPath, String graalVersion)
	{
		this.serialisationPath = serialisationPath;

		this.graalVersion = graalVersion;
	}

	private void mapTemplate(String key, String value)
	{
		//System.out.println("Template: '" + key + "'=>'" + value + "'");

		mapTemplate.put(key, value);
	}

	private void mapClass(String key, String value)
	{
		// System.out.println("Class: '" + key + "'=>'" + value + "'");

		mapClass.put(key, value);
	}

	private void mapName(String key, String value)
	{
		// System.out.println("Name: '" + key + "'=>'" + value + "'");

		mapName.put(key, value);
	}

	private void mapSignature(String key, String value)
	{
		System.out.println("Signature: '" + key + "'=>'" + value + "'");

		mapSignature.put(key, value);
	}

	private void mapAlias(String key, String value)
	{
		//System.out.println("Alias: '" + key + "'=>'" + value + "'");

		mapAlias.put(key, value);
	}

	private void storeIntrinsic(Intrinsic intrinsic)
	{
		// System.out.println("intrinsic: '" + intrinsic.toString());

		instrinsics.add(intrinsic);
	}

	//   do_name(getCharAcquire_name,    "getCharAcquire")         do_name(putCharRelease_name,    "putCharRelease")           \
	private List<String> splitMultipleTagsPerLine(List<String> lines)
	{
		List<String> result = new ArrayList<String>();

		for (String line : lines)
		{
			line = ParseUtil.removeBetween(line, "/*", "*/");

			int lineLength = line.length();

			int lastStart = 0;

			boolean inQuotes = false;

			// System.out.println("line: " + line);

			for (int i = 0; i < lineLength; i++)
			{
				char c = line.charAt(i);

				if (c == '"')
				{
					inQuotes = !inQuotes;
				}
				else if (c == ')')
				{
					if (!inQuotes)
					{
						String part = line.substring(lastStart, i + 1).trim();

						if (validLine(part))
						{
							// System.out.println("partA: '" + part + "'");
							result.add(part);
						}

						lastStart = i + 1;
					}
				}
			}

			String part = line.substring(lastStart, lineLength).trim();

			if (validLine(part))
			{
				// System.out.println("partB: '" + part + "'");
				result.add(part);
			}
		}

		return result;
	}

	public List<Intrinsic> getIntrinsics()
	{
		List<Intrinsic> result = new ArrayList<>();

		for (Intrinsic intrinsic : instrinsics)
		{
			Intrinsic substituted = substituteMappings(intrinsic);

			System.out.println(intrinsic);
			System.out.println(substituted);
			System.out.println();

			result.add(substituted);
		}

		return result;
	}

	private boolean validLine(String line)
	{
		return line != null && line.length() > 0 && line.contains("(") && line.contains(")");
	}

	private void reset()
	{
		instrinsics = new ArrayList<>();

		mapTemplate = new HashMap<String, String>();
		mapClass = new HashMap<String, String>();
		mapName = new HashMap<String, String>();
		mapSignature = new HashMap<String, String>();
		mapAlias = new HashMap<String, String>();
	}

	public void processIntrinsics(String jdkName, Path... filesToProcess) throws IOException
	{
		reset();

		for (Path filePath : filesToProcess)
		{
			processFile(filePath, jdkName);
		}
	}


	// don't split by line
	// read chars

	private void processFile(Path fileToProcess, String jdkName) throws IOException
	{
		List<String> lines = splitMultipleTagsPerLine(Files.readAllLines(fileToProcess));

		for (int i = 0; i <lines.size(); i++)
		{
			String line = lines.get(i);

			System.out.println(line);

			int firstBracket = line.indexOf('(');

			if (firstBracket != -1)
			{
				String lineType = line.substring(0, firstBracket);

				String inBrackets = ParseUtil.getBetween(line, "(", ")");

				inBrackets = inBrackets.replace("<", "&lt;").replaceAll(">", "&gt;");

				switch (lineType)
				{
				case TEMPLATE:
					parseTemplate(inBrackets);
					break;
				case DO_INTRINSIC:
					parseDoIntrinsic(inBrackets);
					break;
				case DO_NAME:
					parseDoName(inBrackets);
					break;
				case DO_CLASS:
					parseDoClass(inBrackets);
					break;
				case DO_SIGNATURE:
					parseDoSignature(inBrackets);
					break;
				case DO_ALIAS:
					parseDoAlias(inBrackets);
					break;
				}
			}
		}

		createHTMLForVM(jdkName);

		if (serialisationPath != null)
		{
			Serialiser.serialiseIntrinsics(serialisationPath.resolve(Paths.get(jdkName + "_instrinsics.json")), getIntrinsics());
		}
	}

	private String[] getParts(String value)
	{
		String[] parts = value.split(",");

		for (int i = 0; i < parts.length; i++)
		{
			parts[i] = parts[i].replace(" ", "").replace("\"", "").trim();
		}

		return parts;
	}

	private void parseTemplate(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 2)
		{
			mapTemplate(parts[0], parts[1]);
		}
		else
		{
			System.err.println("Bad template: " + Arrays.toString(parts));
		}
	}

	private void parseDoIntrinsic(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 5)
		{
			Intrinsic intrinsic = new Intrinsic(parts[0], parts[1], parts[2], parts[3], parts[4]);

			storeIntrinsic(intrinsic);
		}
		else
		{
			System.err.println("Bad intrinsic: " + Arrays.toString(parts));
		}
	}

	private void parseDoName(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 2)
		{
			mapName(parts[0], parts[1]);
		}
		else
		{
			System.err.println("Bad name: " + Arrays.toString(parts));
		}
	}

	private void parseDoClass(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 2)
		{
			mapClass(parts[0], parts[1]);
		}
		else
		{
			System.err.println("Bad class: " + Arrays.toString(parts));
		}
	}

	private void parseDoSignature(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 2)
		{
			mapSignature(parts[0], parts[1]);
		}
		else
		{
			System.err.println("Bad signature: " + Arrays.toString(parts));
		}
	}

	private void parseDoAlias(String value)
	{
		String[] parts = getParts(value);

		if (parts.length == 2)
		{
			mapAlias(parts[0], parts[1]);
		}
		else
		{
			System.err.println("Bad alias: " + Arrays.toString(parts));
		}
	}

	private Intrinsic substituteMappings(Intrinsic original)
	{
		String id = original.getId();
		String klass = original.getKlass();
		String name = original.getName();
		String signature = original.getSignature();
		String flags = original.getFlags();

		String actualKlass = mapClass.get(klass);

		if (actualKlass == null)
		{
			actualKlass = mapTemplate.get(klass);
		}

		if (actualKlass != null)
		{
			actualKlass = actualKlass.replace("/", ".");
		}

		String actualName = mapName.get(name);

		if (actualName == null)
		{
			actualName = mapTemplate.get(name);
		}

		String actualSignature = mapSignature.get(signature);

		if (actualSignature == null)
		{
			actualSignature = mapName.get(signature);

			if (actualSignature == null)
			{
				actualSignature = mapTemplate.get(signature);

				// try with alias

				signature = mapAlias.get(signature);

				if (signature != null)
				{
					actualSignature = mapSignature.get(signature);

					if (actualSignature == null)
					{
						actualSignature = mapName.get(signature);

						if (actualSignature == null)
						{
							actualSignature = mapTemplate.get(signature);
						}
					}
				}
			}
		}

		return new Intrinsic(id, actualKlass, actualName, actualSignature, flags);
	}

	private void createHTMLForVM(String jdkName) throws IOException
	{
		String template = new String(Files.readAllBytes(Paths.get("templates/template_intrinsic.html")), StandardCharsets.UTF_8);
		String headerHTML = new String(Files.readAllBytes(Paths.get("templates/header.html")), StandardCharsets.UTF_8);

		StringBuilder htmlBuilder = new StringBuilder();

		List<Intrinsic> intrinsics = getIntrinsics();

		for (Intrinsic intrinsic : intrinsics)
		{
			String firstSeenInJDK = seenInJDK.get(intrinsic.getId());

			if (firstSeenInJDK == null)
			{
				firstSeenInJDK = jdkName;

				seenInJDK.put(intrinsic.getId(), firstSeenInJDK);
			}

			intrinsic.setSince(firstSeenInJDK);

			htmlBuilder.append(intrinsic.toRow()).append("\n");
		}

		template = template.replace("$HEADER_HTML", headerHTML);

		template = template.replace("$H1_TITLE", "$VMNAME Intrinsics for $JDK");
		template = template.replace("$GRAAL_VERSION", graalVersion);
		template = template.replace("$THEAD", Intrinsic.getHeaderRow());
		template = template.replace("$VMNAME", "HotSpot");
		template = template.replace("$JDK", jdkName);
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$TBODY", htmlBuilder.toString());

		template = template.replace("$TOPHEADER", "<th></th><th></th><th>Class</th><th></th><th></th><th></th>");
		template = template.replace("$ALLCOLUMNS", "[ 0, 1, 2, 3, 4, 5 ]");
		template = template.replace("$SORTCOLUMNS", "[ 2 ]");

		Files.write(Paths.get("html/hotspot_intrinsics_" + jdkName.toLowerCase() + ".html"),
				template.getBytes(StandardCharsets.UTF_8));

		System.out.println(jdkName + " has " + intrinsics.size() + " intrinsics");
	}
}
