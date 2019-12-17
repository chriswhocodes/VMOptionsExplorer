/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.chrisnewland.vmoe.parser.*;
import com.chrisnewland.vmoe.parser.delta.GraalDeltaTable;
import com.chrisnewland.vmoe.parser.delta.HotSpotDeltaTable;
import com.chrisnewland.vmoe.parser.delta.IDeltaTable;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;
import com.chrisnewland.vmoe.parser.intrinsic.IntrinsicParser;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.HashMap;

public class VMOptionsExplorer
{
	private File vmPath;

	private File serialiseDir;

	private List<VMData> vmDataList = new ArrayList<>();

	private Map<String, String> switchNameVersions = new HashMap<>();

	private Path vmoeDir;

	public VMOptionsExplorer(Path vmoeDir)
	{
		this.vmoeDir = vmoeDir;
	}

	public void addVM(VMData data)
	{
		vmDataList.add(data);
	}

	public void process(Path serialiseDir) throws Exception
	{
		if (serialiseDir != null)
		{
			prepareSerialisationDir(serialiseDir);
		}

		for (VMData vmData : vmDataList)
		{
			parseJDK(vmData);
		}
	}

	private void prepareSerialisationDir(Path serialisePath)
	{
		File dir = serialisePath.toFile();

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		if (!dir.exists() && dir.isDirectory())
		{
			throw new RuntimeException("Could not create serialisation dir: " + serialisePath);
		}

		this.serialiseDir = dir;
	}

	public void processVMDeltas(VMType vmType, String title, Path templatePath, Path outputFile) throws IOException
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

