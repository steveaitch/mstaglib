# Minimal Struts Tag Library

Like the Struts 2 framework but prefer to use JSTL-style tags and expressions in your JSPs? The Minimal Struts Tag Library gives
you a simple, fast alternative for the four main form elements.

## Usage

Add the taglib to your JSP:

	<%@ taglib uri="http://www.jiglu.com/taglibs/mstaglib" prefix="ms" %>

The tags are almost exact analogues of the HTML elements - and the Struts framework's own tags:

	<ms:input type="type" name="field" />
	
	<ms:textarea name="field" />
	
	<ms:select name="field">
		<ms:option value="value">Option</ms:option>
	</ms:select>

The `name` attribute is required and used to retrieve the appropriate value from the action bean. Like the Struts taglib you
can use list fields:

	<ms:input type="text" name="list[0]" />

or map fields:

	<ms:input type="text" name="map['key']" />

For the `<ms:input>` tag, the `type` attribute is required.

For the `<ms:option>` tag, whether the option is selected will be tested again the value attribute if present and the body text
if not.

If the `errorClass` attribute is specified and the corresponding Struts action implements `ValidationAware` then if there is
a field error present for the field with that name the `class` attribute value will be replaced by the `errorClass` attribute
value.

	<ms:textarea name="field" class="address" errorClass="address error" />

There is special handling of the `disabled` and `required` attributes. If a JSTL expression evaluates to true or the attribute
value is set to `disabled` / `required` as appropriate then the attribute will be included in the rendered tag.

	<ms:input type="text" name="email" required="required" disabled="${contactMethod != 'email'}" />

All other attributes are passed through to the rendered element as-is.

## Installation

Just drop `mstaglib-(version).jar` into your WEB-INF/lib directory. It's not yet in the Maven repository.

The tag library is compiled against Struts 6.0.3 but should work fine against most versions as it only imports two classes that
have (so far) remained stable between versions.

If you change the `acceptParamNames` regular expression for `ParametersInterceptor`  in `struts.xml` then you will need
to change the `ACCEPTED_PATTERN` regular expression in the `ActionUtils` class to match and recompile.
