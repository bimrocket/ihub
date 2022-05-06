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

import javax.faces.context.FacesContext;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.exceptions.InvalidNameException;
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
public class ConnectorBean
{
  @Autowired
  ApplicationContext context;

  ConnectorSetup connSetup = new ConnectorSetup();
  boolean nameDisabled = false;

  public ConnectorSetup getConnectorSetup()
  {
    return connSetup;
  }

  public void setConnectorSetup(ConnectorSetup connSetup)
  {
    this.connSetup = connSetup;
    this.nameDisabled = connSetup.getName() != null;
  }

  public boolean isNameDisabled()
  {
    return nameDisabled;
  }

  public void accept()
  {
    try
    {
      ConnectorListBean connectorListBean =
        context.getBean(ConnectorListBean.class);

      connectorListBean.putConnector(connSetup);
      PrimeFaces.current().executeScript("PF('connector').hide()");
    }
    catch (InvalidNameException ex)
    {
      FacesContext.getCurrentInstance().addMessage("connector_name",
        FacesUtils.createErrorMessage(ex));
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
      PrimeFaces.current().executeScript("PF('connector').hide()");
    }
  }
}
