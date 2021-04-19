/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import java.util.Objects;

public class SwitchInfo implements Comparable<SwitchInfo>
{
	public static final String PREFIX_X = "-X";
	public static final String PREFIX_XX = "-XX:";

	private String prefix;
	private String name;
	private String type; // intx, bool, uintx, ccstr, ccstrlist, double,
	// uint64_t
	private String os; // aix, bsd, linux, solaris, windows
	private String cpu; // x86, zero, ppc, sparc
	private String component; // c1, c2, runtime, gc
	private String defaultValue;
	private String availability; // product, diagnostic, experimental, debug
	private String description;
	private String comment;
	private String definedIn;
	private String since;
	private String range;

	private String deprecated;
	private String obsoleted;
	private String expired;
	private String macro;

	public String serialise()
	{
		JSONObject jsonObject = OrderedJSONObjectFactory.getJSONObject();

		jsonObject.put("prefix", prefix);
		jsonObject.put("name", name);
		jsonObject.put("type", type);
		jsonObject.put("os", os);
		jsonObject.put("cpu", cpu);
		jsonObject.put("component", component);
		jsonObject.put("defaultValue", defaultValue);
		jsonObject.put("availability", availability);
		jsonObject.put("description", description);
		jsonObject.put("comment", comment);
		jsonObject.put("definedIn", definedIn);
		jsonObject.put("since", since);
		jsonObject.put("range", range);
		jsonObject.put("deprecated", deprecated);
		jsonObject.put("obsoleted", obsoleted);
		jsonObject.put("expired", expired);
		jsonObject.put("macro", macro);

		return jsonObject.toString();
	}

	public static SwitchInfo deserialise(JSONObject jsonObject)
	{
		String prefix = jsonObject.getString("prefix");

		String name = jsonObject.getString("name");

		SwitchInfo switchInfo = new SwitchInfo(prefix, name);

		switchInfo.setType(jsonObject.optString("type", null));
		switchInfo.setOs(jsonObject.optString("os", null));
		switchInfo.setCpu(jsonObject.optString("cpu", null));
		switchInfo.setComponent(jsonObject.optString("component", null));
		switchInfo.setDefaultValue(jsonObject.optString("defaultValue", null));
		switchInfo.setAvailability(jsonObject.optString("availability", null));
		switchInfo.setDescription(jsonObject.optString("description", null));
		switchInfo.setComment(jsonObject.optString("comment", null));
		switchInfo.setDefinedIn(jsonObject.optString("definedIn", null));
		switchInfo.setSince(jsonObject.optString("since", null));
		switchInfo.setRange(jsonObject.optString("range", null));
		switchInfo.setDeprecated(jsonObject.optString("deprecated", null));
		switchInfo.setObsoleted(jsonObject.optString("obsoleted", null));
		switchInfo.setExpired(jsonObject.optString("expired", null));
		switchInfo.setMacro(jsonObject.optString("macro", null));

		return switchInfo;
	}

	public String getRange()
	{
		return range;
	}

	public void setRange(String range)
	{
		this.range = range;
	}

	public String getSince()
	{
		return since;
	}

	public void setSince(String since)
	{
		this.since = since;
	}

	public SwitchInfo(String prefix, String name)
	{
		this.prefix = prefix;

		this.name = name;
	}

	@JSONPropertyIgnore public String getKey()
	{
		return name + "_" + os + "_" + cpu + "_" + component + (macro != null ? ("_" + macro) : "");
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public String getCpu()
	{
		return cpu;
	}

	public void setCpu(String cpu)
	{
		this.cpu = cpu;
	}

	public String getComponent()
	{
		return component;
	}

	public void setComponent(String component)
	{
		this.component = component;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String getAvailability()
	{
		return availability;
	}

	public void setAvailability(String availability)
	{
		this.availability = availability;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDeprecated()
	{
		return deprecated;
	}

	public void setDeprecated(String deprecated)
	{
		this.deprecated = deprecated;
	}

	public String getObsoleted()
	{
		return obsoleted;
	}

	public void setObsoleted(String obsoleted)
	{
		this.obsoleted = obsoleted;
	}

	public String getExpired()
	{
		return expired;
	}

	public void setExpired(String expired)
	{
		this.expired = expired;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getDefinedIn()
	{
		return definedIn;
	}

	public void setDefinedIn(String definedIn)
	{
		this.definedIn = definedIn;
	}

	public String getMacro()
	{
		return macro;
	}

	public void setMacro(String macro)
	{
		this.macro = macro;
	}

	@Override public String toString()
	{
		return "SwitchInfo{" + "prefix='" + prefix + '\'' + ", name='" + name + '\'' + ", type='" + type + '\'' + ", os='" + os
				+ '\'' + ", cpu='" + cpu + '\'' + ", component='" + component + '\'' + ", defaultValue='" + defaultValue + '\''
				+ ", availability='" + availability + '\'' + ", description='" + description + '\'' + ", comment='" + comment + '\''
				+ ", definedIn='" + definedIn + '\'' + ", since='" + since + '\'' + ", range='" + range + '\'' + ", deprecated='"
				+ deprecated + '\'' + ", obsoleted='" + obsoleted + '\'' + ", expired='" + expired + '\'' + ", macro='" + macro
				+ '\'' + '}';
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SwitchInfo that = (SwitchInfo) o;
		return Objects.equals(name, that.name);
	}

	@Override public int hashCode()
	{
		return name.hashCode();
	}

	@Override public int compareTo(SwitchInfo o)
	{
		return name.compareTo(o.name);
	}
}
