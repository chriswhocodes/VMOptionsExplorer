/*
 * Copyright (c) 2018-2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.chrisnewland.vmoe.compare.VMDataComparator;
import com.chrisnewland.vmoe.parser.*;
import com.chrisnewland.vmoe.parser.delta.GraalDeltaTable;
import com.chrisnewland.vmoe.parser.delta.HotSpotDeltaTable;
import com.chrisnewland.vmoe.parser.delta.IDeltaTable;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;
import com.chrisnewland.vmoe.parser.intrinsic.IntrinsicParser;

public class VMOptionsExplorer
{
	private File vmPath;

	private File serialiseDir;

	private Map<String, VMData> vmDataMap = new TreeMap<>();

	private Map<String, String> switchNameVersions = new HashMap<>();

	private Path vmoeDir;

	public VMOptionsExplorer(Path vmoeDir)
	{
		this.vmoeDir = vmoeDir;
	}

	public void addVM(VMData data)
	{
		vmDataMap.put(data.getJdkName(), data);
	}

	public void process(Path serialiseDir) throws Exception
	{
		if (serialiseDir != null)
		{
			prepareSerialisationDir(serialiseDir);
		}

		this.serialiseDir = serialiseDir.toFile();

		for (VMData vmData : vmDataMap.values())
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
		case GRAAL_VM_8:
		case GRAAL_NATIVE_8:
		case GRAAL_VM_11:
		case GRAAL_NATIVE_11:
			return new GraalDeltaTable(earlier, later);
		default:
			throw new UnsupportedOperationException();
		}
	}

	private void addChangesBetweenVMs(VMData earlier, VMData later, StringBuilder builder) throws IOException
	{
		System.out.println("Calculating differences between " + earlier.getJdkName() + " and " + later.getJdkName());

		SwitchInfoMap switchMapEarlier = earlier.getVmType().getParser().process(earlier.getVmPath());

		SwitchInfoMap switchMapLater = later.getVmType().getParser().process(later.getVmPath());

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

		Path serialisationPath = Paths.get(serialiseDir.getAbsolutePath(), later.getSafeJDKName() + "_diffs.json");

		Files.write(serialisationPath, deltaTable.toJSON().getBytes());
	}

	private List<VMData> getVMsOfType(VMType vmType)
	{
		List<VMData> result = new ArrayList<>();

		for (VMData vmData : vmDataMap.values())
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

	public void compareVMData(String vanilla, String flavour) throws IOException
	{
		VMData vmDataVanilla = vmDataMap.get(vanilla);

		VMData vmDataFlavour = vmDataMap.get(flavour);

		StringBuilder[] result = VMDataComparator.compareVMData(vmDataVanilla, vmDataFlavour);

		String template = new String(Files.readAllBytes(vmoeDir.resolve("templates/template_vanilla_vs_flavoured.html")),
				StandardCharsets.UTF_8);

		String headerHTML = new String(Files.readAllBytes(vmoeDir.resolve("templates/header.html")), StandardCharsets.UTF_8);

		template = template.replace("$HEADER_HTML", headerHTML);

		template = template.replace("$H1_TITLE", "Switch comparison of $VANILLA_NAME vs $FLAVOUR_NAME");

		template = template.replace("$GRAAL_VERSION", graalVersion);

		template = template.replace("$VANILLA_NAME", vmDataVanilla.getJdkName());
		template = template.replace("$FLAVOUR_NAME", vmDataFlavour.getJdkName());

		template = template.replace("$FLAVOUR_ADDED", result[0].length() == 0 ? "None" : result[0]);
		template = template.replace("$FLAVOUR_CHANGED", result[1].length() == 0 ? "None" : result[1]);
		template = template.replace("$FLAVOUR_REMOVED", result[2].length() == 0 ? "None" : result[2]);
		template = template.replace("$DATE", new Date().toString());

		String filename = "html/compare_" + vmDataVanilla.getSafeJDKName() + "_" + vmDataFlavour.getSafeJDKName() + ".html";

		Path outputFile = vmoeDir.resolve(filename.toLowerCase());

		Files.write(outputFile, template.getBytes(StandardCharsets.UTF_8));
	}

	private String getVMDisplayName(VMType vmType)
	{
		switch (vmType)
		{
		case GRAAL_VM_8:
		case GRAAL_VM_11:
			return "GraalVM";
		case GRAAL_NATIVE_8:
		case GRAAL_NATIVE_11:
			return "Graal Native";
		case OPENJ9:
			return "OpenJ9";
		case HOTSPOT:
			return "HotSpot";
		case SAPMACHINE:
			return "SapMachine";
		case CORRETTO:
			return "Corretto";
		case ZING:
			return "Zing";
		case ZULU:
			return "Zulu";
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

		ISwitchParser switchParser = vmData.getVmType().getParser();

		SwitchInfoMap switchInfoMap = switchParser.process(vmPath);

		File usageFile = vmData.getUsageFile();

		if (usageFile != null)
		{
			SwitchInfoMap mapXUsage = new XUsageParser().process(usageFile);

			switchInfoMap.putAll(mapXUsage);
		}

		if (serialiseDir != null)
		{
			Path serialisationPath = Paths.get(serialiseDir.getAbsolutePath(), vmData.getSafeJDKName() + ".json");

			Serialiser.serialiseSwitchInfo(serialisationPath, switchInfoMap.values());
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
		case GRAAL_VM_8:
		case GRAAL_VM_11:
			template = template.replace("$TOPHEADER", "<th></th><th>Type</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0,1,2,3 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1 ]");
			break;

		case GRAAL_NATIVE_8:
		case GRAAL_NATIVE_11:
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
		case SAPMACHINE:
		case CORRETTO:
			template = template.replace("$TOPHEADER",
					"<th></th><th>Since</th><th>Deprecated</th><th>Type</th><th>OS</th><th>CPU</th><th>Component</th><th></th><th>Availability</th><th></th><th></th>");
			template = template.replace("$ALLCOLUMNS", "[ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]");
			template = template.replace("$SORTCOLUMNS", "[ 1, 3, 4, 5, 6, 8 ]");
			break;
		case ZING:
		case ZULU:
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
		boolean processZulu = true;
		boolean processSapMachine = true;
		boolean processCorretto = true;

		// parse deprecation info in JDK release order

		if (processHotSpotDeprecated)
		{
			DeprecatedParser.parseFile(baseDir.resolve("jdk10"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk11"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk12"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk13"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk14"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk15"));
			DeprecatedParser.parseFile(baseDir.resolve("jdk16"));
		}

		String graalVersion = "20.2.0";

		VMOptionsExplorer explorer = new VMOptionsExplorer(vmoeDir);

		explorer.setGraalVersion(graalVersion);

		String pre10Usage = "src/share/vm/Xusage.txt";
		String post10Usage = "share/Xusage.txt";
		String post13Usage = "../java.base/share/classes/sun/launcher/resources/launcher.properties";

		if (processHotSpot)
		{
			explorer.addVM(
					new VMData("OpenJDK6", baseDir.resolve("jdk6/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(
					new VMData("OpenJDK7", baseDir.resolve("jdk7/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(
					new VMData("OpenJDK8", baseDir.resolve("jdk8/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(
					new VMData("OpenJDK9", baseDir.resolve("jdk9/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(pre10Usage));
			explorer.addVM(new VMData("OpenJDK10", baseDir.resolve("jdk10/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post10Usage));
			explorer.addVM(new VMData("OpenJDK11", baseDir.resolve("jdk11/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post10Usage));
			explorer.addVM(new VMData("OpenJDK12", baseDir.resolve("jdk12/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post10Usage));
			explorer.addVM(new VMData("OpenJDK13", baseDir.resolve("jdk13/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post13Usage));
			explorer.addVM(new VMData("OpenJDK14", baseDir.resolve("jdk14/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post13Usage));
			explorer.addVM(new VMData("OpenJDK15", baseDir.resolve("jdk15/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post13Usage));
			explorer.addVM(new VMData("OpenJDK16", baseDir.resolve("jdk16/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
					post13Usage));
		}

		if (processGraalVM)
		{
			explorer.addVM(new VMData("GraalVM CE JDK8", vmoeDir.resolve("graal_ce_java8.vm").toFile(), VMType.GRAAL_VM_8));
			explorer.addVM(new VMData("GraalVM EE JDK8", vmoeDir.resolve("graal_ee_java8.vm").toFile(), VMType.GRAAL_VM_8));

			explorer.addVM(new VMData("GraalVM native-image CE JDK8", vmoeDir.resolve("graal_ce_java8.native").toFile(),
					VMType.GRAAL_NATIVE_8));
			explorer.addVM(new VMData("GraalVM native-image EE JDK8", vmoeDir.resolve("graal_ee_java8.native").toFile(),
					VMType.GRAAL_NATIVE_8));

			explorer.addVM(new VMData("GraalVM CE JDK11", vmoeDir.resolve("graal_ce_java11.vm").toFile(), VMType.GRAAL_VM_11));
			explorer.addVM(new VMData("GraalVM EE JDK11", vmoeDir.resolve("graal_ee_java11.vm").toFile(), VMType.GRAAL_VM_11));

			explorer.addVM(new VMData("GraalVM native-image CE JDK11", vmoeDir.resolve("graal_ce_java11.native").toFile(),
					VMType.GRAAL_NATIVE_11));
			explorer.addVM(new VMData("GraalVM native-image EE JDK11", vmoeDir.resolve("graal_ee_java11.native").toFile(),
					VMType.GRAAL_NATIVE_11));
		}

		if (processOpenJ9)
		{
			explorer.addVM(new VMData("OpenJ9", baseDir.resolve("openj9").toFile(), VMType.OPENJ9));
		}

		if (processZing)
		{
			explorer.addVM(new VMData("Zing JDK8", vmoeDir.resolve("zing8.out").toFile(), VMType.ZING));
			explorer.addVM(new VMData("Zing JDK11", vmoeDir.resolve("zing11.out").toFile(), VMType.ZING));
		}

		if (processZulu)
		{
			explorer.addVM(new VMData("Zulu JDK8", vmoeDir.resolve("zulu8.out").toFile(), VMType.ZULU));
			explorer.addVM(new VMData("Zulu JDK11", vmoeDir.resolve("zulu11.out").toFile(), VMType.ZULU));
			explorer.addVM(new VMData("Zulu JDK13", vmoeDir.resolve("zulu13.out").toFile(), VMType.ZULU));
			explorer.addVM(new VMData("Zulu JDK15", vmoeDir.resolve("zulu15.out").toFile(), VMType.ZULU));
			explorer.addVM(new VMData("Zulu JDK16", vmoeDir.resolve("zulu16.out").toFile(), VMType.ZULU));
		}

		if (processSapMachine)
		{
			explorer.addVM(
					new VMData("SapMachine", baseDir.resolve("SapMachine/src/hotspot").toFile(), VMType.SAPMACHINE).addUsageFile(
							post13Usage));
		}

		if (processCorretto)
		{
			explorer.addVM(
					new VMData("Corretto JDK8", baseDir.resolve("corretto-8/src/hotspot").toFile(), VMType.CORRETTO).addUsageFile(
							pre10Usage));

			explorer.addVM(new VMData("Corretto JDK11", baseDir.resolve("corretto-11/src/src/hotspot").toFile(),
					VMType.CORRETTO).addUsageFile(post10Usage));
		}

		explorer.compareVMData("OpenJDK8", "Corretto JDK8");
		explorer.compareVMData("OpenJDK11", "Corretto JDK11");

		explorer.compareVMData("OpenJDK8", "Zulu JDK8");
		explorer.compareVMData("OpenJDK11", "Zulu JDK11");
		explorer.compareVMData("OpenJDK13", "Zulu JDK13");
		explorer.compareVMData("OpenJDK15", "Zulu JDK15");
		explorer.compareVMData("OpenJDK16", "Zulu JDK16");

		explorer.compareVMData("OpenJDK8", "Zing JDK8");
		explorer.compareVMData("OpenJDK11", "Zing JDK11");

		explorer.compareVMData("OpenJDK16", "SapMachine");

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
			explorer.processVMDeltas(VMType.GRAAL_VM_8, "Additional options in GraalVM Enterprise Edition",
					vmoeDir.resolve("templates/template_graal_delta.html"),
					vmoeDir.resolve("html/graalvm_ee_only_jdk8_options.html"));

			explorer.processVMDeltas(VMType.GRAAL_NATIVE_8, "Additional options in Graal Native Enterprise Edition",
					vmoeDir.resolve("templates/template_graal_delta.html"),
					vmoeDir.resolve("html/graalvm_native_image_ee_only_jdk8_options.html"));

			explorer.processVMDeltas(VMType.GRAAL_VM_11, "Additional options in GraalVM Enterprise Edition",
					vmoeDir.resolve("templates/template_graal_delta.html"),
					vmoeDir.resolve("html/graalvm_ee_only_jdk11_options.html"));

			explorer.processVMDeltas(VMType.GRAAL_NATIVE_11, "Additional options in Graal Native Enterprise Edition",
					vmoeDir.resolve("templates/template_graal_delta.html"),
					vmoeDir.resolve("html/graalvm_native_image_ee_only_jdk11_options.html"));
		}

		if (processHotSpotIntrinsics)
		{
			IntrinsicParser intrinsicParser = new IntrinsicParser(serialiseDir, graalVersion);

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk6/hotspot/src/share/vm/classfile/vmSymbols.hpp"), "OpenJDK6");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk7/hotspot/src/share/vm/classfile/vmSymbols.hpp"), "OpenJDK7");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk8/hotspot/src/share/vm/classfile/vmSymbols.hpp"), "OpenJDK8");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk9/hotspot/src/share/vm/classfile/vmSymbols.hpp"), "OpenJDK9");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk10/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK10");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk11/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK11");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk12/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK12");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk13/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK13");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk14/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK14");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk15/src/hotspot/share/classfile/vmSymbols.hpp"), "OpenJDK15");

			intrinsicParser.processIntrinsics(baseDir.resolve("jdk16/src/hotspot/share/classfile/vmIntrinsics.hpp"), "OpenJDK16");
		}
	}
}