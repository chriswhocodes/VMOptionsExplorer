/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import java.io.File;

public class VMData
{
	private String jdkName;
	private File vmPath;
	private VMType vmType;

	private File usageFile;

	public String getJdkName()
	{
		return jdkName;
	}

	public File getVmPath()
	{
		return vmPath;
	}

	public VMType getVmType()
	{
		return vmType;
	}

	public VMData(String jdkName, File vmPath, VMType vmType)
	{
		this.jdkName = jdkName;
		this.vmPath = vmPath;
		this.vmType = vmType;
	}

	public String getHTMLFilename()
	{
		StringBuilder builder = new StringBuilder();

		switch (vmType)
		{
		case HOTSPOT:
			builder.append("hotspot_options_").append(getSafeJDKName().toLowerCase());
			break;
		case GRAAL_VM_8:
		case GRAAL_VM_11:
		case GRAAL_NATIVE_8:
		case GRAAL_NATIVE_11:
		case CORRETTO:
		case MICROSOFT:
		case DRAGONWELL:
			builder.append(getSafeJDKName().toLowerCase()).append("_options");
			break;
		case OPENJ9:
			builder.append("openj9_options");
			break;
		case SAPMACHINE:
			builder.append("sapmachine_options");
			break;
		case ZING:
		case ZULU:
			builder.append(getSafeJDKName().toLowerCase()).append("_options");
			break;
		}

		builder.append(".html");

		return builder.toString();
	}

	public File getUsageFile()
	{
		return usageFile;
	}

	public VMData addUsageFile(String usageFileLocation)
	{
		this.usageFile = new File(vmPath, usageFileLocation);

		if (!usageFile.exists() || !usageFile.isFile())
		{
			throw new RuntimeException("Bad Xusage file: " + usageFile);
		}

		return this;
	}

	public String getSafeJDKName()
	{
		return jdkName.replace(" ", "_").replace("-", "_");
	}
}
