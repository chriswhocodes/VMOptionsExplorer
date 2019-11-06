/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service;

import com.chrisnewland.jacoline.commandline.CommandLineSwitchParser;
import com.chrisnewland.jacoline.dto.RequestDTO;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServiceUtil
{
	private static Path resourcesPath;

	public static void initialise(Path resourcesPath)
	{
		ServiceUtil.resourcesPath = resourcesPath;

		System.out.println("Initialised resources: " + ServiceUtil.resourcesPath);
	}

	public static final String OPTION_ANY = "- Any -";

	private static String loadTemplate(String pageName, String title, String h1) throws IOException
	{
		String template = new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), "template.html")),
				StandardCharsets.UTF_8);

		String page = new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), pageName)), StandardCharsets.UTF_8);

		return template.replace("%BODY%", page).replace("%TITLE%", title).replace("%H1%", h1);
	}

	public static String loadCompareTemplate() throws IOException
	{
		return new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), "compare.html")), StandardCharsets.UTF_8);
	}

	public static String loadForm() throws IOException
	{
		return loadTemplate("form.html", "Inspect your Java command line", "Java Command Line Inspector");
	}

	public static String loadError() throws IOException
	{
		return loadTemplate("error.html", "An error occurred", "Bad Input");
	}

	public static String loadStats() throws IOException
	{
		return loadTemplate("stats.html", "Java Command Line Statistics", "Java Command Line Statistics");
	}

	public static String loadRetrieve(long id) throws IOException
	{
		return loadTemplate("retrieve.html", "View Historical Command Line", "Viewing Historical Command Line #" + id);
	}

	public static String showAbout() throws IOException
	{
		return loadTemplate("about.html", "About Java Command Line Inspector", "About");
	}

	public static String showPrivacy() throws IOException
	{
		return loadTemplate("privacy.html", "Privacy Policy", "Privacy Policy");
	}

	public static String buildComboJVM(String selectedJVM)
	{
		return buildCombo("jvm", CommandLineSwitchParser.getJDKList(), selectedJVM);
	}

	public static String buildComboOperatingSystem(String jdkName, String selectedOS)
	{
		return buildCombo("os", CommandLineSwitchParser.getOperatingSystemList(), selectedOS);
	}

	public static String buildComboArchitecture(String jdkName, String selectedArch)
	{
		return buildCombo("arch", CommandLineSwitchParser.getArchitectureList(), selectedArch);
	}

	public static String buildCombo(String name, List<String> options, String defaultValue)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<select id=\"").append(name).append("\" name=\"").append(name).append("\">");

		for (String option : options)
		{
			if (defaultValue != null && defaultValue.equalsIgnoreCase(option))
			{
				builder.append("<option selected=\"selected\">");
			}
			else
			{
				builder.append("<option>");
			}

			builder.append(option).append("</option>");
		}

		builder.append("</select>");

		return builder.toString();
	}

	public static String buildCheckBox(boolean debugSelected)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<input type=\"checkbox\" name=\"debug\" value=\"debug\"");

		if (debugSelected)
		{
			builder.append(" checked");
		}

		builder.append(">");

		return builder.toString();
	}

	public static String buildForm(String selectedJVM, String selectedOS, String selectedArch, boolean debugSelected)
	{
		try
		{
			String form = loadForm();

			return form.replace("%COMBO_OS%", buildComboOperatingSystem(selectedJVM, selectedOS))
					   .replace("%COMBO_ARCH%", buildComboArchitecture(selectedJVM, selectedArch))
					   .replace("%COMBO_JDK%", buildComboJVM(selectedJVM))
					   .replace("%CHECK_DEBUG%", buildCheckBox(debugSelected));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	public static String buildRetrievedRequest(long requestId)
	{
		try
		{
			String template = loadRetrieve(requestId);

			RequestDTO requestDTO = RequestDTO.loadById(requestId);

			if (requestDTO != null)
			{
				String result = CommandLineSwitchParser.buildReport(requestDTO.getRequest(), requestDTO.getJvm(),
						requestDTO.getOs(), requestDTO.getArch(), requestDTO.isDebugVm(), false);

				return template.replace("%REQUEST_DATE%", requestDTO.getRecordedAt().toString())
							   .replace("%REQUEST_OS%", Encode.forHtml(requestDTO.getOs()))
							   .replace("%REQUEST_ARCH%", Encode.forHtml(requestDTO.getArch()))
							   .replace("%REQUEST_JDK%", Encode.forHtml(requestDTO.getJvm()))
							   .replace("%REQUEST_COMMAND%", Encode.forHtml(requestDTO.getRequest()))
							   .replace("%REQUEST_DEBUG%", requestDTO.isDebugVm() ? "Y" : "N")
							   .replace("%RESULT%", result);
			}

			return "requestId not found";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}
}