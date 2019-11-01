/*
 * Copyright (c) 2018-2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/VMOptionsExplorer/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider public class ResponseFilter implements ContainerResponseFilter
{
	private static final String HTML_ENTITY_NON_BREAKING_HYPHEN = "&#8209;";

	@Override public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext)
			throws IOException
	{
		String response = (String) containerResponseContext.getEntity();

		if (response != null)
		{
			response = response.replace("-", HTML_ENTITY_NON_BREAKING_HYPHEN);

			containerResponseContext.setEntity(response);
		}
	}
}