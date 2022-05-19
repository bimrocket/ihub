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

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

/**
 *
 * @author realor
 */
public class FacesUtils
{
  public static Object getExpressionValue(String expression)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();

    return application.evaluateExpressionGet(context, expression, Object.class);
  }

  public static FacesMessage createErrorMessage(Exception ex)
  {
    return new FacesMessage(SEVERITY_ERROR, ex.getMessage(), null);
  }

  public static FacesMessage createErrorMessage(String summary, Object ...args)
  {
    summary = String.format(summary, args);
    return new FacesMessage(SEVERITY_ERROR, summary, null);
  }

  public static FacesMessage createWarnMessage(String summary, Object ...args)
  {
    summary = String.format(summary, args);
    return new FacesMessage(SEVERITY_WARN, summary, null);
  }

  public static FacesMessage createInfoMessage(String summary, Object ...args)
  {
    summary = String.format(summary, args);
    return new FacesMessage(SEVERITY_INFO, summary, null);
  }

  public static void addErrorMessage(Exception ex)
  {
    FacesContext.getCurrentInstance().addMessage(null,
      FacesUtils.createErrorMessage(ex));
  }

  public static void addErrorMessage(String summary, Object ...args)
  {
    FacesContext.getCurrentInstance().addMessage(null,
      FacesUtils.createErrorMessage(summary, args));
  }

  public static void addWarnMessage(String summary, Object ...args)
  {
    FacesContext.getCurrentInstance().addMessage(null,
      FacesUtils.createWarnMessage(summary, args));
  }

  public static void addInfoMessage(String summary, Object ...args)
  {
    FacesContext.getCurrentInstance().addMessage(null,
      FacesUtils.createInfoMessage(summary, args));
  }
}
