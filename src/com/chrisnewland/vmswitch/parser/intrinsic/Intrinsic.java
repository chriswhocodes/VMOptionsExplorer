/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch.parser.intrinsic;

public class Intrinsic
{
	private String id;
	private String klass;
	private String name;
	private String signature;
	private String flags;

	public String getId()
	{
		return id;
	}

	public String getKlass()
	{
		return klass;
	}

	public String getName()
	{
		return name;
	}

	public String getSignature()
	{
		return signature;
	}

	public String getFlags()
	{
		return flags;
	}

	public Intrinsic(String id, String klass, String name, String signature, String flags)
	{
		super();
		this.id = id;
		this.klass = klass;
		this.name = name;
		this.signature = signature;
		this.flags = flags;
	}

	@Override
	public String toString()
	{
		return "Intrinsic [id=" + id + ", klass=" + klass + ", name=" + name + ", signature=" + signature + ", flags=" + flags
				+ "]";
	}

	public static String getHeaderRow()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");
		builder.append("<th>").append("Id").append("</th>");
		builder.append("<th>").append("Class").append("</th>");
		builder.append("<th>").append("Name").append("</th>");
		builder.append("<th>").append("Signature").append("</th>");
		builder.append("<th>").append("Flags").append("</th>");
		builder.append("</tr>");

		return builder.toString();
	}

	public String toRow()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<tr>");
		builder.append("<td>").append(id).append("</td>");
		builder.append("<td>").append(klass).append("</td>");
		builder.append("<td>").append(name).append("</td>");
		builder.append("<td>").append(signature).append("</td>");
		builder.append("<td>").append(flags).append("</td>");
		builder.append("</tr>");

		return builder.toString();
	}
}