/*
 * Copyright (c) 2018-2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;
import org.json.JSONObject;

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

	@Override
	public void recordAddition(SwitchInfo switchInfo)
	{
		added.add(switchInfo);
	}

	@Override
	public void recordRemoval(SwitchInfo switchInfo)
	{
		removed.add(switchInfo);
	}

	@Override
	public int getAdditionCount()
	{
		return added.size();
	}

	@Override
	public int getRemovalCount()
	{
		return removed.size();
	}

	@Override
	public String toJSON()
	{
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("earlierVM", earlierVM.getSafeJDKName());
		jsonObject.put("laterVM", laterVM.getSafeJDKName());
		jsonObject.put("added", added);
		jsonObject.put("removed", removed);

		return jsonObject.toString(4);
	}
}