/*
 * Copyright 2022 Dynamic Discovery Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * $Id: ActionUtils.java 467 2022-11-07 15:42:01Z steveh $
 */

package com.jiglu.mstaglib;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import com.opensymphony.xwork2.interceptor.ValidationAware;
import com.opensymphony.xwork2.ognl.OgnlValueStack;

/**
 * Utilities for tags working with Struts 2 actions.
 */
public final class ActionUtils
{
	/**
	 * Pattern to limit OGNL access to property retrieval. This must match the
	 * Struts params interceptor acceptParamNames parameter.
	 */
	private static final Pattern ACCEPTED_PATTERN = Pattern.compile(
		"\\w+((\\.\\w+)|(\\[\\d+])|(\\(\\d+\\))|(\\['(\\w-?|[\\u4e00-\\u9fa5]-?)+'])|(\\('(\\w-?|[\\u4e00-\\u9fa5]-?)+'\\)))*");

	/** Private default constructor to prevent external instantiation. */
	private ActionUtils()
	{
	}

	/**
	 * Escapes characters that need entities in XML: &lt;, &gt;, &amp; and
	 * &quot;.
	 * @param input a plain text string to escape.
	 * @return the escaped string.
	 */
	public static String escapeEntities(String input)
	{
		StringBuilder buffer;
		String entity;
		int c, pos;

		buffer = null;

		for (int i = 0, l = input.length(); i < l; i++)
		{
			entity = null;
			c = input.charAt(i);
			pos = i;

			switch (c)
			{
				case '<':
					entity = "&lt;";
					break;

				case '>':
					entity = "&gt;";
					break;

				case '&':
					entity = "&amp;";
					break;

				case '"':
					entity = "&quot;";
					break;

				default:
					break;
			}

			if (buffer == null)
			{
				if (entity != null)
				{
					// An entity occurred, so we'll have to use StringBuilder so allocate room for it plus a few more entities
					buffer = new StringBuilder(input.length() + 20);

					// Copy previous skipped characters and fall through to pickup current character
					buffer.append(input.substring(0, pos)).append(entity);
				}
			}
			else
			{
				if (entity == null)
				{
					buffer.append((char)c);
				}
				else
				{
					buffer.append(entity);
				}
			}
		}

		return (buffer == null) ? input : buffer.toString();
	}

	/**
	 * Gets the value of a specified property from the action bean.
	 * @param valueStack the value stack.
	 * @param name the name of the property to retrieve.
	 * @return the value from the action bean matching the name.
	 * @throws JspTagException if an error occurred.
	 */
	public static Object getActionValue(OgnlValueStack valueStack, String name) throws JspTagException
	{
		if (!ACCEPTED_PATTERN.matcher(name).matches())
		{
			throw new JspTagException("Invalid name for a property: " + name);
		}

		try
		{
			return valueStack.findValue(name, true);
		}
		catch (Exception e)
		{
			throw new JspTagException("Unable to access the specified property: " + name, e);
		}
	}

	/**
	 * Gets the value of a specified property from the action bean as a string.
	 * If the value is null then an empty string is returned.
	 * @param valueStack the value stack.
	 * @param name the name of the property to retrieve.
	 * @return the value from the action bean matching the name.
	 * @throws JspTagException if an error occurred.
	 */
	public static String getActionValueString(OgnlValueStack valueStack, String name) throws JspTagException
	{
		Object value = getActionValue(valueStack, name);

		return (value != null) ? value.toString() : "";
	}

	/**
	 * Gets the field errors.
	 * @param valueStack the value stack.
	 * @return the field errors.
	 * @throws JspTagException if an error occurred.
	 */
	public static Map<String, List<String>> getFieldErrors(OgnlValueStack valueStack) throws JspTagException
	{
		try
		{
			Object action = valueStack.peek();

			return (action instanceof ValidationAware) ? ((ValidationAware)action).getFieldErrors() : null;
		}
		catch (Exception e)
		{
			throw new JspTagException("Unable to retrieve field errors from action", e);
		}
	}

	/**
	 * Gets the Struts OGNL value stack.
	 * @param pageContext the page context.
	 * @return the OGNL value stack.
	 * @throws JspTagException if an error occurred.
	 */
	public static OgnlValueStack getValueStack(PageContext pageContext) throws JspTagException
	{
		OgnlValueStack valueStack =
			(OgnlValueStack)pageContext.getAttribute("struts.valueStack", PageContext.REQUEST_SCOPE);

		if (valueStack == null)
		{
			throw new JspTagException("The request attribute struts.valueStack was not found");
		}

		return valueStack;
	}

	/**
	 * Determines whether a checkbox, radio button or select option is currently
	 * selected based on a property from the action bean.
	 * @param valueStack the value stack.
	 * @param name the name of the property to retrieve.
	 * @param value the value that a list or map value should be tested against
	 * to see if the control is checked.
	 * @return true if is checked.
	 * @throws JspTagException the jsp tag exception.
	 */
	public static boolean isSelected(OgnlValueStack valueStack, String name, String value) throws JspTagException
	{
		Object actionValue = ActionUtils.getActionValue(valueStack, name);

		boolean selected = false;

		if (actionValue != null)
		{
			if (actionValue.getClass().isArray())
			{
				for (Object item : (Object[])actionValue)
				{
					if ((item != null) && item.toString().equals(value))
					{
						selected = true;
						break;
					}
				}
			}
			else if (actionValue instanceof List)
			{
				for (Object item : (List<?>)actionValue)
				{
					if ((item != null) && item.toString().equals(value))
					{
						selected = true;
						break;
					}
				}
			}
			else if (actionValue instanceof Map)
			{
				for (Object item : ((Map<?, ?>)actionValue).values())
				{
					if ((item != null) && item.toString().equals(value))
					{
						selected = true;
						break;
					}
				}
			}
			else
			{
				selected = actionValue.toString().equals(value);
			}
		}

		return selected;
	}
}
