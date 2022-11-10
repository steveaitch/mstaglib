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
 * $Id: Option.java 467 2022-11-07 15:42:01Z steveh $
 */

package com.jiglu.mstaglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.opensymphony.xwork2.ognl.OgnlValueStack;

/**
 * Creates an &lt;option&gt; HTML tag populated from a Struts 2 action.
 */
public final class Option extends BodyTagSupport implements DynamicAttributes
{
	private String m_name;

	private String m_value;

	private String m_disabled;

	private final Map<String, Object> m_dynamicAttributes = new HashMap<>();

	/** Serial version UID */
	private static final long serialVersionUID = -3108771368170343834L;

	@SuppressWarnings("resource")
	@Override
	public int doEndTag() throws JspException
	{
		// Get the value stack
		OgnlValueStack valueStack = ActionUtils.getValueStack(pageContext);

		// Build up the tags
		StringBuilder output = new StringBuilder(128);

		output.append("<option");

		if (m_value != null)
		{
			output.append(" value=\"").append(ActionUtils.escapeEntities(m_value)).append('"');
		}

		String bodyText = (getBodyContent() != null) ? getBodyContent().getString() : "";

		if (ActionUtils.isSelected(valueStack, m_name, m_value != null ? m_value : bodyText))
		{
			output.append(" selected=\"selected\"");
		}

		// Handle disabled attribute converting from boolean if needed
		if ((m_disabled != null) && (m_disabled.equalsIgnoreCase("true") || m_disabled.equalsIgnoreCase("disabled")))
		{
			output.append(" disabled=\"disabled\"");
		}

		// Handle dynamic attributes
		for (Entry<String, Object> entry : m_dynamicAttributes.entrySet())
		{
			output.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue()).append('"');
		}

		output.append('>').append(bodyText).append("</option>");

		// Write it out
		try
		{
			pageContext.getOut().print(output);
		}
		catch (IOException e)
		{
			throw new JspTagException(e);
		}

		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException
	{
		// Validate we have what we need
		Select parent = (Select)findAncestorWithClass(this, Select.class);

		if (parent == null)
		{
			throw new JspTagException("Can only be used inside select tag");
		}

		m_name = parent.getName();

		return EVAL_BODY_BUFFERED;
	}

	/**
	 * Sets the disabled.
	 * @param disabled the disabled.
	 */
	public void setDisabled(String disabled)
	{
		m_disabled = disabled;
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException
	{
		m_dynamicAttributes.put(localName, value);
	}

	/**
	 * Sets the value.
	 * @param value the value.
	 */
	public void setValue(String value)
	{
		m_value = value;
	}
}
