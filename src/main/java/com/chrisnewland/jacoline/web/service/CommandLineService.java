/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service;

import com.chrisnewland.jacoline.commandline.CommandLineSwitchParser;
import com.chrisnewland.jacoline.commandline.SwitchStatus;
import com.chrisnewland.jacoline.report.ReportBuilder;
import org.owasp.encoder.Encode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Path("/") public class CommandLineService
{
	private static final String DEFAULT_JDK = "JDK8";

	private static boolean statsCacheIsValid = false;

	private static String generatedStatsHTML = null;

	public static void initialise(java.nio.file.Path serialisedPath) throws IOException
	{
		CommandLineSwitchParser.initialise(serialisedPath);
	}

	@GET @Path("inspect") @Produces(MediaType.TEXT_HTML) public String handleForm()
	{
		return ServiceUtil.buildForm(DEFAULT_JDK, "linux", "x86", false)
						  .replace("%COMMAND%", ""/*Encode.forHtml(builder.toString())*/)
						  .replace("%RESULT%", "")
						  .replace("%STORED%", "");
	}

	@POST @Path("inspect") @Consumes(MediaType.APPLICATION_FORM_URLENCODED) @Produces(MediaType.TEXT_HTML) public String handleCommand(
			@FormParam("commandline") String command, @FormParam("jvm") String jvm, @FormParam("os") String os,
			@FormParam("arch") String arch, @FormParam("debug") List<String> debug)
	{
		boolean debugJVM = (debug != null) && !debug.isEmpty();

		try
		{
			String form = ServiceUtil.buildForm(jvm, os, arch, debugJVM);

			command = command.replace("\n", " ").replace("&#8209;", "-");

			boolean storeDTO = !command.contains("com.chrisnewland.someproject.SomeApplication");

			String result = CommandLineSwitchParser.buildReport(command, jvm, os, arch, debugJVM, storeDTO);

			String storedMessage = "";

			if (!storeDTO)
			{
				storedMessage = "Not updating statistics database when command line contains the example class 'com.chrisnewland.someproject.SomeApplication'";
			}
			else
			{
				statsCacheIsValid = false;
			}

			form = form.replace("%STORED%", storedMessage);

			return form.replace("%COMMAND%", Encode.forHtml(command)).replace("%RESULT%", result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("retrieve/{requestId}") @Produces(MediaType.TEXT_HTML) public String retrieve(@PathParam("requestId") long requestId)
	{
		return ServiceUtil.buildRetrievedRequest(requestId);
	}

	@GET @Path("about") @Produces(MediaType.TEXT_HTML) public String about()
	{
		try
		{
			return ServiceUtil.showAbout();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("privacy") @Produces(MediaType.TEXT_HTML) public String privacy()
	{
		try
		{
			return ServiceUtil.showPrivacy();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("stats") @Produces(MediaType.TEXT_HTML) public String stats()
	{
		long now = System.currentTimeMillis();

		if (generatedStatsHTML == null || !statsCacheIsValid)
		{
			try
			{
				String template = ServiceUtil.loadStats();

				StringBuilder builder = new StringBuilder();

				builder.append(ReportBuilder.getLastRequests(5));
				builder.append(ReportBuilder.getReportJVMCounts());
				builder.append(ReportBuilder.getReportOperatingSystemCounts());
				builder.append(ReportBuilder.getReportArchitectureCounts());
				builder.append(ReportBuilder.getReportSwitchNameCounts());
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.OK, "No errors"));
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.WARNING, "With warnings"));
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.ERROR, "With errors"));

				builder.append("<div class=\"divclear\"></div>");
				builder.append(ReportBuilder.getTopErrors());

				builder.append("<div class=\"divclear\"></div>");
				builder.append(ReportBuilder.getTopWarnings());

				template = template.replace("%REPORTS%", builder.toString());

				generatedStatsHTML = template;
				statsCacheIsValid = true;

				return template;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return "An error occurred! Tell @chriswhocodes!";
			}
		}
		else
		{
			System.out.println("Returning cached stats page");
			return generatedStatsHTML;
		}
	}
}
