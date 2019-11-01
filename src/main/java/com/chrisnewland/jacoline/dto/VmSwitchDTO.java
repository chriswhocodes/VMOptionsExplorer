/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.dto;

import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.jacoline.commandline.SwitchStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VmSwitchDTO
{
	private long requestId;
	private KeyValue keyValue;
	private SwitchStatus status;
	private String analysis;

	public VmSwitchDTO(long requestId, KeyValue keyValue, SwitchStatus status, String analysis)
	{
		this.requestId = requestId;
		this.keyValue = keyValue;
		this.status = status;
		this.analysis = analysis;
	}

	private VmSwitchDTO()
	{
	}

	public long getRequestId()
	{
		return requestId;
	}

	public KeyValue getKeyValue()
	{
		return keyValue;
	}

	public SwitchStatus getStatus()
	{
		return status;
	}

	public String getAnalysis()
	{
		return analysis;
	}

	// =================================================================
	// LOAD FROM RESULTSET
	// =================================================================
	private void loadFromResultSet(ResultSet rs) throws SQLException
	{
		int paramPos = 1;

		requestId = rs.getLong(paramPos++);

		String prefix = rs.getString(paramPos++);
		String name = rs.getString(paramPos++);
		String value = rs.getString(paramPos++);

		keyValue = new KeyValue(prefix, name, value);

		status = SwitchStatus.valueOf(rs.getString(paramPos++));

		analysis = rs.getString(paramPos++);
	}

	private void loadAllFromResultSet(List<VmSwitchDTO> results, ResultSet rs) throws SQLException
	{
		while (rs.next())
		{
			VmSwitchDTO datum = new VmSwitchDTO();

			datum.loadFromResultSet(rs);

			results.add(datum);
		}
	}

	// =================================================================
	// SELECT BY ID
	// =================================================================

	public boolean loadByRequestId(String requestId) throws SQLException
	{
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createPSLoadFromRequestId(conn, requestId);
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

	private PreparedStatement createPSLoadFromRequestId(Connection conn, String requestId) throws SQLException
	{
		String selectSQL = "SELECT * FROM vm_switch WHERE request_id=?";

		PreparedStatement ps = DatabaseManager.prepare(conn, selectSQL);

		ps.setString(1, requestId);

		return ps;
	}

	// =================================================================
	// SELECT ALL
	// =================================================================

	public List<VmSwitchDTO> loadAll() throws SQLException
	{
		List<VmSwitchDTO> result = new ArrayList<>();

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
		String selectSQL = "SELECT * FROM vm_switch";

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
		}
	}

	private String getInsertSQL()
	{
		return "INSERT INTO vm_switch (request_id, prefix, name, value, status, analysis) VALUES (?, ?, ?, ?, ?::switch_status, ?)";
	}

	private PreparedStatement createPSForInsert(Connection conn) throws SQLException
	{
		String insertSQL = getInsertSQL();

		int paramPos = 1;

		PreparedStatement ps = DatabaseManager.prepare(conn, insertSQL);

		ps.setLong(paramPos++, requestId);
		ps.setString(paramPos++, keyValue.getPrefix());
		ps.setString(paramPos++, keyValue.getKey());
		ps.setString(paramPos++, keyValue.getValue());
		ps.setString(paramPos++, status.toString());
		ps.setString(paramPos++, analysis);

		return ps;
	}
}