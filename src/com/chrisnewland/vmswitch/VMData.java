/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch;

import java.io.File;

public class VMData
{
    private String jdkName;
    private File vmPath;
    private VMType vmType;

    public String getJdkName()
    {
        return jdkName;
    }

    public File getVmPath()
    {
        return vmPath;
    }

    public VMType getVmType()
    {
        return vmType;
    }

    public VMData(String jdkName, File vmPath, VMType vmType)
    {
        this.jdkName = jdkName;
        this.vmPath = vmPath;
        this.vmType = vmType;
    }
    
	public String getHTMLFilename()
	{
		StringBuilder builder = new StringBuilder();
				
		switch (vmType)
		{
		case GRAAL:
			builder.append("graal_options_");
			break;
		case OPENJ9:
			builder.append("openj9_options_");
			break;
		case HOTSPOT:
			builder.append("hotspot_options_");
			break;
		}
		
		builder.append(jdkName.toLowerCase().replace(" ", "_")).append(".html");
		
		return builder.toString();
	}
}