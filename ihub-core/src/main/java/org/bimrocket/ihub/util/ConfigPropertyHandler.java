package org.bimrocket.ihub.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class ConfigPropertyHandler
{
  private final String name;
  private final String description;
  private final Field field;
  private final boolean required;
  private final boolean secret;
  private final String contentType;

  ConfigPropertyHandler(ConfigProperty property, Field field)
  {
    String propName = property.name();
    if (propName.length() == 0)
      propName = field.getName();

    this.name = propName;
    this.description = property.description();
    this.required = property.required();
    this.secret = property.secret();
    this.contentType = property.contentType();

    this.field = field;
    this.field.setAccessible(true);
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public boolean isRequired()
  {
    return required;
  }

  public boolean isSecret()
  {
    return secret;
  }

  public String getContentType()
  {
    return contentType;
  }

  public Field getField()
  {
    return field;
  }

  public void setValue(Object bean, Object value) throws Exception
  {
    if (value != null)
    {
      // special castings
      Class fieldClass = field.getType();

      if ((fieldClass.equals(float.class) || fieldClass.equals(Float.class))
        && value instanceof Number)
      {
        value = ((Number)value).floatValue();
      }

      field.set(bean, value);
    }
    else if (Collection.class.isAssignableFrom(field.getType()))
    {
      // clear collection if value is null
      ((Collection)field.get(bean)).clear();
    }
  }

  public Object getValue(Object bean) throws Exception
  {
    return field.get(bean);
  }

  public String getType()
  {
    String typeName = field.getType().getSimpleName();
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType)
    {
      ParameterizedType paramType = (ParameterizedType) genericType;
      typeName += "<";
      Type[] typeArgs = paramType.getActualTypeArguments();
      for (int i = 0; i < typeArgs.length; i++)
      {
        if (i > 0) typeName += ", ";
        typeName += ((Class) typeArgs[i]).getSimpleName();
      }
      typeName += ">";
    }
    return typeName;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(name);
    if (required)
    {
      buffer.append("*");
    }
    buffer.append(" : ").append(getType());
    return buffer.toString();
  }

  public static List<ConfigPropertyHandler> findProperties(Class cls)
  {
    return findProperties(cls, null);
  }

  public static List<ConfigPropertyHandler> findProperties(Class cls,
    List<ConfigPropertyHandler> properties)
  {
    if (properties == null)
      properties = new ArrayList<>();

    Class superClass = cls.getSuperclass();
    if (superClass != null && superClass != Object.class)
    {
      findProperties(superClass, properties);
    }

    Field[] fields = cls.getDeclaredFields();
    for (Field field : fields)
    {
      ConfigProperty property = field.getAnnotation(ConfigProperty.class);
      if (property != null)
      {
        ConfigPropertyHandler propHandler =
          new ConfigPropertyHandler(property, field);
        
        properties.add(propHandler);
      }
    }

    return properties;
  }
}
