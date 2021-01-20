/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.OrderedJSONObjectFactory;
import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		JSONObject jsonObject = OrderedJSONObjectFactory.getJSONObject();

		jsonObject.put("earlierVM", earlierVM.getSafeJDKName());
		jsonObject.put("laterVM", laterVM.getSafeJDKName());
		jsonObject.put("added", added);
		jsonObject.put("removed", removed);

		return jsonObject.toString(4);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractDeltaTable that = (AbstractDeltaTable) o;
		return Objects.equals(added, that.added) && Objects.equals(removed, that.removed) && Objects.equals(earlierVM,
				that.earlierVM) && Objects.equals(laterVM, that.laterVM);
	}

	@Override public int hashCode()
	{
		return Objects.hash(added, removed, earlierVM, laterVM);
	}
}