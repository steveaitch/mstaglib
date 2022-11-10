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
 * $Id: Input.java 467 2022-11-07 15:42:01Z steveh $
 */

package com.jiglu.mstaglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.opensymphony.xwork2.ognl.OgnlValueStack;

/**
 * Creates an &lt;input&gt; HTML tag populated from a Struts 2 action.
 */
public final class Input extends BodyTagSupport implements DynamicAttributes
{
	private String m_type;

	private String m_name;

	private String m_value;

	private String m_checked;

	private String m_disabled;

	private String m_required;

	private String m_class;

	private String m_errorClass;

	private final Map<String, Object> m_dynamicAttributes = new HashMap<>();

	/** Serial version UID */
	private static final long serialVersionUID = 8589423684828945795L;

	@SuppressWarnings("resource")
	@Override
	public int doEndTag() throws JspException
	{
		// Validate we have what we need
		if (m_type == null)
		{
			throw new JspTagException("No type attribute supplied");
		}

		if (m_name == null)
		{
			throw new JspTagException("No name attribute supplied");
		}

		// Get the value stack
		OgnlValueStack valueStack = ActionUtils.getValueStack(pageContext);

		// Build up the tag
		StringBuilder output = new StringBuilder(128);

		output.append("<input type=\"").append(m_type).append("\" name=\"").append(ActionUtils.escapeEntities(m_name))
			.append('"');

		// Handle checked and value according to type
		if (m_type.equals("checkbox") || m_type.equals("radio"))
		{
			if (m_checked != null)
			{
				if (m_checked.equalsIgnoreCase("true") || m_checked.equalsIgnoreCase("checked"))
				{
					output.append(" checked=\"checked\"");
				}
			}
			else
			{
				// We default to a value of true if none has been supplied
				if (m_value == null)
				{
					m_value = "true";
				}

				if (ActionUtils.isSelected(valueStack, m_name, m_value))
				{
					output.append(" checked=\"checked\"");
				}
			}

			output.append(" value=\"").append(m_value).append('"');
		}
		else if (!m_type.equals("file"))
		{
			if (m_value != null)
			{
				output.append(" value=\"").append(m_value).append('"');
			}
			else
			{
				output.append(" value=\"")
					.append(ActionUtils.escapeEntities(ActionUtils.getActionValueString(valueStack, m_name)))
					.append('"');
			}
		}

		// Handle required attribute converting from boolean if needed
		if ((m_required != null) && (m_required.equalsIgnoreCase("true") || m_required.equalsIgnoreCase("required")))
		{
			output.append(" required=\"required\"");
		}

		// Handle disabled attribute converting from boolean if needed
		if ((m_disabled != null) && (m_disabled.equalsIgnoreCase("true") || m_disabled.equalsIgnoreCase("disabled")))
		{
			output.append(" disabled=\"disabled\"");
		}

		// If have an error then swap to the error class
		Map<String, List<String>> fieldErrors = ActionUtils.getFieldErrors(valueStack);

		if ((m_errorClass != null) && (fieldErrors != null) && fieldErrors.containsKey(m_name))
		{
			output.append(" class=\"").append(m_errorClass).append('"');
		}
		else if (m_class != null)
		{
			output.append(" class=\"").append(m_class).append('"');
		}

		// Handle dynamic attributes
		for (Entry<String, Object> entry : m_dynamicAttributes.entrySet())
		{
			output.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue()).append('"');
		}

		output.append(" />");

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

	/**
	 * Sets the checked.
	 * @param checked the checked.
	 */
	public void setChecked(String checked)
	{
		m_checked = checked;
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
		if (localName.equals("class"))
		{
			m_class = (String)value;
		}
		else
		{
			m_dynamicAttributes.put(localName, value);
		}
	}

	/**
	 * Sets the error class.
	 * @param errorClass the error class.
	 */
	public void setErrorClass(String errorClass)
	{
		m_errorClass = errorClass;
	}

	/**
	 * Sets the name.
	 * @param name the name.
	 */
	public void setName(String name)
	{
		m_name = name;
	}

	/**
	 * Sets the required.
	 * @param required the required.
	 */
	public void setRequired(String required)
	{
		m_required = required;
	}

	/**
	 * Sets the type.
	 * @param type the type.
	 */
	public void setType(String type)
	{
		m_type = type;
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
