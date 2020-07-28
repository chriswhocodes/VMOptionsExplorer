/*
 * Copyright (c) 2018-2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import org.json.JSONObject;

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
	private String deprecation;

	public String serialise()
	{
		JSONObject jsonObject = new JSONObject();

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
		jsonObject.put("deprecation", deprecation);

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
		switchInfo.setDeprecation(jsonObject.optString("deprecation", null));

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

	public String getKey()
	{
		return name + "_" + os + "_" + cpu + "_" + component;
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

	public String getDeprecation()
	{
		return deprecation;
	}

	public void setDeprecation(String deprecation)
	{
		this.deprecation = deprecation;
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

	@Override public String toString()
	{
		return "SwitchInfo{" + "prefix='" + prefix + '\'' + ", name='" + name + '\'' + ", type='" + type + '\'' + ", os='" + os
				+ '\'' + ", cpu='" + cpu + '\'' + ", component='" + component + '\'' + ", defaultValue='" + defaultValue + '\''
				+ ", availability='" + availability + '\'' + ", description='" + description + '\'' + ", comment='" + comment + '\''
				+ ", definedIn='" + definedIn + '\'' + ", since='" + since + '\'' + ", range='" + range + '\'' + ", deprecation='"
				+ deprecation + '\'' + '}';
	}

	public static String getHeaderRow(VMType vmType)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");
		builder.append("<th>").append("Name").append("</th>");

		if (vmType == VMType.HOTSPOT)
		{
			builder.append("<th>").append("Since").append("</th>");
			builder.append("<th>").append("Deprecated").append("</th>");
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append("<th>").append("Type").append("</th>");
		}

		if (vmType == VMType.HOTSPOT)
		{
			builder.append("<th>").append("OS").append("</th>");
			builder.append("<th>").append("CPU").append("</th>");
			builder.append("<th>").append("Component").append("</th>");
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append("<th>").append("Default").append("</th>");
		}

		if (vmType == VMType.HOTSPOT || vmType == VMType.GRAAL_NATIVE_8 || vmType == VMType.GRAAL_NATIVE_11)
		{
			builder.append("<th>").append("Availability").append("</th>");
		}

		if (vmType != VMType.ZING && vmType != VMType.ZULU)
		{
			builder.append("<th>").append("Description").append("</th>");
		}

		if (vmType == VMType.HOTSPOT)
		{
			builder.append("<th>").append("Defined in").append("</th>");
		}

		builder.append("</tr>");

		return builder.toString();
	}

	public String toRow(VMType vmType)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");

		builder.append(getRow(name));

		if (vmType == VMType.HOTSPOT)
		{
			builder.append(getRow(since));
			builder.append(getRow(deprecation));
		}

		if (vmType != VMType.OPENJ9)
		{
			builder.append(getRow(escapeHTMLEntities(type)));
		}

		if (vmType == VMType.HOTSPOT)
		{
			builder.append(getRow(os));
			builder.append(getRow(cpu));
			builder.append(getRow(component));
		}

		if (vmType != VMType.OPENJ9)
		{
			if (defaultValue != null)
			{
				builder.append(getRow(defaultValue + ((range == null) ? "" : "<br>" + range)));
			}
			else
			{
				builder.append(getRow(""));
			}
		}

		if (vmType == VMType.HOTSPOT || vmType == VMType.GRAAL_NATIVE_8 || vmType == VMType.GRAAL_NATIVE_11)
		{
			builder.append(getRow(availability));
		}

		if (vmType != VMType.ZING && vmType != VMType.ZULU)
		{
			String descriptionComment = "";

			if (description != null)
			{
				descriptionComment += description;
				if (comment != null)
				{
					descriptionComment += "<br>" + comment;
				}
			}
			else if (comment != null)
			{
				descriptionComment += comment;
			}

			builder.append(getRow(escapeHTMLEntities(descriptionComment)));
		}

		if (vmType == VMType.HOTSPOT)
		{
			builder.append(getRow(definedIn));
		}

		builder.append("</tr>");

		return builder.toString();
	}

	public static String escapeHTMLEntities(String raw)
	{
		if (raw == null)
		{
			return "";
		}

		return raw.toString()
				  .replace("<br>", "SAFE_BR")
				  .replace("<pre>", "SAFE_PRE_OPEN")
				  .replace("</pre>", "SAFE_PRE_CLOSE")
				  .replace("&", "&amp;")
				  .replace("<", "&lt;")
				  .replace(">", "&gt;")
				  .replace("\"", "&quot;")
				  .replace("SAFE_BR", "<br>")
				  .replace("SAFE_PRE_OPEN", "<pre>")
				  .replace("SAFE_PRE_CLOSE", "</pre>");
	}

	private String getRow(String value)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<td>").append(value == null ? "" : value).append("</td>");

		return builder.toString();
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
