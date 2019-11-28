package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.jacoline.commandline.SwitchStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleDetectDuplicatesAfterSwitch extends AbstractSwitchRule
{
	private int currentSwitchIndex;

	public RuleDetectDuplicatesAfterSwitch(int currentSwitchIndex)
	{
		this.currentSwitchIndex = currentSwitchIndex;
	}

	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		SwitchRuleResult result = SwitchRuleResult.ok();

		if (currentSwitchIndex < keyValueList.size() - 1)
		{
			for (int laterIndex = currentSwitchIndex + 1; laterIndex < keyValueList.size(); laterIndex++)
			{
				KeyValue laterKeyValue = keyValueList.get(laterIndex);

				if (keyValue.getKeyWithPrefix().equals(laterKeyValue.getKeyWithPrefix()))
				{
					result = new SwitchRuleResult(SwitchStatus.WARNING,"Duplicate switch. This is overridden by " + laterKeyValue.toStringForHTML());
				}
			}
		}

		return result;
	}
}