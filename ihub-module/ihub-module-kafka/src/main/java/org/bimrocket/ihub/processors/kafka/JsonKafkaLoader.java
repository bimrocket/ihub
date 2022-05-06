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
package org.bimrocket.ihub.processors.kafka;

import org.bimrocket.ihub.connector.ProcessedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bimrocket.ihub.util.ConfigProperty;

/**
 *
 * @author kfiertek-nexus-geographics
 */
public class JsonKafkaLoader extends KafkaLoader
{
  private static final Logger log =
    LoggerFactory.getLogger(JsonKafkaLoader.class);

  @ConfigProperty(name = "objectType",
    description = "The object type to load")
  public String objectType;

  protected final ObjectMapper mapper = new ObjectMapper();

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    String recordLoaded = getRecord();

    log.debug("getting record from kafka, record::{}", recordLoaded);
    if (recordLoaded == null)
    {
      procObject.setObjectType(objectType);
      procObject.setOperation(ProcessedObject.IGNORE);
      return false;
    }
    else
    {
      procObject.setOperation(ProcessedObject.INSERT);
      procObject.setObjectType(objectType);
      try
      {
        procObject.setLocalObject(mapper.readTree(recordLoaded));
      }
      catch (JsonProcessingException e)
      {
        log.debug("record not a valid json, this should never happen incoming record::{}",
          recordLoaded);
        procObject.setOperation(ProcessedObject.IGNORE);
        return false;
      }
      return true;
    }
  }
}
