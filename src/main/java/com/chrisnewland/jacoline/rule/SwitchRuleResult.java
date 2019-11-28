package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.SwitchStatus;

public class SwitchRuleResult
{
	public static final String MESSAGE_OK = "OK";

	public SwitchStatus getSwitchStatus()
	{
		return switchStatus;
	}

	private SwitchStatus switchStatus;

	private String message;

	public String getMessage()
	{
		return message;
	}

	public SwitchRuleResult(SwitchStatus switchStatus, String message)
	{
		this.switchStatus = switchStatus;
		this.message = message;
	}

	public static SwitchRuleResult ok()
	{
		return new SwitchRuleResult(SwitchStatus.OK, MESSAGE_OK);
	}
}
