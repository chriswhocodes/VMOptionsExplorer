/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import com.chrisnewland.vmoe.parser.*;

public enum VMType
{
	DRAGONWELL("Dragonwell"), HOTSPOT("HotSpot"), GRAAL_VM("GraalVM"), GRAAL_NATIVE("Graal Native"), MICROSOFT("Microsoft"), OPENJ9("OpenJ9"), ZING("Zing"), ZULU(
		"Zulu"), SAPMACHINE("SapMachine"), CORRETTO("Corretto");

	public ISwitchParser getParser()
	{
		switch (this)
		{
		case GRAAL_VM:
			return new GraalVMSwitchParser();
		case GRAAL_NATIVE:
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