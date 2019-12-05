/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.chrisnewland.vmoe.SwitchInfo.PREFIX_XX;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedInfo;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;

public class HotSpotSwitchParser extends AbstractSwitchParser
{
	@Override
	public SwitchInfoMap process(File vmPath) throws IOException
	{
		switchMap = new SwitchInfoMap();

		for (File hotspotFile : findSwitchFilesHotSpot(vmPath))
		{
			parseFile(hotspotFile, vmPath);
		}

		return switchMap;
	}

	private void parseFile(File hotspotFile, File vmPath) throws IOException
	{
		List<String> lines = Files.readAllLines(hotspotFile.toPath());

		StringBuilder lineBuilder = new StringBuilder();

		boolean inLine = false;

		String expectedLineEnding = null;

		String availability = null;

		int descriptionField = -1;
		int defaultValueField = -1;

		SwitchInfo info = null;

		for (String line : lines)
		{
			String trimmed = line.replace("\\\"", "'").replace("\\", "").replace("\" )", "\")").replace("JFR_ONLY(", "").trim();

			// System.out.println(trimmed);

			if (!inLine)
			{
				int bracketPos = trimmed.indexOf('(');

				if (bracketPos == -1)
				{
					continue;
				}

				availability = trimmed.substring(0, bracketPos);

				if ("define_pd_global".equals(availability))
				{
					inLine = true;
					defaultValueField = 2;
					expectedLineEnding = ");";
				}
				else if ("product_pd".equals(availability) || "develop_pd".equals(availability) || "diagnostic_pd".equals(availability))
				{
					inLine = true;
					expectedLineEnding = "\")";
					descriptionField = 2;
				}
				else if ("product".equals(availability) || "product_rw".equals(availability) || "develop".equals(availability) || "lp64_product".equals(availability)
						|| "notproduct".equals(availability) || "diagnostic".equals(availability)
						|| "experimental".equals(availability) || "manageable".equals(availability))
				{
					inLine = true;
					expectedLineEnding = "\")";
					defaultValueField = 2;
					descriptionField = 3;
				}
				else if ("range".equals(availability))
				{
					if (info != null)
					{
						info.setRange(trimmed);
					}
				}
				else
				{
					if (!availability.contains("//") && !availability.contains("*") && !availability.contains("#") && !availability.equals("constraint"))
					{
					//	System.out.println("Unhandled availability mode:" + availability);
					}
					continue;
				}
			}

			if (inLine)
			{
				if (looksLikeListItem(trimmed))
				{
					lineBuilder.append("\"<br>");
					lineBuilder.append(trimmed.substring(1));
				}
				else
				{
					lineBuilder.append(trimmed);
				}

				if (trimmed.contains(expectedLineEnding))
				{
					String result = lineBuilder.toString().replace("\"\"", "").replace("\n", "").replaceAll("\\s+", " ");

					int lineEndingPos = result.indexOf(expectedLineEnding);

					int commentPos = result.indexOf("//", lineEndingPos);

					String comment = null;

					if (commentPos != -1)
					{
						comment = result.substring(commentPos);
					}

					result = result.substring(0, lineEndingPos);

					lineBuilder.delete(0, lineBuilder.length());
					inLine = false;

					result = result.substring(result.indexOf('(') + 1);

					List<String> parts = explodeLine(result);

					final int partCount = parts.size();

					String type = partCount > 0 ? parts.get(0) : null;
					String name = partCount > 1 ? parts.get(1) : null;

					info = switchMap.get(name);

					if (info == null)
					{
						info = new SwitchInfo(PREFIX_XX, name);
						info.setType(type);
						info.setAvailability(availability);
						info.setComment(comment);
						info
							.setDefinedIn(
									hotspotFile.getCanonicalPath().substring(vmPath.getCanonicalFile().toString().length() + 1));
					}

					setFieldsFromPath(info, hotspotFile);

					if (defaultValueField != -1)
					{
						String defaultValue = partCount > defaultValueField ? parts.get(defaultValueField) : null;

						if (defaultValue != null)
						{
							info.setDefaultValue(defaultValue);
						}
					}

					if (descriptionField != -1)
					{
						String description = partCount > descriptionField ? parts.get(descriptionField) : null;

						if (description != null)
						{
							if (description.startsWith("<br>"))
							{
								description = description.substring(4);
							}

							info.setDescription(description);
						}
					}

					DeprecatedInfo deprecatedInfo = DeprecatedParser.getDeprecatedInfo(name);
					
					if (deprecatedInfo != null)
					{
						info.setDeprecation(deprecatedInfo.toHTMLStringVertical());
					}
					
					switchMap.put(info.getKey(), info);

					descriptionField = -1;
					defaultValueField = -1;

					// System.out.println(parts);
				}
			}
		} // for
	}

	private boolean looksLikeListItem(String line)
	{
		return line.length() > 1 && line.charAt(0) == '"' && Character.isDigit(line.charAt(1));
	}

	private List<File> findSwitchFilesHotSpot(File dir)
	{
		List<File> result = new ArrayList<>();

		File[] children = dir.listFiles();

		for (File file : children)
		{
			if (file.isDirectory())
			{
				result.addAll(findSwitchFilesHotSpot(file));
			}
			else
			{
				String filename = file.getName();

				if (filename.contains("globals") && filename.endsWith(".hpp"))
				{
					result.add(file);
				}
			}
		}

		return result;
	}
}
