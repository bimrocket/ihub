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
package org.bimrocket.ihub.repo;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bimrocket.ihub.dto.IdPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author realor
 */

public interface IdPairRepository
{
  public IdPair save(IdPair idPair);

  public void delete(IdPair idPair);

  public void deleteById(String id);

  public Optional<IdPair> findByInventoryAndObjectTypeAndLocalId(
    String inventory, String objectType, String localId);

  public Optional<IdPair> findByInventoryAndGlobalId(
    String inventory, String globalId);

  public List<IdPair> findByInventoryAndObjectTypeAndLastUpdateLessThan(
    String inventory, String objectType, Date date);

  public Page<IdPair> findByInventoryLikeAndObjectTypeLikeAndLocalIdLikeAndGlobalIdLike(
    String inventory, String objectType, String localId, String globalId,
      Pageable pageable);

  public List<IdPair> findByInventoryAndObjectType(
    String inventory, String objectType);
}
