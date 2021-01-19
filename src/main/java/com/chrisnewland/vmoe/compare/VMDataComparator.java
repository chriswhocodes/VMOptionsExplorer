package com.chrisnewland.vmoe.compare;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;
import com.chrisnewland.vmoe.VMData;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class VMDataComparator
{
	public static StringBuilder[] compareVMData(VMData vanilla, VMData flavour)
	{
		StringBuilder builderAdd = new StringBuilder();
		StringBuilder builderChange = new StringBuilder();
		StringBuilder builderRemove = new StringBuilder();

		try
		{
			SwitchInfoMap switchMapVanilla = vanilla.getVmType().getParser().process(vanilla.getVmPath());

			SwitchInfoMap switchMapFlavour = flavour.getVmType().getParser().process(flavour.getVmPath());

			Set<SwitchInfo> inVanilla = new TreeSet<>(switchMapVanilla.values());

			Set<SwitchInfo> inFlavour = new TreeSet<>(switchMapFlavour.values());

			for (SwitchInfo switchInfoVanilla : inVanilla)
			{
				if ("develop".equals(switchInfoVanilla.getAvailability()) || "notproduct".equals(
						switchInfoVanilla.getAvailability()))
				{
					continue;
				}

				if (!inFlavour.contains(switchInfoVanilla))
				{
					builderRemove.append("<div>")
								 .append("<a href=\"")
								 .append(vanilla.getHTMLFilename())
								 .append("?s=")
								 .append(switchInfoVanilla.getName())
								 .append("\">")
								 .append(switchInfoVanilla.getName())
								 .append("</a>")
								 .append("</div> ");
				}
				else
				{
					String valueVanilla = switchInfoVanilla.getDefaultValue();

					SwitchInfo switchInfoFlavour = switchMapFlavour.get(switchInfoVanilla.getKey());

					if (switchInfoFlavour != null)
					{
						String valueFlavour = switchInfoFlavour.getDefaultValue();

						if (valueVanilla != null && valueFlavour != null)
						{
							if (!valueVanilla.equals(valueFlavour))
							{
								builderChange.append("<tr>")
											 .append("<td>")
											 .append(switchInfoVanilla.getName())
											 .append("</td>")
											 .append("<td>")
											 .append(valueVanilla)
											 .append("</td>")
											 .append("<td>")
											 .append(valueFlavour)
											 .append("</td>")
											 .append("</tr>");
							}
						}
					}
				}
			}

			for (SwitchInfo switchInfo : inFlavour)
			{
				if (!inVanilla.contains(switchInfo))
				{
					builderAdd.append("<div>")
							  .append("<a href=\"")
							  .append(flavour.getHTMLFilename())
							  .append("?s=")
							  .append(switchInfo.getName())
							  .append("\">")
							  .append(switchInfo.getName())
							  .append("</a>")
							  .append("</div> ");
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		if (builderChange.length() > 0)
		{
			builderChange.insert(0, "<table><tr><th>Name</th><th>" + vanilla.getJdkName() + " value</th><th>" + flavour.getJdkName()
					+ " value</th></tr>");
			builderChange.append("</table>");
		}

		return new StringBuilder[] { builderAdd, builderChange, builderRemove };
	}
}