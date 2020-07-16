/*
 * Copyright (c) 2018-2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;

public interface IDeltaTable
{
	void recordAddition(SwitchInfo switchInfo);

	void recordRemoval(SwitchInfo switchInfo);

	int getAdditionCount();

	int getRemovalCount();

	String toJSON();
}
