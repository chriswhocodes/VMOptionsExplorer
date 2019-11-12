/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web;

import com.chrisnewland.jacoline.dto.DatabaseManager;
import com.chrisnewland.jacoline.web.filter.RequestFilter;
import com.chrisnewland.jacoline.web.service.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 4)
		{
			System.err.println("WebServer <resources dir> <database properties file> <serialised dir> <bad words file>");
			System.exit(-1);
		}

		Path resourcesPath = Paths.get(args[0]);

		ServiceUtil.initialise(resourcesPath);

		Path databasePropertiesPath = Paths.get(args[1]);

		DatabaseManager.initialise(databasePropertiesPath);

		ResourceConfig config = new ResourceConfig();

		Path serialisedPath = Paths.get(args[2]);

		CommandLineService.initialise(serialisedPath);

		Path badWordsPath = Paths.get(args[3]);

		RequestFilter.initialise(badWordsPath);

		String packageWeb = WebServer.class.getPackage().getName();

		config.packages(packageWeb + ".service");
		config.packages(packageWeb + ".filter");

		Server server = new Server(new InetSocketAddress("127.0.0.1", 4444));

		ServletContextHandler context = new ServletContextHandler(server, "/");

		Path staticResourcePath = Paths.get(resourcesPath.toString(), "static");

		ServletHolder holderHome = new ServletHolder("static-home", DefaultServlet.class);

		holderHome.setInitParameter("resourceBase", staticResourcePath.toString());
		holderHome.setInitParameter("dirAllowed", "true");
		holderHome.setInitParameter("pathInfoOnly", "true");

		context.addServlet(holderHome, "/static/*");

		ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
		context.addServlet(holderDefault, "/");

		ServletHolder servletHolder = new ServletHolder(new ServletContainer(config));

		context.addServlet(servletHolder, "/*");

		try
		{
			server.start();
			server.join();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		finally
		{
			server.destroy();
		}
	}
}
