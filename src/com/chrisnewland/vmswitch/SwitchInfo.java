
/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch;

public class SwitchInfo
{
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

	public SwitchInfo(String name)
	{
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

	@Override
	public String toString()
	{
		return "SwitchInfo [name=" + name + ", type=" + type + ", os=" + os + ", cpu=" + cpu + ", component=" + component
				+ ", defaultValue=" + defaultValue + ", availability=" + availability + ", description=" + description
				+ ", comment=" + comment + ", definedIn=" + definedIn + ", since=" + since + ", range=" + range + "]";
	}

	public static String getHeaderRow(VMType vmType)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");
		builder.append("<th>").append("Name").append("</th>");

		if (vmType == VMType.HOTSPOT)
		{
			builder.append("<th>").append("Since").append("</th>");
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

		if (vmType == VMType.HOTSPOT)
		{
			builder.append("<th>").append("Availability").append("</th>");
		}

		builder.append("<th>").append("Description").append("</th>");

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
		}
		
		if (vmType != VMType.OPENJ9)
		{
			builder.append(getRow(type));
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

		if (vmType == VMType.HOTSPOT)
		{
			builder.append(getRow(availability));
		}
		
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

		builder.append(getRow(descriptionComment));

		if (vmType == VMType.HOTSPOT)
		{
			builder.append(getRow(definedIn));
		}

		builder.append("</tr>");

		return builder.toString();
	}

	private String getRow(String value)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<td>").append(value == null ? "" : value).append("</td>");

		return builder.toString();
	}
}
