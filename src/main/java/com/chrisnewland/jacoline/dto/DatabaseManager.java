/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.dto;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager
{
	private static Properties databaseProperties;

	public static void initialise(Path databasePropertiesPath)
	{
		databaseProperties = new Properties();

		try (FileReader reader = new FileReader(databasePropertiesPath.toFile()))
		{
			databaseProperties.load(reader);
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Could not load database properties from " + databasePropertiesPath, ioe);
		}
	}

	public static Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(databaseProperties.getProperty("jdbc_url"), databaseProperties);
	}

	public static PreparedStatement prepare(Connection connection, String sql) throws SQLException
	{
		return connection.prepareStatement(sql);
	}

	public static PreparedStatement prepareWithID(Connection connection, String sql) throws SQLException
	{
		return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}
}
