/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMSwitch/blob/master/LICENSE
 */
package com.chrisnewland.vmswitch.parser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.chrisnewland.vmswitch.SwitchInfo;

public interface ISwitchParser
{
    Map<String, SwitchInfo> process(File vmPath) throws IOException;
}