package com.chrisnewland.jacoline.rule;

public class SwitchRuleResult
{
	public static final String MESSAGE_OK = "OK";

	private boolean compliant;

	private String message;

	public boolean isCompliant()
	{
		return compliant;
	}

	public String getMessage()
	{
		return message;
	}

	public SwitchRuleResult(boolean compliant, String message)
	{
		this.compliant = compliant;
		this.message = message;
	}

	public static SwitchRuleResult ok()
	{
		return new SwitchRuleResult(true, MESSAGE_OK);
	}
}
