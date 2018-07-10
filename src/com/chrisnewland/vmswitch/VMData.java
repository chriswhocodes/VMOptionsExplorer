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
}