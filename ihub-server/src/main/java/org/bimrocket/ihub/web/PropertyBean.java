/*
 * BIMROCKET
 *
 * Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.bimrocket.ihub.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import org.bimrocket.ihub.dto.ProcessorProperty;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author realor
 */
@Component
@Scope("session")
public class PropertyBean
{
  @Autowired
  ApplicationContext context;

  ProcessorProperty property;
  Object value;
  ObjectMapper mapper = new ObjectMapper();
  Exception jsonError;

  static final Map<String, Converter> converters = new HashMap<>();
  static final Set<String> supportedContentTypes = new HashSet<>();

  static
  {
    converters.put("Short", new ShortConverter());
    converters.put("Integer", new IntegerConverter());
    converters.put("Long", new LongConverter());
    converters.put("Float", new FloatConverter());
    converters.put("Double", new DoubleConverter());

    converters.put("short", converters.get("Short"));
    converters.put("int", converters.get("Integer"));
    converters.put("long", converters.get("Long"));
    converters.put("float", converters.get("Float"));
    converters.put("double", converters.get("Double"));

    supportedContentTypes.add("application/javascript");
    supportedContentTypes.add("application/json");
    supportedContentTypes.add("text/x-sql");
  }

  public ProcessorProperty getProperty()
  {
    return property;
  }

  public void setProperty(ProcessorProperty property)
  {
    this.property = property;
  }

  public Object getValue()
  {
    return value;
  }

  public void setValue(Object value)
  {
    this.value = value;
  }

  public Converter getConverter()
  {
    if (property == null) return null;
    return converters.get(property.getType());
  }

  public String getPropertyType()
  {
    if (property == null) return null;

    if (isStructuredText())
    {
      return property.getContentType();
    }
    else
    {
      return property.getType();
    }
  }

  public boolean isStructuredText()
  {
    if (property == null) return false;
    return supportedContentTypes.contains(property.getContentType());
  }

  public boolean isNumericValue()
  {
    if (property == null) return false;
    return converters.get(property.getType()) instanceof NumericConverter;
  }

  public boolean isDecimalValue()
  {
    if (property == null) return false;
    return "float double Float Double".contains(property.getType());
  }

  public boolean isStringValue()
  {
    if (property == null) return false;
    return "String".equals(property.getType());
  }

  public boolean isBooleanValue()
  {
    if (property == null) return false;
    return "boolean Boolean".contains(property.getType());
  }

  public boolean isGenericValue()
  {
    if (property == null) return false;

    String propType = property.getType();

    return !"String".equals(propType)
           && !"boolean Boolean".contains(propType)
           && !converters.containsKey(propType);
  }

  public String getJsonValue()
  {
    try
    {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public void setJsonValue(String json)
  {
    try
    {
      jsonError = null;
      this.value = mapper.readValue(json, Object.class);
    }
    catch (Exception ex)
    {
      jsonError = ex;
    }
  }

  public void accept()
  {
    if (jsonError != null)
    {
      FacesContext.getCurrentInstance().addMessage("json_editor",
        FacesUtils.createErrorMessage(jsonError));
    }
    else
    {
      try
      {
        ConnectorListBean connectorListBean =
          context.getBean(ConnectorListBean.class);

       connectorListBean.putProperty(value);
        PrimeFaces.current().executeScript("PF('property').hide()");
      }
      catch (Exception ex)
      {
        FacesUtils.addErrorMessage(ex);
      }
    }
  }

  public static abstract class NumericConverter implements Converter
  {
    @Override
    public String getAsString(FacesContext facesContext,
      UIComponent component, Object value)
    {
      return value == null ? "0" : value.toString();
    }
  }

  public static class ShortConverter extends NumericConverter
  {
    @Override
    public Object getAsObject(FacesContext facesContext,
      UIComponent component, String value)
    {
      try
      {
        return value == null ? Short.valueOf((short)0) : Short.valueOf(value);
      }
      catch (NumberFormatException ex)
      {
        throw new ConverterException(
          FacesUtils.createErrorMessage("Invalid short: {%s}", value));
      }
    }
  }

  public static class IntegerConverter extends NumericConverter
  {
    @Override
    public Object getAsObject(FacesContext facesContext,
      UIComponent component, String value)
    {
      try
      {
        return value == null ? Integer.valueOf(0) : Integer.valueOf(value);
      }
      catch (NumberFormatException ex)
      {
        throw new ConverterException(
          FacesUtils.createErrorMessage("Invalid integer: {%s}", value));
      }
    }
  }

  public static class LongConverter extends NumericConverter
  {
    @Override
    public Object getAsObject(FacesContext facesContext,
      UIComponent component, String value)
    {
      try
      {
        return value == null ? Long.valueOf(0L) : Long.valueOf(value);
      }
      catch (NumberFormatException ex)
      {
        throw new ConverterException(
          FacesUtils.createErrorMessage("Invalid long: {%s}", value));
      }
    }
  }

  public static class FloatConverter extends NumericConverter
  {
    @Override
    public Object getAsObject(FacesContext facesContext,
      UIComponent component, String value)
    {
      try
      {
        return value == null ? Float.valueOf(0f) : Float.valueOf(value);
      }
      catch (NumberFormatException ex)
      {
        throw new ConverterException(
          FacesUtils.createErrorMessage("Invalid float: {%s}", value));
      }
    }
  }

  public static class DoubleConverter extends NumericConverter
  {
    @Override
    public Object getAsObject(FacesContext facesContext,
      UIComponent component, String value)
    {
      try
      {
        return value == null ? Double.valueOf(0d) : Double.valueOf(value);
      }
      catch (NumberFormatException ex)
      {
        throw new ConverterException(
          FacesUtils.createErrorMessage("Invalid double: {%s}", value));
      }
    }
  }
}
