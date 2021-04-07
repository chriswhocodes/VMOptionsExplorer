/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import com.chrisnewland.vmoe.parser.*;

public enum VMType
{
	DRAGONWELL("Dragonwell"), HOTSPOT("HotSpot"), GRAAL_VM_8("GraalVM"), GRAAL_VM_11("GraalVM"), GRAAL_NATIVE_8(
		"Graal Native"), GRAAL_NATIVE_11("Graal Native"), MICROSOFT("Microsoft"), OPENJ9("OpenJ9"), ZING("Zing"), ZULU(
		"Zulu"), SAPMACHINE("SapMachine"), CORRETTO("Corretto");

	public ISwitchParser getParser()
	{
		switch (this)
		{
		case GRAAL_VM_8:
		case GRAAL_VM_11:
			return new GraalVMSwitchParser();
		case GRAAL_NATIVE_8:
		case GRAAL_NATIVE_11:
			return new GraalNativeImageSwitchParser();
		case OPENJ9:
			return new OpenJ9SwitchParser();
		case CORRETTO:
		case DRAGONWELL:
		case HOTSPOT:
		case MICROSOFT:
		case SAPMACHINE:
			return new HotSpotSwitchParser();
		case ZING:
		case ZULU:
			return new ZingSwitchParser();
		default:
			throw new RuntimeException("Unexpected VM Type: " + this);
		}
	}

	public boolean isHotSpotBased()
	{
		switch (this)
		{
		case CORRETTO:
		case DRAGONWELL:
		case HOTSPOT:
		case MICROSOFT:
		case SAPMACHINE:
			return true;
		default:
			return false;
		}
	}

	private String displayName;

	private VMType(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}
}
