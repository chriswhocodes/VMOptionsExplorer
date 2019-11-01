/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDeltaTable implements IDeltaTable
{
	protected List<SwitchInfo> added = new ArrayList<>();
	protected List<SwitchInfo> removed = new ArrayList<>();

	protected VMData earlierVM;
	protected VMData laterVM;

	public AbstractDeltaTable(VMData earlierVM, VMData laterVM)
	{
		this.earlierVM = earlierVM;
		this.laterVM = laterVM;
	}

	public void recordAddition(SwitchInfo switchInfo)
	{
		added.add(switchInfo);
	}

	public void recordRemoval(SwitchInfo switchInfo)
	{
		removed.add(switchInfo);
	}

	public int getAdditionCount()
	{
		return added.size();
	}

	public int getRemovalCount()
	{
		return removed.size();
	}

}
