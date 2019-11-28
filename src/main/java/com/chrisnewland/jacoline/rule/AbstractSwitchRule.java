package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSwitchRule implements ISwitchRule
{
	private static final long SIZE_KILOBYTE = 1024L;

	private static final long SIZE_MEGABYTE = SIZE_KILOBYTE * 1024L;

	private static final long SIZE_GIGABYTE = SIZE_MEGABYTE * 1024L;

	private static final long SIZE_TERABYTE = SIZE_GIGABYTE * 1024L;

	protected KeyValue getLastOccurrence(String keyName, List<KeyValue> keyValueList)
	{
		KeyValue result = null;

		for (KeyValue keyValue : keyValueList)
		{
			if (keyName.equals(keyValue.getKeyWithPrefix()))
			{
				result = keyValue;
			}
		}

		return result;
	}

	protected long parseSize(String value)
	{
		long result = -1;

		Pattern patternSize = Pattern.compile("^([0-9]+)(.*)");

		Matcher matcher = patternSize.matcher(value);

		if (matcher.find())
		{
			result = Long.parseLong(matcher.group(1));

			if (matcher.groupCount() == 2)
			{
				String sizeSuffix = matcher.group(2).trim();

				if (sizeSuffix.length() == 1)
				{
					char suffixChar = sizeSuffix.toLowerCase().charAt(0);

					switch (suffixChar)
					{
					case 'k':
						result *= SIZE_KILOBYTE;
						break;
					case 'm':
						result *= SIZE_MEGABYTE;
						break;
					case 'g':
						result *= SIZE_GIGABYTE;
						break;
					case 't':
						result *= SIZE_TERABYTE;
						break;
					}
				}
			}
		}

		return result;
	}
}