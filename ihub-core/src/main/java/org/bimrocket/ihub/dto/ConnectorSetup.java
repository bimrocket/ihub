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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author realor
 */
public class ConnectorSetup implements Serializable
{
  @Id
  @Schema(description = "Unique identifier of the connector.",
    example = "ConsumerGeoConnector", required = true)
  @NotBlank
  @Size(min = 5, max = 50)
  private String name;

  @Schema(description = "Description of the connector.",
    example = "Put here connector description and usage guide.",
    required = true)
  @Size(min = 0, max = 4000)
  private String description;

  @Schema(description = "Identifier of the inventory " +
    "(multiple connectors can access the same inventory), " +
    "it acts as identifier for objects that should be " +
    "equal but can be from different sources.",
    example = "GeoObjectsInventory", required = true)
  @NotBlank
  @Size(min = 5, max = 50)
  private String inventory;

  @Schema(description = "Should connector be started after creation ?",
    example = "true/false", required = true)
  private Boolean autoStart;

  @Schema(description = "Should connector run once after start ?",
    example = "true/false", required = true)
  private Boolean singleRun;

  @Schema(description = "How much to wait between runs ? (in miliseconds)",
    example = "1000", required = true)
  @Min(0)
  @Max(24 * 60 * 60 * 1000)
  private Long waitMillis;

  @Schema(description = "Execution periodicity (crontab format)",
    example = "30 * * *", required = false)
  private String crontab;

  private List<ProcessorSetup> processors = new ArrayList<>();

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

  public String getInventory()
  {
    return inventory;
  }

  public void setInventory(String inventory)
  {
    this.inventory = inventory;
  }

  public Boolean getAutoStart()
  {
    return autoStart;
  }

  public void setAutoStart(Boolean autoStart)
  {
    this.autoStart = autoStart;
  }

  public Boolean getSingleRun()
  {
    return singleRun;
  }

  public void setSingleRun(Boolean singleRun)
  {
    this.singleRun = singleRun;
  }

  public Long getWaitMillis()
  {
    return waitMillis;
  }

  public void setWaitMillis(Long waitMillis)
  {
    this.waitMillis = waitMillis;
  }

  public String getCrontab()
  {
    return crontab;
  }

  public void setCrontab(String crontab)
  {
    this.crontab = crontab;
  }

  public List<ProcessorSetup> getProcessors()
  {
    return processors;
  }

  public void setProcessors(List<ProcessorSetup> processors)
  {
    this.processors = processors;
  }

  public void copyTo(ConnectorSetup connSetup)
  {
    connSetup.name = name;
    connSetup.description = description;
    connSetup.inventory = inventory;
    connSetup.autoStart = autoStart;
    connSetup.singleRun = singleRun;
    connSetup.waitMillis = waitMillis;
    connSetup.crontab = crontab;
  }
}
