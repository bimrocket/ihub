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
package org.bimrocket.ihub.connector;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author realor
 */
public class ProcessedObject
{
  public static final String IGNORE = "ignore";
  public static final String INSERT = "insert";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";

  private String objectType;
  private String localId;
  private String globalId;

  private JsonNode localObject;
  private JsonNode globalObject;

  private String operation = IGNORE;

  public String getObjectType()
  {
    return objectType;
  }

  public void setObjectType(String objectType)
  {
    this.objectType = objectType;
  }

  public String getLocalId()
  {
    return localId;
  }

  public void setLocalId(String localId)
  {
    this.localId = localId;
  }

  public String getGlobalId()
  {
    return globalId;
  }

  public void setGlobalId(String globalId)
  {
    this.globalId = globalId;
  }

  public JsonNode getLocalObject()
  {
    return localObject;
  }

  public void setLocalObject(JsonNode localObject)
  {
    this.localObject = localObject;
  }

  public JsonNode getGlobalObject()
  {
    return globalObject;
  }

  public void setGlobalObject(JsonNode globalObject)
  {
    this.globalObject = globalObject;
  }

  public String getOperation()
  {
    return operation;
  }

  public void setOperation(String operation)
  {
    this.operation = operation;
  }

  public boolean isIgnore()
  {
    return IGNORE.equals(operation);
  }

  public boolean isInsert()
  {
    return INSERT.equals(operation);
  }

  public boolean isUpdate()
  {
    return UPDATE.equals(operation);
  }

  public boolean isDelete()
  {
    return DELETE.equals(operation);
  }

  public void reset()
  {
    localId = null;
    globalId = null;
    objectType = null;
    localObject = null;
    globalObject = null;
    operation = IGNORE;
  }

  public ProcessedObject duplicate()
  {
    ProcessedObject copy = new ProcessedObject();
    copy.localId = localId;
    copy.globalId = globalId;
    copy.objectType = objectType;
    copy.localObject = localObject;
    copy.globalObject = globalObject;
    copy.operation = operation;
    return copy;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    return buffer.append("ConnectorObject")
      .append("{ localId: ")
      .append(localId)
      .append(", globalId: ")
      .append(globalId)
      .append(", objectType: ")
      .append(objectType)
      .append(", operation: ")
      .append(operation)
      .append(", localObject: ")
      .append(localObject)
      .append(", globalObject: ")
      .append(globalObject)
      .append(" }")
      .toString();
  }
}
