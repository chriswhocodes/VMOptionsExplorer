/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.commandline;

public enum SwitchStatus
{
	OK, WARNING, ERROR;

	public String getCssClass()
	{
		return "status_" + this.toString().toLowerCase();
	}
}