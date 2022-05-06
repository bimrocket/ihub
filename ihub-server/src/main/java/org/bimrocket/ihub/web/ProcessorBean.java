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

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.bimrocket.ihub.dto.ProcessorSetup;
import org.bimrocket.ihub.dto.ProcessorType;
import org.bimrocket.ihub.service.ProcessorService;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author realor
 */
@Component
@Scope("session")
public class ProcessorBean
{
  @Autowired
  ApplicationContext context;

  @Autowired
  ProcessorService processorService;

  private ProcessorSetup procSetup = new ProcessorSetup();
  private boolean creation = true;
  private List<SelectItem> processorTypeSelectItems;

  public ProcessorSetup getProcessorSetup()
  {
    return procSetup;
  }

  public void setProcessorSetup(ProcessorSetup procSetup)
  {
    this.procSetup = procSetup;
    creation = procSetup.getClassName() == null;
  }

  public boolean isCreation()
  {
    return creation;
  }

  public List<SelectItem> getProcessorTypeSelectItems()
  {
    if (processorTypeSelectItems == null)
    {
      List<ProcessorType> processorTypes =
        processorService.findProcessorTypes(null);
      processorTypeSelectItems = new ArrayList<>();

      processorTypes.sort((a, b) ->
        a.getClassName().compareTo(b.getClassName()));

      processorTypes.forEach(processorType -> processorTypeSelectItems.add(
        new SelectItem(processorType.getClassName())));
    }
    return processorTypeSelectItems;
  }

  public void accept()
  {
    try
    {
      ConnectorListBean connectorListBean =
        context.getBean(ConnectorListBean.class);

      connectorListBean.putProcessor(procSetup);
      PrimeFaces.current().executeScript("PF('processor').hide()");
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }
}
