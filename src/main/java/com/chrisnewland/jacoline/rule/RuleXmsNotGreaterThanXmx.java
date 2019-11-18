package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.vmoe.parser.ISwitchParser;

import java.util.List;

public class RuleXmsNotGreaterThanXmx extends AbstractSwitchRule
{
	@Override public SwitchRuleResult apply(List<KeyValue> keyValueList)
	{
		KeyValue keyValueXms = getLastOccurrence(ISwitchParser.PREFIX_X + "ms", keyValueList);

		KeyValue keyValueXmx = getLastOccurrence(ISwitchParser.PREFIX_X + "mx", keyValueList);

		System.out.println("KVms:" + keyValueXms);

		System.out.println("KVmx:" + keyValueXmx);

		SwitchRuleResult result = null;

		if (keyValueXms != null && keyValueXmx != null)
		{
			long ms = parseSize(keyValueXms.getValue());
			long mx = parseSize(keyValueXmx.getValue());

			System.out.println("ms:" + ms);

			System.out.println("mx:" + mx);

			if (ms > mx)
			{
				result = new SwitchRuleResult(false, "Xmx must be >= Xms");
			}
			else
			{
				result = SwitchRuleResult.ok();
			}
		}
		else
		{
			result = SwitchRuleResult.ok();
		}

		return result;
	}
}
