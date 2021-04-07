/*
 * Copyright (c) 2018-2021 Chris Newland.
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
import com.chrisnewland.vmoe.html.HTMLUtil;
import com.chrisnewland.vmoe.parser.*;
import com.chrisnewland.vmoe.parser.delta.GraalDeltaTable;
import com.chrisnewland.vmoe.parser.delta.HotSpotDeltaTable;
import com.chrisnewland.vmoe.parser.delta.IDeltaTable;
import com.chrisnewland.vmoe.parser.deprecated.DeprecatedParser;
import com.chrisnewland.vmoe.parser.intrinsic.IntrinsicParser;

public class VMOptionsExplorer
{
	private Path serialiseDir;

	private Map<String, VMData> vmDataMap = new LinkedHashMap<>();

	private Map<String, String> switchNameVersions = new HashMap<>();

	private Path vmoeDir;

	private Serialiser serialiser;

	public VMOptionsExplorer(Path vmoeDir)
	{
		this.vmoeDir = vmoeDir;
	}

	public void addVM(VMData data)
	{
		vmDataMap.put(data.getJdkName(), data);
	}

	public void process(Path serialiseDir, Serialiser serialiser) throws Exception
	{
		this.serialiseDir = serialiseDir;

		this.serialiser = serialiser;

		prepareSerialisationDir();

		for (VMData vmData : vmDataMap.values())
		{
			parseJDK(vmData);
		}
	}

	private void prepareSerialisationDir()
	{
		File rootDir = serialiseDir.toFile();

		if (!rootDir.exists())
		{
			rootDir.mkdirs();
			new File(rootDir, "options").mkdir();
			new File(rootDir, "diffs").mkdir();
			new File(rootDir, "intrinsics").mkdir();
		}

		if (!rootDir.exists() && rootDir.isDirectory())
		{
			throw new RuntimeException("Could not create serialisation dirs: " + serialiseDir);
		}
	}

	public void processVMDeltas(VMType vmType, String title, Path templatePath, Path outputFile) throws Exception
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

	private void addChangesBetweenVMs(VMData earlier, VMData later, StringBuilder builder) throws Exception
	{
		System.out.println("Calculating differences between " + earlier.getJdkName() + " and " + later.getJdkName());

		SwitchInfoMap switchMapEarlier = earlier.getVmType().getParser().process(earlier.getVmPath());

		SwitchInfoMap switchMapLater = later.getVmType().getParser().process(later.getVmPath());

		// TODO SwitchInfo equals and hashCode rely on name only

		Set<SwitchInfo> inEarlier = new TreeSet<>(switchMapEarlier.values());

		Set<SwitchInfo> inLater = new TreeSet<>(switchMapLater.values());

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

		Path serialisationPath = Paths.get(serialiseDir.resolve(Paths.get("diffs")).toString(),
				later.getSafeJDKName() + "_diffs.json");

		serialiser.serialiseDiffs(serialisationPath, deltaTable);
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

	private String graalVersion;

	public void setGraalVersion(String version)
	{
		this.graalVersion = version;
	}

	private void parseJDK(VMData vmData) throws Exception
	{
		File vmPath = vmData.getVmPath();

		String jdkName = vmData.getJdkName();

		VMType vmType = vmData.getVmType();

		String vmName = vmData.getVmType().getDisplayName();

		ISwitchParser switchParser = vmData.getVmType().getParser();

		SwitchInfoMap switchInfoMap = switchParser.process(vmPath);

		File usageFile = vmData.getUsageFile();

		if (usageFile != null)
		{
			SwitchInfoMap mapXUsage = new XUsageParser().process(usageFile);

			switchInfoMap.putAll(mapXUsage);
		}

		Path serialisationPath = Paths.get(serialiseDir.resolve("options").toString(), vmData.getSafeJDKName() + ".json");

		serialiser.serialiseSwitchInfo(serialisationPath, switchInfoMap.values());

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

			htmlBuilder.append(HTMLUtil.renderSwitchInfoRow(vmType, info)).append("\n");

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
		template = template.replace("$THEAD", HTMLUtil.getHeaderRow(vmType));
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
		case MICROSOFT:
		case SAPMACHINE:
		case CORRETTO:
		case DRAGONWELL:
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
		if (args.length != 3)
		{
			System.err.println("VMOptionsExplorer <jdk base dir> <VMOptionsExplorer base dir> <JSON output dir>");
			System.exit(-1);
		}

		Path baseDir = Paths.get(args[0]);

		Path vmoeDir = Paths.get(args[1]);

		Path jsonOutputDir = Paths.get(args[2]);

		boolean processHotSpot = true;
		boolean processHotSpotDeprecated = true;
		boolean processHotSpotIntrinsics = true;
		boolean processGraalVM = true;
		boolean processOpenJ9 = true;
		boolean processZing = true;
		boolean processZulu = true;
		boolean processSapMachine = true;
		boolean processCorretto = true;
		boolean processMicrosoft = true;
		boolean processDragonwell = true;

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
			DeprecatedParser.parseFile(baseDir.resolve("jdk17"));
		}

		String graalVersion = "21.0.0";

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

			explorer.addVM(new VMData("OpenJDK17", baseDir.resolve("jdk17/src/hotspot").toFile(), VMType.HOTSPOT).addUsageFile(
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

			explorer.addVM(
					new VMData("Corretto JDK11", baseDir.resolve("corretto-11/src/hotspot").toFile(), VMType.CORRETTO).addUsageFile(
							post13Usage));
		}

		if (processMicrosoft)
		{
			explorer.addVM(
					new VMData("Microsoft JDK11", baseDir.resolve("microsoft-11/src/hotspot").toFile(), VMType.MICROSOFT).addUsageFile(
							post10Usage));
		}

		if (processDragonwell)
		{
			explorer.addVM(
					new VMData("Dragonwell JDK8", baseDir.resolve("dragonwell8/hotspot").toFile(), VMType.DRAGONWELL).addUsageFile(
							pre10Usage));

			explorer.addVM(
					new VMData("Dragonwell JDK11", baseDir.resolve("dragonwell11/src/hotspot").toFile(), VMType.DRAGONWELL).addUsageFile(
							post10Usage));
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

		explorer.compareVMData("OpenJDK11", "Microsoft JDK11");

		explorer.compareVMData("OpenJDK8", "Dragonwell JDK8");
		explorer.compareVMData("OpenJDK11", "Dragonwell JDK11");

		Serialiser serialiser = new Serialiser();

		explorer.process(jsonOutputDir, serialiser);

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
			IntrinsicParser intrinsicParser = new IntrinsicParser(jsonOutputDir.resolve(Paths.get("intrinsics")), serialiser,
					graalVersion);

			String pre10vmSymbols = "hotspot/src/share/vm/classfile/vmSymbols.hpp";
			String post10vmSymbols = "src/hotspot/share/classfile/vmSymbols.hpp";
			String post16vmIntrinsics = "src/hotspot/share/classfile/vmIntrinsics.hpp";

			intrinsicParser.processIntrinsics("OpenJDK6", baseDir.resolve("jdk6/" + pre10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK7", baseDir.resolve("jdk7/" + pre10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK8", baseDir.resolve("jdk8/" + pre10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK9", baseDir.resolve("jdk9/" + pre10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK10", baseDir.resolve("jdk10/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK11", baseDir.resolve("jdk11/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK12", baseDir.resolve("jdk12/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK13", baseDir.resolve("jdk13/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK14", baseDir.resolve("jdk14/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK15", baseDir.resolve("jdk15/" + post10vmSymbols));
			intrinsicParser.processIntrinsics("OpenJDK16", baseDir.resolve("jdk16/" + post10vmSymbols),
					baseDir.resolve("jdk16/" + post16vmIntrinsics));
			intrinsicParser.processIntrinsics("OpenJDK17", baseDir.resolve("jdk17/" + post10vmSymbols),
					baseDir.resolve("jdk17/" + post16vmIntrinsics));
		}

		serialiser.saveHashes(jsonOutputDir);
	}
}
