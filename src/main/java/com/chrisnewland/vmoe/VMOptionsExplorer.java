/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.chrisnewland.vmoe.parser.GraalSwitchParser;
import com.chrisnewland.vmoe.parser.HotSpotSwitchParser;
import com.chrisnewland.vmoe.parser.ISwitchParser;
import com.chrisnewland.vmoe.parser.OpenJ9SwitchParser;
import com.chrisnewland.vmoe.parser.ZingSwitchParser;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;
import com.chrisnewland.vmoe.parser.intrinsic.IntrinsicParser;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.HashMap;

public class VMOptionsExplorer
{
	private File vmPath;

	private List<VMData> vmDataList = new ArrayList<>();

	private Map<String, String> switchNameVersions = new HashMap<>();

	public VMOptionsExplorer()
	{
	}

	public void addVM(VMData data)
	{
		vmDataList.add(data);
	}

	public void process() throws Exception
	{
		int count = vmDataList.size();

		for (int i = 0; i < count; i++)
		{
			parseJDK(i, vmDataList.get(i));
		}
	}

	public void processVMDeltas(VMType vmType) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		List<VMData> vmsOfType = getVMsOfType(vmType);

		int count = vmsOfType.size();

		if (count > 1)
		{
			for (int i = 0; i < count - 1; i++)
			{
				VMData earlier = vmsOfType.get(i);
				VMData later = vmsOfType.get(i + 1);

				addChangesBetweenVMs(earlier, later, builder);
			}
		}

		String template = new String(Files.readAllBytes(Paths.get("templates/template_delta.html")), StandardCharsets.UTF_8);

		String headerHTML = new String(Files.readAllBytes(Paths.get("templates/header.html")), StandardCharsets.UTF_8);

		template = template.replace("$HEADER_HTML", headerHTML);
		template = template.replace("$DELTA_BODY", builder.toString());
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$H1_TITLE", "Differences between HotSpot VM Versions");

