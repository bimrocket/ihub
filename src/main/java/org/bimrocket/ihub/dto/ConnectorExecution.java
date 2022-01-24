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
package org.bimrocket.ihub.dto;

/**
 *
 * @author realor
 */
public class ConnectorExecution
{
  private String name;
  private String description;
  private String startTime;
  private String endTime;
  private int processed;
  private int ignored;
  private int inserted;
  private int updated;
  private int deleted;
  private String lastError;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  public int getProcessed()
  {
    return processed;
  }

  public void setProcessed(int processed)
  {
    this.processed = processed;
  }

  public int getIgnored()
  {
    return ignored;
  }

  public void setIgnored(int ignored)
  {
    this.ignored = ignored;
  }

  public int getInserted()
  {
    return inserted;
  }

  public void setInserted(int inserted)
  {
    this.inserted = inserted;
  }

  public int getUpdated()
  {
    return updated;
  }

  public void setUpdated(int updated)
  {
    this.updated = updated;
  }

  public int getDeleted()
  {
    return deleted;
  }

  public void setDeleted(int deleted)
  {
    this.deleted = deleted;
  }

  public String getLastError()
  {
    return lastError;
  }

  public void setLastError(String lastError)
  {
    this.lastError = lastError;
  }
}
