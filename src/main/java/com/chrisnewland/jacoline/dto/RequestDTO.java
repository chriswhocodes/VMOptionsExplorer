/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.dto;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestDTO
{
	private long id = -1;
	private String request;
	private String jvm;
	private String os;
	private String arch;
	private boolean debug_vm;
	private Date recorded_at;

	public RequestDTO(String request, String jvm, String os, String arch, boolean debugVm)
	{
		this.request = request;
		this.jvm = jvm;
		this.os = os;
		this.arch = arch;
		this.debug_vm = debugVm;
		this.recorded_at = new Date();
	}

	private RequestDTO()
	{
	}

	public long getId()
	{
		return id;
	}

	public String getRequest()
	{
		return request;
	}

	public String getJvm()
	{
		return jvm;
	}

	public String getOs()
	{
		return os;
	}

	public String getArch()
	{
		return arch;
	}

	public boolean isDebugVm()
	{
		return debug_vm;
	}

	public Date getRecordedAt()
	{
		return recorded_at;
	}

	// =================================================================
	// LOAD FROM RESULTSET
	// =================================================================
	private void loadFromResultSet(ResultSet rs) throws SQLException
	{
		int paramPos = 1;

		id = rs.getInt(paramPos++);
		request = rs.getString(paramPos++);
		jvm = rs.getString(paramPos++);
		os = rs.getString(paramPos++);
		arch = rs.getString(paramPos++);
		debug_vm = rs.getBoolean(paramPos++);
		recorded_at = rs.getDate(paramPos++);
	}

	private void loadAllFromResultSet(List<RequestDTO> results, ResultSet rs) throws SQLException
	{
		while (rs.next())
		{
			RequestDTO datum = new RequestDTO();

			datum.loadFromResultSet(rs);

			results.add(datum);
		}
	}

	// =================================================================
	// SELECT BY ID
	// =================================================================

	public static RequestDTO loadById(long id)
	{
		RequestDTO requestDTO = new RequestDTO();

		boolean loaded = false;

		try
		{
			loaded = requestDTO.load(id);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return loaded ? requestDTO : null;
	}

	private boolean load(long id) throws SQLException
	{
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createPSLoadFromId(conn, id);
				ResultSet rs = ps.executeQuery())
		{
			boolean found = false;

			if (rs.next())
			{
				loadFromResultSet(rs);
				found = true;
			}

			return found;
		}
	}

	private PreparedStatement createPSLoadFromId(Connection conn, long id) throws SQLException
	{
		String selectSQL = "SELECT * FROM request WHERE id=?";

		PreparedStatement ps = DatabaseManager.prepare(conn, selectSQL);

		ps.setLong(1, id);

		return ps;
	}

	// =================================================================
	// SELECT ALL
	// =================================================================

	public List<RequestDTO> loadAll() throws SQLException
	{
		List<RequestDTO> result = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createPSLoadAll(conn);
				ResultSet rs = ps.executeQuery())
		{
			loadAllFromResultSet(result, rs);
		}

		return result;
	}

	private PreparedStatement createPSLoadAll(Connection conn) throws SQLException
	{
		String selectSQL = "SELECT * FROM request";

		PreparedStatement ps = DatabaseManager.prepare(conn, selectSQL);

		return ps;
	}

	// =================================================================
	// INSERT
	// =================================================================

	public void insert() throws SQLException
	{
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = createPSForInsert(conn))
		{
			ps.execute();

			ResultSet resultSet = ps.getGeneratedKeys();

			if (resultSet.next())
			{
				this.id = resultSet.getLong(1);
			}
		}
	}

	private String getInsertSQL()
	{
		return "INSERT INTO request (request, jvm, os, arch, debug_vm, recorded_at) VALUES (?, ?, ?, ?, ?, ?)";
	}

	private PreparedStatement createPSForInsert(Connection conn) throws SQLException
	{
		String insertSQL = getInsertSQL();

		int paramPos = 1;

		PreparedStatement ps = DatabaseManager.prepareWithID(conn, insertSQL);

		Timestamp timestamp = new Timestamp(recorded_at.getTime());

		ps.setString(paramPos++, request);
		ps.setString(paramPos++, jvm);
		ps.setString(paramPos++, os);
		ps.setString(paramPos++, arch);
		ps.setBoolean(paramPos++, debug_vm);
		ps.setTimestamp(paramPos++, timestamp);
		return ps;
	}
}