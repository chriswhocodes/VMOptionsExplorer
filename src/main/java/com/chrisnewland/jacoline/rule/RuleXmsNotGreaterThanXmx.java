package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.vmoe.parser.ISwitchParser;

import java.util.List;

public class RuleXmsNotGreaterThanXmx extends AbstractSwitchRule
{
	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		String keyXms = ISwitchParser.PREFIX_X + "ms";

		String keyXmx = ISwitchParser.PREFIX_X + "mx";

		SwitchRuleResult result = SwitchRuleResult.ok();

		if (keyXms.equals(keyValue.getKeyWithPrefix()) || keyXmx.equals(keyValue.getKeyWithPrefix()))
		{
			KeyValue keyValueXms = getLastOccurrence(ISwitchParser.PREFIX_X + "ms", keyValueList);

			KeyValue keyValueXmx = getLastOccurrence(ISwitchParser.PREFIX_X + "mx", keyValueList);

			System.out.println("KVms:" + keyValueXms);

			System.out.println("KVmx:" + keyValueXmx);

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
			}
		}
		
		return result;
	}
}
