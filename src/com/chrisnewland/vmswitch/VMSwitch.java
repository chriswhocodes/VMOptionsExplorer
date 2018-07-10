/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.chrisnewland.vmswitch.parser.GraalSwitchParser;
import com.chrisnewland.vmswitch.parser.HotSpotSwitchParser;
import com.chrisnewland.vmswitch.parser.ISwitchParser;
import com.chrisnewland.vmswitch.parser.OpenJ9SwitchParser;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.HashMap;

public class VMSwitch
{
	private File vmPath;

	private List<VMData> vmData = new ArrayList<>();

	private Map<String, String> switchNameVersions = new HashMap<>();

	public VMSwitch()
	{
	}

	public void addVM(VMData data)
	{
		vmData.add(data);
	}

	public void process() throws Exception
	{
		int count = vmData.size();

		for (int i = 0; i < count; i++)
		{
			parseJDK(i, vmData.get(i));
		}
	}

	private void parseJDK(int jdkIndex, VMData vmData) throws IOException
	{
		this.vmPath = vmData.getVmPath();

		String jdkName = vmData.getJdkName();
		VMType vmType = vmData.getVmType();

		String vmName = null;

		switch (vmData.getVmType())
		{
		case GRAAL:
			vmName = "Graal";
			break;
		case OPENJ9:
			vmName = "OpenJ9";
			break;
		case HOTSPOT:
			vmName = "HotSpot";
			break;
		}

		Map<String, SwitchInfo> switchMap = new TreeMap<>();

		ISwitchParser switchParser = null;

		switch (vmData.getVmType())
		{
		case GRAAL:
			switchParser = new GraalSwitchParser();
			break;
		case OPENJ9:
			switchParser = new OpenJ9SwitchParser();
			break;
		case HOTSPOT:
			switchParser = new HotSpotSwitchParser();
			break;
		default:
			throw new RuntimeException("Unexpected VM Type: " + vmData.getVmType());
		}

		switchParser.process(vmPath, switchMap);

		String template = new String(Files.readAllBytes(Paths.get("html/template.html")), StandardCharsets.UTF_8);

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

		template = template.replace("$THEAD", SwitchInfo.getHeaderRow(vmType));
		template = template.replace("$VMNAME", vmName);
		template = template.replace("$JDK", jdkName);
		template = template.replace("$COUNT", Integer.toString(switchNames.size()));
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$TBODY", htmlBuilder.toString());

		String outputFilename = null;

		switch (vmData.getVmType())
		{
		case GRAAL:
			outputFilename = "html/graal_options_" + jdkName.toLowerCase().replace(" ", "_") + ".html";
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2,3 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1 ]");
			break;
		case OPENJ9:
			outputFilename = "html/openj9_options_" + jdkName.toLowerCase().replace(" ", "_") + ".html";
			template = template.replace("$TOPHEADER", "<th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1 ]");
			template = template.replace("$SORTCOLUMNS", "[ ]");
			break;
		case HOTSPOT:
			outputFilename = "html/hotspot_options_" + jdkName.toLowerCase() + ".html";
			template = template
								.replace("$TOPHEADER",
										"<th></th><th>Since</th><th>Type</th><th>OS</th><th>CPU</th><th>Component</th><th></th><th>Availability</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1, 2, 3, 4, 5, 7 ]");
			break;
		}

		Files.write(Paths.get(outputFilename), template.getBytes(StandardCharsets.UTF_8));

		System.out.println(jdkName + " Count: " + switchNames.size());
	}

	public static void main(String[] args) throws Exception
	{
		VMSwitch vms = new VMSwitch();

		vms.addVM(new VMData("JDK6", new File("/home/chris/openjdk/jdk6/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK7", new File("/home/chris/openjdk/jdk7/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK8", new File("/home/chris/openjdk/jdk8u/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK9", new File("/home/chris/openjdk/jdk9-dev/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK10", new File("/home/chris/openjdk/jdk10/src/hotspot"), VMType.HOTSPOT));
		vms.addVM(new VMData("JDK11", new File("/home/chris/openjdk/jdk11/src/hotspot"), VMType.HOTSPOT));

		// Generate these files with -XX:+JVMCIPrintProperties
		vms.addVM(new VMData("Graal CE 1.0", new File("/home/chris/openjdk/VMSwitch/graal_ce.out"), VMType.GRAAL));
		vms.addVM(new VMData("Graal EE 1.0", new File("/home/chris/openjdk/VMSwitch/graal_ee.out"), VMType.GRAAL));

		vms.addVM(new VMData("OpenJ9", new File("/home/chris/openjdk/openj9"), VMType.OPENJ9));

		vms.process();
	}
}
