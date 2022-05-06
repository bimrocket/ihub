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
package org.bimrocket.ihub.processors.rhino;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author realor
 */
public class JsonNodeScriptable extends NativeJavaObject
{
  private final JsonNode node;

  public JsonNodeScriptable(Scriptable scope, JsonNode node)
  {
    super(scope, node, JsonNode.class);
    this.node = node;
  }

  @Override
  public String getClassName()
  {
    return "JsonNodeScriptable";
  }

  JsonNode getNode()
  {
    return node;
  }

  @Override
  public Object get(String name, Scriptable scope)
  {
    if (node.has(name))
    {
      JsonNode valueNode = node.get(name);
      if (valueNode.isArray() || valueNode.isObject())
      {
        return new JsonNodeScriptable(scope, valueNode);
      }
      else
      {
        return nodeToJava(valueNode);
      }
    }
    return super.get(name, scope);
  }

  @Override
  public Object get(int i, Scriptable scope)
  {
    if (i < node.size())
    {
      JsonNode valueNode = node.get(i);
      if (valueNode.isArray() || valueNode.isObject())
      {
        return new JsonNodeScriptable(scope, valueNode);
      }
      else
      {
        return nodeToJava(valueNode);
      }
    }
    return super.get(i, scope);
  }

  private Object nodeToJava(JsonNode valueNode)
  {
    if (valueNode.isInt())
    {
      return valueNode.asInt();
    }
    if (valueNode.isLong())
    {
      return valueNode.asInt();
    }
    if (valueNode.isNumber())
    {
      return valueNode.asDouble();
    }
    if (valueNode.isBoolean())
    {
      return valueNode.asBoolean();
    }
    if (valueNode.isTextual())
    {
      return valueNode.asText();
    }
    return null;
  }

  public static void main(String[] args) throws Exception
  {
    String code = "a = localObject.person";

    ObjectMapper mapper = new ObjectMapper();

    JsonNode node = mapper.readTree("{\"person\": {\"name\": \"Ricard\"} }");

    Context cx = Context.enter();
    try
    {
      ScriptableObject scope = cx.initStandardObjects();
      scope.put("localObject", scope, new JsonNodeScriptable(scope, node));

      Object result = cx.evaluateString(scope, code, "<code>", 1, null);
      System.out.println(result.getClass());

      System.out.println(Context.toString(result));
    }
    finally
    {
      // Exit from the context.
      Context.exit();
    }

  }
}
