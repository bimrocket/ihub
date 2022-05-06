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
package org.bimrocket.ihub.processors;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import org.bimrocket.ihub.connector.ProcessedObject;

/**
 *
 * @author realor
 */
public abstract class IncrementalLoader extends Loader
{
  private Iterator<JsonNode> changeIterator;

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (changeIterator == null)
    {
      changeIterator = scan();
    }

    if (changeIterator != null)
    {
      if (changeIterator.hasNext())
      {
        JsonNode localObject = changeIterator.next();
        procObject.setLocalObject(localObject);
        procObject.setLocalId(getLocalId(localObject));
        procObject.setOperation(getOperation(localObject));
        return true;
      }
      else
      {
        changeIterator = null;
      }
    }
    return false;
  }

  protected abstract Iterator<JsonNode> scan();
  protected abstract String getLocalId(JsonNode localObject);
  protected abstract String getOperation(JsonNode localObject);

}
