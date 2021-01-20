/*
 * Copyright (c) 2018-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.vmoe;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class OrderedJSONObjectFactory
{
	// Thanks to https://towardsdatascience.com/create-an-ordered-jsonobject-in-java-fb9629247d76
	public static JSONObject getJSONObject()
	{
		JSONObject jsonObject = new JSONObject();
//		try
//		{
//			Field changeMap = jsonObject.getClass().getDeclaredField("map");
//			changeMap.setAccessible(true);
//			changeMap.set(jsonObject, new LinkedHashMap<>());
//			changeMap.setAccessible(false);
//		}
//		catch (IllegalAccessException | NoSuchFieldException e)
//		{
//			e.printStackTrace();
//		}

		return jsonObject;
	}
}
