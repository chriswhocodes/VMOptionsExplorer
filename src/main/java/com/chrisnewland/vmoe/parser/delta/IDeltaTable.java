package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;

public interface IDeltaTable
{
	void recordAddition(SwitchInfo switchInfo);

	void recordRemoval(SwitchInfo switchInfo);

	int getAdditionCount();

	int getRemovalCount();
}
