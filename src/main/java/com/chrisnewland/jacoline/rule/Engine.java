package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;

import java.util.ArrayList;
import java.util.List;

public class Engine
{
	private static final List<ISwitchRule> RULES_LIST;

	static
	{
		RULES_LIST = new ArrayList<>();

		RULES_LIST.add(new RuleXmsNotGreaterThanXmx());
	}

	public static List<SwitchRuleResult> applyRules(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		List<SwitchRuleResult> results = new ArrayList<>();

		for (ISwitchRule rule : RULES_LIST)
		{
			System.out.println("Applying " + rule);

			SwitchRuleResult ruleResult = rule.apply(keyValueList);

			if (!ruleResult.isCompliant())
			{
				results.add(ruleResult);
			}
		}

		return results;
	}
}
