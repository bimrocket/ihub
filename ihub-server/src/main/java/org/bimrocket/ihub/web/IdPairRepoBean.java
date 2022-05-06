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

import java.util.AbstractList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 *
 * @author realor
 */
@Component
@Scope("session")
public class IdPairRepoBean
{
  @Autowired
  IdPairRepository idPairRepository;

  private String inventory;
  private String objectType;
  private String localId;
  private String globalId;
  private Date lastUpdate;

  private final IdPairList idPairList = new IdPairList();
  private int rows = 10;
  private int firstRow = 0;

  public String getInventory()
  {
    return inventory;
  }

  public void setInventory(String inventory)
  {
    this.inventory = inventory;
  }

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

  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public void setLastUpdate(Date lastUpdate)
  {
    this.lastUpdate = lastUpdate;
  }

  public List<IdPair> getIdPairs()
  {
    return idPairList;
  }

  public int getRows()
  {
    return rows;
  }

  public void setRows(int rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public void search()
  {
    firstRow = 0;
    idPairList.refresh();
  }

  public void clearFilter()
  {
    inventory = null;
    objectType = null;
    localId = null;
    globalId = null;
    lastUpdate = null;
    firstRow = 0;
    idPairList.refresh();
  }

  public void saveIdPair(IdPair idPair)
  {
    idPairRepository.save(idPair);

    idPairList.refresh();
  }

  public void deleteIdPair(IdPair idPair)
  {
    idPairRepository.delete(idPair);

    idPairList.refresh();
  }

  public class IdPairList extends AbstractList<IdPair>
  {
    int pageSize = 20;
    Map<Integer, Page<IdPair>> pages = new HashMap<>();

    @Override
    public IdPair get(int index)
    {
      var page = loadPage(index);

      List<IdPair> content = page.getContent();
      if (content.isEmpty()) return null;

      return content.get(index % pageSize);
    }

    @Override
    public int size()
    {
      var page = loadPage(0);
      return (int)page.getTotalElements();
    }

    void refresh()
    {
      pages.clear();
    }

    Page<IdPair> loadPage(int index)
    {
      int pageNumber = index / pageSize;

      var page = pages.get(pageNumber);

      if (page == null)
      {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        page = idPairRepository
          .findByInventoryLikeAndObjectTypeLikeAndLocalIdLikeAndGlobalIdLike(
            blankNull(inventory), blankNull(objectType),
            blankNull(localId), blankNull(globalId), pageable);

        pages.put(pageNumber, page);
      }
      return page;
    }

    String blankNull(String value)
    {
      return value == null || value.isBlank() ? "" : value.trim();
    }
  }
}
