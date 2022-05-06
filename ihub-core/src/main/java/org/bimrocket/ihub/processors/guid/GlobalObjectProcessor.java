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
package org.bimrocket.ihub.processors.guid;

import java.util.Iterator;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.GlobalIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Designed for objects that doesn't have globalId, usually after using one
 * loader which gets external data we only have localId, this processor adds to
 * ProcessedObject either new globalId or existing one from database IdPair's.
 * So use should be sequential after loader processor. Also sets global object
 * as json from dto ProcessorsGlobalObject
 *
 * @author kfiertek-nexus-geographics
 *
 */
public class GlobalObjectProcessor extends Processor
{
  private static final Logger log =
    LoggerFactory.getLogger(GlobalObjectProcessor.class);

  @ConfigProperty(name = "objectType",
    description = "The object type to treat Global Ids")
  public String objectType;

  @ConfigProperty(name = "pathLocalId",
    description = "Path to local id in JsonNode object if empty means sender will set local id",
    required = false)
  public String pathLocalId = "";

  protected final ObjectMapper mapper = new ObjectMapper();

  private List<IdPair> actualIdPairs;

  @Override
  public boolean processObject(ProcessedObject proObject)
  {
    if (proObject.isIgnore())
    {
      log.trace("processed object is ignore");
      return true;
    }

    if ((proObject.getObjectType() == null
        || proObject.getObjectType().isBlank()))
    {

      log.error("processed object type is null or blank");
      // This should never happen all loaders should assign object type to
      // ProcessedObject
      return false;
    }

    if (proObject.getLocalObject() == null
        && (proObject.isInsert() || proObject.isUpdate()))
    {
      log.error(
          "processed object local object is null while operation is insert or update");
      // This should never happen all insert or update should have valid
      // JsonNode as localObject
      // In case of Ignore or Delete operation localObject can be null
      return false;
    }

    if ((proObject.getLocalId() == null || proObject.getLocalId().isBlank())
        && (proObject.isInsert() || proObject.isUpdate())
        && !pathLocalId.isBlank())
    {
      log.debug("setting local id of ProcessedObject");
      JsonNode nodeToProcess = proObject.getLocalObject();
      String localId = null;

      String[] pathsLocalId = pathLocalId.split("\\.");
      JsonNode currentNode = nodeToProcess;
      for (int i = 0; i < pathsLocalId.length; i++)
      {
        currentNode = currentNode.get(pathsLocalId[i]);
      }
      localId = currentNode.asText();

      log.debug("local id found::{}", localId);
      proObject.setLocalId(localId);
    }

    if (proObject.getGlobalId() == null || proObject.getGlobalId().isBlank())
    {
      log.debug("setting global id for ProcessedObject");

      // Check if local object is actually global object
      // If true sets globalId
      JsonNode localObjectNode = proObject.getLocalObject();
      if (localObjectNode != null && !localObjectNode.isNull())
      {
        JsonNode globalIdNode = localObjectNode.get("globalId");
        if (globalIdNode != null && !globalIdNode.isNull()
            && globalIdNode.isTextual())
        {
          proObject.setGlobalId(globalIdNode.asText());
        }
        else
        {
          log.trace(
              "local object not a valid global object skipping set of globalId");
        }
      }
      // Searching for Pair of LocalID and GlobalID for specificed inventory and
      // object type
      var idPair = searchIdPair(proObject);
      if (idPair == null)
      {
        // Not found Pair in database
        // Create new IdPair
        // If processed object doesn't have globalId defined
        // new one is created.
        idPair = new IdPair();
        idPair.setConnectorName(getConnector().getName());
        idPair.setGlobalId(proObject.getGlobalId() != null
            && !proObject.getGlobalId().isBlank() ? proObject.getGlobalId()
                : GlobalIdGenerator.randomGlobalId());
        idPair.setInventory(getConnector().getInventory());
        idPair.setLocalId(proObject.getLocalId());
        idPair.setObjectType(proObject.getObjectType());
        idPair.setLastUpdate(new Date());
        actualIdPairs.add(idPair);
        proObject.setGlobalId(idPair.getGlobalId());
        proObject.setOperation(ProcessedObject.INSERT);
      }
      else
      {
        // IdPair found in database
        // either localId or globalId was already
        // saved
        idPair.setLastUpdate(new Date());
        if (proObject.getLocalId() == null && proObject.getLocalId().isBlank())
        {
          proObject.setLocalId(idPair.getLocalId());
        }
        if (proObject.getGlobalId() == null
            && proObject.getGlobalId().isBlank())
        {
          proObject.setGlobalId(idPair.getGlobalId());
        }
        proObject.setOperation(ProcessedObject.UPDATE);
      }
    }
    // If global object is empty set new ProcessorsGlobalObject
    if (proObject.getGlobalObject() == null)
    {
      ProcessorsGlobalObject globObject = new ProcessorsGlobalObject()
          .globalId(proObject.getGlobalId());
      if (proObject.isDelete())
      {
        globObject.element(mapper.nullNode());
      }
      else
      {
        globObject.element(proObject.getLocalObject());
      }
      proObject.setGlobalObject(mapper.valueToTree(globObject));

    }
    return true;
  }

  @Override
  public void init() throws Exception
  {
    super.init();
    this.setActualIdPairs();
  }

  private IdPair searchIdPair(ProcessedObject proObject)
  {
    IdPair found = null;
    Iterator<IdPair> ite = actualIdPairs.iterator();
    while (ite.hasNext() && found == null)
    {
      IdPair actual = ite.next();
      if ((actual.getLocalId() != null && actual.getLocalId().equals(proObject.getLocalId()))
          || (actual.getGlobalId() != null && actual.getGlobalId().equals(proObject.getGlobalId())))
      {
        found = actual;
      }
    }
    return found;
  }

  private void setActualIdPairs()
  {
    actualIdPairs = getConnector().getConnectorService()
      .getIdPairRepository().findByInventoryAndObjectType(
         getConnector().getInventory(), objectType);
  }
}
