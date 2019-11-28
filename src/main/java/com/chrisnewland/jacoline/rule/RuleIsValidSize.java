package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;
import com.chrisnewland.jacoline.commandline.SwitchStatus;
import com.chrisnewland.vmoe.parser.ISwitchParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleIsValidSize extends AbstractSwitchRule
{
	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		SwitchRuleResult result;

		if (isValidSize(keyValue.getValue()))
		{
			result = SwitchRuleResult.ok();
		}
		else
		{
			result = new SwitchRuleResult(SwitchStatus.ERROR,
					"Bad value for type '<size>'. Must be a number with an optional suffix of 'k', 'm', 'g', or 't'.");
		}

		return result;
	}

	private boolean isValidSize(String value)
	{
		boolean result = false;

		Pattern patternSize = Pattern.compile("^([0-9]+)(.*)");

		Matcher matcher = patternSize.matcher(value);

		if (matcher.find())
		{
			if (matcher.groupCount() == 2)
			{
				String sizeSuffix = matcher.group(2).trim();

				if (sizeSuffix.isEmpty())
				{
					result = true;
				}
				else if (sizeSuffix.length() == 1)
				{
					char suffixChar = sizeSuffix.toLowerCase().charAt(0);

					switch (suffixChar)
					{
					case 'k':
					case 'm':
					case 'g':
					case 't':
						result = true;
					}
				}
			}
		}

		return result;
	}
}