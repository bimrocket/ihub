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
package org.bimrocket.ihub.processors.test;

import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.util.ConfigProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.bimrocket.ihub.processors.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfiertek-nexus-geographics
 * @author realor
 */
public class HardcodedLoader extends Loader
{
  private static final Logger log =
    LoggerFactory.getLogger(HardcodedLoader.class);

  @ConfigProperty(name = "objectType",
    description = "The object type to load")
  public String objectType;

  @ConfigProperty(name = "jsonData",
    description = "Sample json object array to load",
    contentType = "application/json")
  public String jsonData;

  private JsonNode jsonArray;
  private int index = 0;

  @Override
  public void init() throws InvalidSetupException
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      jsonArray = mapper.readTree(jsonData);
    }
    catch (Exception ex)
    {
      throw new InvalidSetupException(400, "parse error");
    }

    if (!jsonArray.isArray())
      throw new InvalidSetupException(401, "json data is not an array");

    index = 0;
  }

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (index < jsonArray.size())
    {
      JsonNode node = jsonArray.get(index++);
      procObject.setLocalObject(node);
      procObject.setObjectType(objectType);
      procObject.setOperation(INSERT);

      log.debug("get object at index {}: {}", index, node.toString());

      return true;
    }
    else
    {
      return false;
    }
  }

  @Override
  public void end()
  {
    jsonArray = null;
  }
}
