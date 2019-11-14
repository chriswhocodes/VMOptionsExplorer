/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.report;

import com.chrisnewland.jacoline.commandline.AnalysedSwitchResult;
import com.chrisnewland.jacoline.commandline.CommandLineSwitchParser;
import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.jacoline.commandline.SwitchStatus;
import com.chrisnewland.jacoline.dto.DatabaseManager;
import com.chrisnewland.jacoline.web.service.ServiceUtil;
import org.owasp.encoder.Encode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportBuilder
{
	public static String getReportJVMCounts()
	{
		return createReport("JVM Counts", new String[] { "JVM", "Count" }, getSingleColumnCount("request", "jvm"));
	}

	public static String getReportOperatingSystemCounts()
	{
		return createReport("OS Counts", new String[] { "OS", "Count" }, getSingleColumnCount("request", "os"));
	}

	public static String getReportArchitectureCounts()
	{
		return createReport("CPU Counts", new String[] { "CPU", "Count" }, getSingleColumnCount("request", "arch"));
	}

	public static String getReportSwitchNameCounts()
	{
		return createReport("Switch Counts (all)", new String[] { "Switch", "Count" }, getSwitchToplist(null));
	}

	public static String getReportSwitchNameCountsWithStatus(SwitchStatus status, String subtitle)
	{
		return createReport("Switch Counts (" + subtitle + ")", new String[] { "Switch", "Count" }, getSwitchToplist(status),
				"stats_container", status.getCssClass());
	}

	public static String getTopWarnings()
	{
		SwitchStatus status = SwitchStatus.WARNING;
		return createReport("Top Warning Reasons", new String[] { "Switch", "Warning", "Count" }, getSwitchAnalysisToplist(status),
				"top_reasons", status.getCssClass());
	}

	public static String getTopErrors()
	{
		SwitchStatus status = SwitchStatus.ERROR;
		return createReport("Top Error Reasons", new String[] { "Switch", "Error", "Count" }, getSwitchAnalysisToplist(status),
				"top_reasons", status.getCssClass());
	}

	public static String getLastRequests(int limit)
	{
		return createReport("Last " + limit + " Inspections", new String[] { "Date", "JVM", "OS", "CPU", "Request", "DebugVM" },
				getRecentRequests(limit));
	}

	private static String createReport(String title, String[] columnHeaders, List<ReportRow> rows)
	{
		return createReport(title, columnHeaders, rows, "stats_container", "skyblue");
	}

	private static String createReport(String title, String[] columnHeaders, List<ReportRow> rows, String containerClass,
			String cssClassTH)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("\n\n<div class=\"").append(containerClass).append("\">");
		builder.append("<h2>").append(title).append("</h2>");
		builder.append("<table class=\"stats_table\">");

		builder.append("<tr class=\"").append(cssClassTH).append("\">");
		for (String header : columnHeaders)
		{
			builder.append("<th>").append(header).append("</th>");
		}
		builder.append("</tr>");

		for (ReportRow row : rows)
		{
			builder.append("<tr>");

			for (String col : row.getColumns())
			{
				builder.append("<td>").append(col).append("</td>");
			}

			builder.append("<tr>");
		}

		builder.append("</table>");
		builder.append("</div>");

		return builder.toString();
	}

	private static List<ReportRow> getSingleColumnCount(String tableName, String colName)
	{
		List<ReportRow> result = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createStatementSingleColumn(conn, tableName, colName);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				ReportRow row = new ReportRow(2);

				row.setColumn(0, clean(rs.getString(1)));
				row.setColumn(1, rs.getString(2));

				result.add(row);
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return result;
	}

	private static List<ReportRow> getSwitchToplist(SwitchStatus switchStatus)
	{
		List<ReportRow> result = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createStatementGetSwitchToplist(conn, switchStatus);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				ReportRow row = new ReportRow(2);

				row.setColumn(0, rs.getString(1));
				row.setColumn(1, rs.getString(2));

				result.add(row);
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return result;
	}

	private static List<ReportRow> getSwitchAnalysisToplist(SwitchStatus switchStatus)
	{
		List<ReportRow> result = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createStatementGetSwitchAnalysisToplist(conn, switchStatus);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				ReportRow row = new ReportRow(3);

				String concat = rs.getString(1);
				String count = rs.getString(2);

				String[] parts = concat.split("@");

				row.setColumn(0, parts[0]);
				row.setColumn(1, clean(parts[1]));
				row.setColumn(2, count);

				result.add(row);
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return result;
	}

	private static List<ReportRow> getRecentRequests(int limit)
	{
		List<ReportRow> result = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = createStatementGetRecentRequests(conn, limit);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				ReportRow row = new ReportRow(6);

				// "id", "JVM", "OS", "CPU", "Request", "DebugVM"

				long id = rs.getLong(1);

				String date = rs.getDate(2).toString().replace("-", "/");

				String jvm = clean(rs.getString(3));

				String os = clean(rs.getString(4));

				String cpu = clean(rs.getString(5));

				String request = rs.getString(6);

				boolean debugVM = rs.getBoolean(7);

				String analysedRequest = analyseRequest(id, jvm, os, cpu, request, debugVM);

				row.setColumn(0, date);
				row.setColumn(1, jvm);
				row.setColumn(2, os);
				row.setColumn(3, cpu);
				row.setColumn(4, analysedRequest);
				row.setColumn(5, debugVM ? "Y" : "N");

				result.add(row);
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		return result;
	}

	private static String analyseRequest(long id, String jvm, String os, String arch, String request, boolean debugVM)
	{
		List<KeyValue> parsedSwitches = CommandLineSwitchParser.parse(request);

		int unlockFlags = CommandLineSwitchParser.getUnlockFlags(parsedSwitches, debugVM);

		String identity = "";

		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < parsedSwitches.size(); index++)
		{
			AnalysedSwitchResult result = CommandLineSwitchParser.getSwitchAnalysis(identity, jvm, index, parsedSwitches, os, arch,
					unlockFlags);

			builder.append(result.getKeyValueHTML("/retrieve/" + id, identity));
		}

		return builder.toString();
	}

	private static String clean(String input)
	{
		if (input != null)
		{
			input = input.replace(ServiceUtil.OPTION_ANY, "-");
		}

		return Encode.forHtml(input);
	}

	private static PreparedStatement createStatementSingleColumn(Connection conn, String tableName, String colName)
			throws SQLException
	{
		String selectSQL = "SELECT " + colName + ", COUNT(" + colName + ") FROM " + tableName + " GROUP BY 1 ORDER BY 2 DESC";

		PreparedStatement ps = DatabaseManager.prepare(conn, selectSQL);

		return ps;
	}

	private static PreparedStatement createStatementGetSwitchToplist(Connection conn, SwitchStatus switchStatus) throws SQLException
	{
		PreparedStatement ps;

		if (switchStatus == null)
		{
			String selectSQL = "SELECT switch, COUNT(switch) FROM " + getSwitchQuery() + " GROUP BY 1 ORDER BY 2 DESC";

			ps = DatabaseManager.prepare(conn, selectSQL);
		}
		else
		{
			String selectSQL = "SELECT switch, COUNT(switch) FROM " + getSwitchQuery()
					+ " WHERE status=?::switch_status GROUP BY 1 ORDER BY 2 DESC";

			ps = DatabaseManager.prepare(conn, selectSQL);

			ps.setString(1, switchStatus.toString());
		}

		return ps;
	}

	private static String getSwitchQuery()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		builder.append("SELECT prefix||'+'||name AS switch, * FROM vm_switch WHERE value='true'");
		builder.append(" UNION ALL ");
		builder.append("SELECT prefix||'-'||name AS switch, * FROM vm_switch WHERE value='false'");
		builder.append(" UNION ALL ");
		builder.append("SELECT prefix||name AS switch, * FROM vm_switch WHERE value NOT IN ('true', 'false')");
		builder.append(") AS switches");

		return builder.toString();
	}

	private static PreparedStatement createStatementGetSwitchAnalysisToplist(Connection conn, SwitchStatus switchStatus)
			throws SQLException
	{
		PreparedStatement ps;

		String concat = "switch||'@'||analysis";

		String selectSQL = "SELECT " + concat + ", COUNT(" + concat + ") FROM " + getSwitchQuery()
				+ " WHERE status=?::switch_status GROUP BY 1 ORDER BY 2 DESC";

		ps = DatabaseManager.prepare(conn, selectSQL);

		ps.setString(1, switchStatus.toString());

		return ps;
	}

	private static PreparedStatement createStatementGetRecentRequests(Connection conn, int limit) throws SQLException
	{
		String selectSQL = "SELECT id, recorded_at, jvm, os, arch, request, debug_vm FROM request ORDER BY 1 DESC LIMIT ?";

		PreparedStatement ps = DatabaseManager.prepare(conn, selectSQL);

		ps.setLong(1, limit);

		return ps;
	}
}
