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

package org.bimrocket.ihub.connector;

import java.util.Properties;

/**
 *
 * @author realor
 */
public abstract class Processor
{
  private Connector connector;
  protected String description;
  protected boolean enabled;
  protected Properties props = new Properties();

  public Processor()
  {
  }

  public final Connector getConnector()
  {
    return connector;
  }

  public final void setConnector(Connector connector)
  {
    if (connector == null)
      throw new RuntimeException("Null connector not allowed");

    if (this.connector != null && this.connector != connector)
      throw new RuntimeException("Connector already set for this Processor");

    this.connector = connector;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }
  
  public Properties getProperties() {
    return this.props;
  }
  
  public void setProperty(String key, String value) {
    this.props.setProperty(key, value);
  }


  /**
   * Initialize processor. This method is called every time a connector
   * is started. The processor can obtain the resources required to perform
   * the object processing.
   *
   * @throws Exception when init fails and object processing can not go on.
   */
  public void init() throws Exception
  {
  }

  /**
   * Process an object.
   *
   * @param procObject, the object to process
   * @return false if object processing must be interrupted, true otherwise.
   */
  public abstract boolean processObject(ProcessedObject procObject);

  /**
   * Cleanup processor. This method is called every time a connector
   * stops running. The processor can release the resources obtained in the
   * init method.
   *
   */
  public void end()
  {
  }
  
  public void afterProcessing() {
    
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{}";
  }
}
