/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser.delta;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.VMData;

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

	@Override public void recordAddition(SwitchInfo switchInfo)
	{
		added.add(switchInfo);
	}

	@Override public void recordRemoval(SwitchInfo switchInfo)
	{
		removed.add(switchInfo);
	}

	@Override public int getAdditionCount()
	{
		return added.size();
	}

	@Override public int getRemovalCount()
	{
		return removed.size();
	}

	@Override public String toJSON()
	{
		StringBuilder builder = new StringBuilder();

		builder.append('{');

		builder.append(putJSONKeyValue("earlierVM", earlierVM.getSafeJDKName()));
		builder.append(putJSONKeyValue("laterVM", laterVM.getSafeJDKName()));
		builder.append(putJSONKeyValueList("added", added));
		builder.append(putJSONKeyValueList("removed", removed));

		builder.deleteCharAt(builder.length() - 2).append('}');

		return builder.toString();
	}

	private String putJSONKeyValue(String key, String value)
	{
		StringBuilder builder = new StringBuilder();

		builder.append('"').append(key).append("\": \"").append(value).append("\",\n");

		return builder.toString();
	}

	private String putJSONKeyValueList(String key, List<SwitchInfo> list)
	{
		StringBuilder builder = new StringBuilder();

		builder.append('"').append(key).append("\":").append(listToOrderedJSONString(list)).append(",\n");

		return builder.toString();
	}

	private String listToOrderedJSONString(List<SwitchInfo> list)
	{
		StringBuilder builder = new StringBuilder();

		builder.append('[');

		for (SwitchInfo switchInfo : list)
		{
			//builder.append('{');

			builder.append(switchInfo.serialise());

			builder.append(",\n");
		}

		if (!list.isEmpty())
		{
			builder.deleteCharAt(builder.length() - 2);
		}

		builder.append(']');

		return builder.toString();
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