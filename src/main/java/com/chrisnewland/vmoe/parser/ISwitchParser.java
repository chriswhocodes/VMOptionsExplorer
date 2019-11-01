/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe.parser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.chrisnewland.vmoe.SwitchInfo;
import com.chrisnewland.vmoe.SwitchInfoMap;

public interface ISwitchParser
{
    public static final String PREFIX_X = "-X";

    public static final String PREFIX_XX = "-XX:";

    SwitchInfoMap process(File vmPath) throws IOException;
}