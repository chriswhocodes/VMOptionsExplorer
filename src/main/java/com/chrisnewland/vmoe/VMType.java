/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import com.chrisnewland.vmoe.parser.*;

public enum VMType
{
	HOTSPOT, GRAAL_VM_8, GRAAL_VM_11, GRAAL_NATIVE_8, GRAAL_NATIVE_11, MICROSOFT, OPENJ9, ZING, ZULU, SAPMACHINE, CORRETTO;

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
}
