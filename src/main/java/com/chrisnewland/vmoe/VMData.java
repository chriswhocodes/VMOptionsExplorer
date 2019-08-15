/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

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
		case HOTSPOT:
			builder.append("hotspot_options_").append(jdkName.toLowerCase().replace(" ", "_"));
			break;
		case GRAAL_VM:
			builder.append(jdkName.toLowerCase().replace(" ", "_")).append("_options");
			break;
		case GRAAL_NATIVE:
			builder.append(jdkName.toLowerCase().replace(" ", "_").replace("-", "_")).append("_options");
			break;
		case OPENJ9:
			builder.append("openj9_options");
			break;
		case ZING:
			builder.append("zing_options");
			break;			
		}
		
		builder.append(".html");
		
		return builder.toString();
	}
}