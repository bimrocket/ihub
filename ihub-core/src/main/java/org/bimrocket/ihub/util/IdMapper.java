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
package org.bimrocket.ihub.util;

import java.util.Optional;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;

/**
 *
 * @author realor
 */
public class IdMapper
{
  private final IdPairRepository idPairRepository;
  private final String inventory;

  public IdMapper(IdPairRepository idPairRepository, String inventory)
  {
    this.idPairRepository = idPairRepository;
    this.inventory = inventory;
  }

  public String getGlobalId(String objectType, String localId)
  {
    return getGlobalId(objectType, localId, true);
  }

  public String getGlobalId(String objectType, String localId, boolean create)
  {
    Optional<IdPair> idPair = idPairRepository.
      findByInventoryAndObjectTypeAndLocalId(inventory, objectType, localId);

    if (idPair.isPresent())
    {
      return idPair.get().getGlobalId();
    }
    else if (create)
    {
      return GlobalIdGenerator.randomGlobalId();
    }
    return null;
  }

  public String getLocalId(String globalId)
  {
    Optional<IdPair> idPair =
      idPairRepository.findByInventoryAndGlobalId(inventory, globalId);

    return idPair.isPresent() ? idPair.get().getLocalId() : null;
  }

  public IdPair getIdPairByLocalId(String objectType,  String localId)
  {
    Optional<IdPair> idPair = idPairRepository.
      findByInventoryAndObjectTypeAndLocalId(inventory, objectType, localId);

    return idPair.isPresent() ? idPair.get() : null;
  }

  public IdPair getIdPairByGlobalId(String globalId)
  {
    Optional<IdPair> idPair =
      idPairRepository.findByInventoryAndGlobalId(inventory, globalId);

    return idPair.isPresent() ? idPair.get() : null;
  }
}