		Files.write(Paths.get("html/hotspot_option_differences.html"), template.getBytes(StandardCharsets.UTF_8));
	}

	private void addChangesBetweenVMs(VMData earlier, VMData later, StringBuilder builder) throws IOException
	{
		System.out.println("Calculating changes between " + earlier.getJdkName() + " and " + later.getJdkName());

		Map<String, SwitchInfo> switchMapEarlier = getParser(earlier.getVmType()).process(earlier.getVmPath());

		Map<String, SwitchInfo> switchMapLater = getParser(later.getVmType()).process(later.getVmPath());

		Set<SwitchInfo> inEarlier = new HashSet<>(switchMapEarlier.values());

		Set<SwitchInfo> inLater = new HashSet<>(switchMapLater.values());

		builder.append("<hr>");

		builder
				.append("<h2 class=\"deltaH2\" id=\"").append(later.getJdkName()).append("\">")
				.append("Differences between ")
				.append(earlier.getJdkName())
				.append(" and ")
				.append(later.getJdkName())
				.append("</h2>");

		builder.append("<hr>");

		DeltaTable deltaTable = new DeltaTable(earlier, later);

		for (SwitchInfo switchInfo : inEarlier)
		{
			if (!inLater.contains(switchInfo))
			{
				deltaTable.recordRemoval(switchInfo);
			}
		}

		for (SwitchInfo switchInfo : inLater)
		{
			if (!inEarlier.contains(switchInfo))
			{
				deltaTable.recordAddition(switchInfo);
			}
		}

		System.out.println("Removed " + deltaTable.getRemovalCount() + " Added " + deltaTable.getAdditionCount());

		builder.append(deltaTable.toString());
	}

	private List<VMData> getVMsOfType(VMType vmType)
	{
		List<VMData> result = new ArrayList<>();

		for (VMData vmData : vmDataList)
		{
			if (vmData.getVmType() == vmType)
			{
				result.add(vmData);
			}
		}

		Collections.sort(result, new Comparator<VMData>()
		{
			@Override
			public int compare(VMData vmd1, VMData vmd2)
			{
				int version1 = getVersionFromJDKName(vmd1.getJdkName());
				int version2 = getVersionFromJDKName(vmd2.getJdkName());

				return Integer.compare(version1, version2);
			}
		});

		return result;
	}

	private int getVersionFromJDKName(String name)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);

			if (Character.isDigit(c))
			{
				builder.append(c);
			}
		}

		int result = 0;

		try
		{
			result = Integer.parseInt(builder.toString());
		}
		catch (NumberFormatException nfe)
		{
			nfe.printStackTrace();
		}

		return result;
	}

	private ISwitchParser getParser(VMType vmType)
	{
		switch (vmType)
		{
		case GRAAL:
			return new GraalSwitchParser();
		case OPENJ9:
			return new OpenJ9SwitchParser();
		case HOTSPOT:
			return new HotSpotSwitchParser();
		case ZING:
			return new ZingSwitchParser();
		default:
			throw new RuntimeException("Unexpected VM Type: " + vmType);
		}
	}

	private String getVMDisplayName(VMType vmType)
	{
		switch (vmType)
		{
		case GRAAL:
			return "Graal";
		case OPENJ9:
			return "OpenJ9";
		case HOTSPOT:
			return "HotSpot";
		case ZING:
			return "Zing";
		default:
			throw new RuntimeException("Unexpected VM Type: " + vmType);
		}
	}

	private void parseJDK(int jdkIndex, VMData vmData) throws IOException
	{
		this.vmPath = vmData.getVmPath();

		String jdkName = vmData.getJdkName();

		VMType vmType = vmData.getVmType();

		String vmName = getVMDisplayName(vmData.getVmType());

		ISwitchParser switchParser = getParser(vmData.getVmType());

		Map<String, SwitchInfo> switchMap = switchParser.process(vmPath);

		String template = new String(Files.readAllBytes(Paths.get("templates/template.html")), StandardCharsets.UTF_8);

		String headerHTML = new String(Files.readAllBytes(Paths.get("templates/header.html")), StandardCharsets.UTF_8);

		StringBuilder htmlBuilder = new StringBuilder();

		Set<String> switchNames = new HashSet<>();

		for (Map.Entry<String, SwitchInfo> entry : switchMap.entrySet())
		{
			SwitchInfo info = entry.getValue();

			String switchName = info.getName();

			// System.out.println(info);

			if (vmType == VMType.HOTSPOT)
			{
				String firstSeenInJDK = switchNameVersions.get(switchName);

				if (firstSeenInJDK == null)
				{
					firstSeenInJDK = jdkName;

					switchNameVersions.put(switchName, firstSeenInJDK);
				}

				info.setSince(firstSeenInJDK);
			}

			htmlBuilder.append(info.toRow(vmType)).append("\n");

			switchNames.add(switchName);
		}

		template = template.replace("$HEADER_HTML", headerHTML);

		String title = jdkName;

		if (vmData.getVmType() == VMType.HOTSPOT)
		{
			title = title + " HotSpot";
		}

		template = template.replace("$H1_TITLE", title);
		template = template.replace("$THEAD", SwitchInfo.getHeaderRow(vmType));
		template = template.replace("$VMNAME", vmName);
		template = template.replace("$JDK", jdkName);
		template = template.replace("$COUNT", Integer.toString(switchNames.size()));
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$TBODY", htmlBuilder.toString());

		String outputFilename = "html/" + vmData.getHTMLFilename();

		switch (vmData.getVmType())
		{
		case GRAAL:
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2,3 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1 ]");
			break;
		case OPENJ9:
			template = template.replace("$TOPHEADER", "<th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1 ]");
			template = template.replace("$SORTCOLUMNS", "[ ]");
			break;
		case HOTSPOT:
			template = template
								.replace("$TOPHEADER",
										"<th></th><th>Since</th><th>Deprecated</th><th>Type</th><th>OS</th><th>CPU</th><th>Component</th><th></th><th>Availability</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1, 3, 4, 5, 6, 8 ]");
			break;
		case ZING:
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1 ]");
			break;
		}

		Files.write(Paths.get(outputFilename), template.getBytes(StandardCharsets.UTF_8));

		System.out.println(jdkName + " Count: " + switchNames.size());
	}

	public static void main(String[] args) throws Exception
	{
		String baseDir = "/home/chris/openjdk/";

		// parse deprecation info in JDK release order

		DeprecatedParser.parseFile(Paths.get(baseDir + "jdk10"));
		DeprecatedParser.parseFile(Paths.get(baseDir + "jdk11"));
		DeprecatedParser.parseFile(Paths.get(baseDir + "jdk12"));
		DeprecatedParser.parseFile(Paths.get(baseDir + "jdk13"));

		VMOptionsExplorer vms = new VMOptionsExplorer();

		vms.addVM(new VMData("JDK6", new File(baseDir + "jdk6/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK7", new File(baseDir + "jdk7u/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK8", new File(baseDir + "jdk8u/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK9", new File(baseDir + "jdk9-dev/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK10", new File(baseDir + "jdk10/src/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK11", new File(baseDir + "jdk11/src/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK12", new File(baseDir + "jdk12/src/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK13", new File(baseDir + "jdk13/src/hotspot"), VMType.HOTSPOT));

		// Generate these files with -XX:+JVMCIPrintProperties
		vms.addVM(new VMData("Graal CE 19", new File(baseDir + "VMOptionsExplorer/graal_ce.out"), VMType.GRAAL));
		vms.addVM(new VMData("Graal EE 19", new File(baseDir + "VMOptionsExplorer/graal_ee.out"), VMType.GRAAL));

		vms.addVM(new VMData("OpenJ9", new File(baseDir + "openj9"), VMType.OPENJ9));

		// /opt/zing/zing-jdk8/bin/java \
		// -XX:+PrintFlagsFinal >zing.out 2>/dev/null
		vms.addVM(new VMData("Zing", new File(baseDir + "VMOptionsExplorer/zing.out"), VMType.ZING));

		vms.process();

		vms.processVMDeltas(VMType.HOTSPOT);

		IntrinsicParser intrinsicParser = new IntrinsicParser();

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk6/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK6");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk7u/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK7");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk8u/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK8");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk9-dev/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK9");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk10/src/hotspot/share/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK10");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk11/src/hotspot/share/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK11");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk12/src/hotspot/share/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK12");

		intrinsicParser.parseFile(Paths.get(baseDir + "jdk13/src/hotspot/share/classfile/vmSymbols.hpp"));
		intrinsicParser.createHTMLForVM("JDK13");
	}
}