		String template = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);

		String headerHTML = new String(Files.readAllBytes(vmoeDir.resolve("templates/header.html")), StandardCharsets.UTF_8);

		template = template.replace("$HEADER_HTML", headerHTML);
		template = template.replace("$GRAAL_VERSION", graalVersion);
		template = template.replace("$DELTA_BODY", builder.toString());
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$H1_TITLE", title);

		Files.write(outputFile, template.getBytes(StandardCharsets.UTF_8));
	}

	private IDeltaTable createDeltaTable(VMData earlier, VMData later)
	{
		switch (earlier.getVmType())
		{
		case HOTSPOT:
			return new HotSpotDeltaTable(earlier, later);
		case GRAAL_VM:
		case GRAAL_NATIVE:
			return new GraalDeltaTable(earlier, later);
		default:
			throw new UnsupportedOperationException();
		}
	}

	private void addChangesBetweenVMs(VMData earlier, VMData later, StringBuilder builder) throws IOException
	{
		System.out.println("Calculating differences between " + earlier.getJdkName() + " and " + later.getJdkName());

		SwitchInfoMap switchMapEarlier = getParser(earlier.getVmType()).process(earlier.getVmPath());

		SwitchInfoMap switchMapLater = getParser(later.getVmType()).process(later.getVmPath());

		Set<SwitchInfo> inEarlier = new HashSet<>(switchMapEarlier.values());

		Set<SwitchInfo> inLater = new HashSet<>(switchMapLater.values());

		IDeltaTable deltaTable = createDeltaTable(earlier, later);

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
			@Override public int compare(VMData vmd1, VMData vmd2)
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
		case GRAAL_VM:
			return new GraalVMSwitchParser();
		case GRAAL_NATIVE:
			return new GraalNativeImageSwitchParser();
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
		case GRAAL_VM:
			return "GraalVM";
		case GRAAL_NATIVE:
			return "Graal Native";
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

	private String graalVersion;

	public void setGraalVersion(String version)
	{
		this.graalVersion = version;
	}

	private void parseJDK(VMData vmData) throws IOException
	{
		this.vmPath = vmData.getVmPath();

		String jdkName = vmData.getJdkName();

		VMType vmType = vmData.getVmType();

		String vmName = getVMDisplayName(vmData.getVmType());

		ISwitchParser switchParser = getParser(vmData.getVmType());

		SwitchInfoMap switchInfoMap = switchParser.process(vmPath);

		File usageFile = vmData.getUsageFile();

		if (usageFile != null)
		{
			SwitchInfoMap mapXUsage = new XUsageParser().process(usageFile);

			switchInfoMap.putAll(mapXUsage);
		}

		if (serialiseDir != null)
		{
			Path serialisationPath = Paths.get(serialiseDir.getAbsolutePath(), jdkName);

			Serialiser.serialise(serialisationPath, switchInfoMap.values());
		}

		String template = new String(Files.readAllBytes(vmoeDir.resolve("templates/template.html")), StandardCharsets.UTF_8);

		String headerHTML = new String(Files.readAllBytes(vmoeDir.resolve("templates/header.html")), StandardCharsets.UTF_8);

		StringBuilder htmlBuilder = new StringBuilder();

		Set<String> switchNames = new HashSet<>();

		for (Map.Entry<String, SwitchInfo> entry : switchInfoMap.entrySet())
		{
			SwitchInfo info = entry.getValue();

			String switchName = info.getName();

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
		template = template.replace("$GRAAL_VERSION", graalVersion);
		template = template.replace("$THEAD", SwitchInfo.getHeaderRow(vmType));
		template = template.replace("$VMNAME", vmName);
		template = template.replace("$JDK", jdkName);
		template = template.replace("$COUNT", Integer.toString(switchNames.size()));
		template = template.replace("$DATE", new Date().toString());
		template = template.replace("$TBODY", htmlBuilder.toString());

		String outputFilename = "html/" + vmData.getHTMLFilename();

		switch (vmData.getVmType())
		{
		case GRAAL_VM:
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2,3 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1 ]");
			break;

		case GRAAL_NATIVE:
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th><th>Availability</th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2,3,4 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1,3 ]");
			break;
		case OPENJ9:
			template = template.replace("$TOPHEADER", "<th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1 ]");
			template = template.replace("$SORTCOLUMNS", "[ ]");
			break;
		case HOTSPOT:
			template = template.replace("$TOPHEADER",
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
		long before = Runtime.getRuntime().freeMemory();

		if (args.length != 2)
		{
			System.err.println("VMOptionsExplorer <jdk base dir> <VMOptionsExplorer base dir>");
			System.exit(-1);
		}

		Path baseDir = Paths.get(args[0]);

		Path vmoeDir = Paths.get(args[1]);

		boolean processHotSpot = true;
		boolean processHotSpotDeprecated = true;
		boolean processHotSpotIntrinsics = true;
		boolean processGraalVM = true;
		boolean processOpenJ9 = true;
		boolean processZing = true;

		// parse deprecation info in JDK release order

		if (processHotSpotDeprecated)
		{
			DeprecatedParser.parseFile(baseDir.resolve("jdk10"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk11"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk12"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk13"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk14"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk15"));
		}

		VMOptionsExplorer explorer = new VMOptionsExplorer(vmoeDir);

		explorer.setGraalVersion("19.3.0");

		if (processHotSpot)
		{
			String pre10Usage = "src/share/vm/Xusage.txt";
			String post10Usage = "share/Xusage.txt";
			String post13Usage = "../java.base/share/classes/sun/launcher/resources/launcher.properties";

			explorer.addVM(new VMData("JDK6", baseDir.resolve("jdk6/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(new VMData("JDK7", baseDir.resolve("jdk7u/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(new VMData("JDK8", baseDir.resolve("jdk8u/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(new VMData("JDK9", baseDir.resolve("jdk9/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(
					new VMData("JDK10", baseDir.resolve("jdk10/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post10Usage));
			explorer.addVM(
					new VMData("JDK11", baseDir.resolve("jdk11/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post10Usage));
			explorer.addVM(
					new VMData("JDK12", baseDir.resolve("jdk12/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post10Usage));
			explorer.addVM(
					new VMData("JDK13", baseDir.resolve("jdk13/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post13Usage));
			explorer.addVM(
					new VMData("JDK14", baseDir.resolve("jdk14/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post13Usage));
			explorer.addVM(
					new VMData("JDK15", baseDir.resolve("jdk15/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(post13Usage));
		}

		if (processGraalVM)
		{
			String[] jdkVersions = new String[] { "8", "11" };

			for (String version : jdkVersions)
			{
				explorer.addVM(new VMData("GraalVM CE JDK" + version, vmoeDir.resolve("graal_ce_java" + version + ".vm").toFile(),
						VMType.GRAAL_VM));
				explorer.addVM(new VMData("GraalVM EE JDK" + version, vmoeDir.resolve("graal_ee_java" + version + ".vm").toFile(),
						VMType.GRAAL_VM));

				explorer.addVM(new VMData("GraalVM native-image CE JDK" + version,
						vmoeDir.resolve("graal_ce_java" + version + ".native").toFile(), VMType.GRAAL_NATIVE));
				explorer.addVM(new VMData("GraalVM native-image EE JDK" + version,
						vmoeDir.resolve("graal_ee_java" + version + ".native").toFile(), VMType.GRAAL_NATIVE));
			}
		}

		if (processOpenJ9)
		{
			explorer.addVM(new VMData("OpenJ9", baseDir.resolve("openj9").toFile(), VMType.OPENJ9));
		}

		if (processZing)
		{
			// /opt/zing/zing-jdk8/bin/java \
			// -XX:+PrintFlagsFinal >zing.out 2>/dev/null
			explorer.addVM(new VMData("Zing JDK8", vmoeDir.resolve("zing8.out").toFile(), VMType.ZING));

			explorer.addVM(new VMData("Zing JDK11", vmoeDir.resolve("zing11.out").toFile(), VMType.ZING));
		}

		Path serialiseDir = baseDir.resolve("serialised");

		explorer.process(serialiseDir);

		if (processHotSpot)
		{
			explorer.processVMDeltas(VMType.HOTSPOT, "Differences between HotSpot VM Versions",
					vmoeDir.resolve("templates/template_hotspot_delta.html"),
					vmoeDir.resolve("html/hotspot_option_differences.html"));
		}

		if (processGraalVM)
		{
			String[] jdkVersions = new String[] { "8", "11" };

			for (String version : jdkVersions)
			{
				explorer.processVMDeltas(VMType.GRAAL_VM, "Additonal options in GraalVM Enterprise Edition",
						vmoeDir.resolve("templates/template_graal_delta.html"),
						vmoeDir.resolve("html/graalvm_ee_only_jdk" + version + "_options.html"));

				explorer.processVMDeltas(VMType.GRAAL_NATIVE, "Additonal options in Graal Native Enterprise Edition",
						vmoeDir.resolve("templates/template_graal_delta.html"),
						vmoeDir.resolve("html/graalvm_native_image_ee_only_jdk" + version + "_options.html"));
			}
		}

		if (processHotSpotIntrinsics)
		{
			IntrinsicParser intrinsicParser = new IntrinsicParser();

			intrinsicParser.parseFile(baseDir.resolve("jdk6/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK6");

			intrinsicParser.parseFile(baseDir.resolve("jdk7u/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK7");

			intrinsicParser.parseFile(baseDir.resolve("jdk8u/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK8");

			intrinsicParser.parseFile(baseDir.resolve("jdk9/hotspot/src/share/vm/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK9");

			intrinsicParser.parseFile(baseDir.resolve("jdk10/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK10");

			intrinsicParser.parseFile(baseDir.resolve("jdk11/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK11");

			intrinsicParser.parseFile(baseDir.resolve("jdk12/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK12");

			intrinsicParser.parseFile(baseDir.resolve("jdk13/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK13");

			intrinsicParser.parseFile(baseDir.resolve("jdk14/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK14");

			intrinsicParser.parseFile(baseDir.resolve("jdk15/src/hotspot/share/classfile/vmSymbols.hpp"));
			intrinsicParser.createHTMLForVM("JDK15");
		}
	}
}
