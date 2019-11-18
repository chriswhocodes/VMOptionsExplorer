/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.commandline;

import com.chrisnewland.vmoe.parser.ISwitchParser;
import org.owasp.encoder.Encode;

import java.util.Objects;

public class KeyValue
{
	private String prefix;
	private String key;
	private String value;

	public KeyValue(String prefix, String key, String value)
	{
		this.prefix = prefix;
		this.key = key;
		this.value = value;

		System.out.println("Creating KeyValue (" + prefix + "," + key + "," + value + ")");
	}

	public String getPrefix()
	{
		return prefix;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public String getKeyWithPrefix()
	{
		return prefix + key;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		KeyValue keyValue = (KeyValue) o;
		return Objects.equals(key, keyValue.key) && Objects.equals(value, keyValue.value);
	}

	@Override public int hashCode()
	{
		return Objects.hash(key, value);
	}

	public String toStringForDTO()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(prefix);

		if ("true".equals(value.toLowerCase()))
		{
			builder.append('+').append(key);
		}
		else if ("false".equals(value.toLowerCase()))
		{
			builder.append('-').append(key);
		}
		else if (ISwitchParser.PREFIX_X.equals(prefix))
		{
			builder.append(key).append(value);
		}
		else
		{
			builder.append(key).append('=').append(value);
		}

		return builder.toString();
	}

	public String toStringForHTML()
	{
		return Encode.forHtml(toStringForDTO());
	}

	@Override public String toString()
	{
		return toStringForHTML();
	}

}