package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.commandline.KeyValue;

import java.util.List;

public interface ISwitchRule
{
	SwitchRuleResult apply(List<KeyValue> keyValueList);
}
