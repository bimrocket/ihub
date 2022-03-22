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
package org.bimrocket.ihub.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.dto.ProcessorSetup;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.ConnectorExecution;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.bimrocket.ihub.util.ConfigPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author realor
 */
@Service
public class ConnectorMapperService
{
  @Autowired
  ProcessorService processorService;

  public ConnectorSetup getConnectorSetup(Connector connector)
  {
    ConnectorSetup connSetup = new ConnectorSetup();

    connSetup.setName(connector.getName());
    connSetup.setDescription(connector.getDescription());
    connSetup.setInventory(connector.getInventory());
    connSetup.setAutoStart(connector.isAutoStart());
    connSetup.setSingleRun(connector.isSingleRun());
    connSetup.setWaitMillis(connector.getWaitMillis());

    List<ProcessorSetup> procSetups = new ArrayList<>();
    connector.getProcessors().forEach(processor ->
    {
      procSetups.add(getProcessorSetup(processor));
    });
    connSetup.setProcessors(procSetups);

    return connSetup;
  }

  public void setConnectorSetup(Connector connector, ConnectorSetup connSetup)
    throws Exception
  {
    setConnectorSetup(connector, connSetup, false);
  }

  public void setConnectorSetup(Connector connector, ConnectorSetup connSetup,
    boolean ignoreErrors) throws Exception
  {
    if (connSetup.getDescription() != null)
    {
      connector.setDescription(connSetup.getDescription());
    }
    if (connSetup.getInventory() != null)
    {
      connector.setInventory(connSetup.getInventory());
    }
    if (connSetup.getAutoStart() != null)
    {
      connector.setAutoStart(connSetup.getAutoStart());
    }
    if (connSetup.getSingleRun() != null)
    {
      connector.setSingleRun(connSetup.getSingleRun());
    }
    if (connSetup.getWaitMillis() != null)
    {
      connector.setWaitMillis(connSetup.getWaitMillis());
    }
    List<ProcessorSetup> procSetups = connSetup.getProcessors();

    if (procSetups != null)
    {
      List<Processor> processors = connector.getProcessors();

      for (int i = 0; i < procSetups.size(); i++)
      {
        ProcessorSetup procSetup = procSetups.get(i);

        if (i < processors.size()) // already exists a processor at i position
        {
          if (procSetup == null) continue;

          Processor processor = processors.get(i);

          String className = procSetup.getClassName();

          if (className != null
            && !processor.getClass().getName().equals(className))
          {
            // replace Processor
            try
            {
              processor = processorService.createProcessor(className);
              processors.set(i, processor);
            }
            catch (InvalidSetupException ex)
            {
              if (ignoreErrors)
              {
                continue;
              }

              throw ex;
            }
          }
          setProcessorSetup(processor, procSetup, ignoreErrors);
        }
        else // must add a new processor at i position
        {
          if (procSetup == null)
          {
            if (ignoreErrors) continue;

            throw new InvalidSetupException(340,
              "null processor at position {%d}", i);
          }

          String className = procSetup.getClassName();

          if (className == null)
          {
            if (ignoreErrors) continue;

            throw new InvalidSetupException(350,
              "processor className not set at position {%d}", i);
          }

          try
          {
            Processor processor = processorService.createProcessor(className);
            processors.add(processor);
            setProcessorSetup(processor, procSetup, ignoreErrors);
          }
          catch (InvalidSetupException ex)
          {
            if (ignoreErrors) continue;

            throw ex;
          }
        }
      }
      // remove remaining processors
      while (processors.size() > procSetups.size())
      {
        processors.remove(processors.size() - 1);
      }
      // apply changes to connector
      connector.setProcessors(processors);
    }
  }

  public ProcessorSetup getProcessorSetup(Processor processor)
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setClassName(processor.getClass().getName());
    procSetup.setDescription(processor.getDescription());
    procSetup.setEnabled(processor.isEnabled());

    procSetup.setProperties(getProcessorProperties(processor));

    return procSetup;
  }

  public void setProcessorSetup(Processor processor, ProcessorSetup procSetup)
    throws InvalidSetupException
  {
    setProcessorSetup(processor, procSetup, false);
  }

  public void setProcessorSetup(Processor processor, ProcessorSetup procSetup,
    boolean ignoreErrors) throws InvalidSetupException
  {
    if (procSetup.getDescription() != null)
    {
      processor.setDescription(procSetup.getDescription());
    }
    if (procSetup.getEnabled() != null)
    {
      processor.setEnabled(procSetup.getEnabled());
    }
    else
    {
      processor.setEnabled(false);
    }

    Map<String, Object> properties = procSetup.getProperties();
    if (properties != null)
    {
      setProcessorProperties(processor, properties, ignoreErrors);
    }
  }

  public Map<String, Object> getProcessorProperties(Processor processor)
  {
    Map<String, Object> properties = new HashMap<>();

    List<ConfigPropertyHandler> propHandlers =
      ConfigPropertyHandler.findProperties(processor.getClass());

    for (ConfigPropertyHandler propHandler : propHandlers)
    {
      String propertyName = propHandler.getName();
      try
      {
        properties.put(propertyName, propHandler.getValue(processor));
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
    return properties;
  }

  public void setProcessorProperties(Processor processor,
    Map<String, Object> properties, boolean ignoreErrors)
    throws InvalidSetupException
  {
    List<ConfigPropertyHandler> propHandlers =
      ConfigPropertyHandler.findProperties(processor.getClass());

    Map<String, ConfigPropertyHandler> propHandlerMap = new HashMap<>();
    propHandlers.forEach(
      propHandler -> propHandlerMap.put(propHandler.getName(), propHandler));

    for (Map.Entry<String, Object> entry : properties.entrySet())
    {
      String propertyName = entry.getKey();
      Object propertyValue = entry.getValue();
      ConfigPropertyHandler propHandler = propHandlerMap.get(propertyName);

      if (propHandler == null)
      {
        if (ignoreErrors) continue;

        throw new InvalidSetupException(360, "Unsupported config property {%s}",
          propertyName);
      }

      try
      {
        processor.setProperty(propertyName, propertyValue.toString());
        propHandler.setValue(processor, propertyValue);
      }
      catch (Exception ex)
      {
        if (ignoreErrors) continue;

        throw new InvalidSetupException(360,
          "Can not set value {%s} to config property {%s}", propertyValue,
          propertyName);
      }
    }
  }

  public ConnectorExecution getConnectorExecution(Connector connector)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    ConnectorExecution execution = new ConnectorExecution();
    execution.setName(connector.getName());
    execution.setDescription(connector.getDescription());
    if (connector.getStartTime() != null)
    {
      execution.setStartTime(df.format(connector.getStartTime()));
    }
    if (connector.getEndTime() != null)
    {
      execution.setEndTime(df.format(connector.getEndTime()));
    }
    execution.setProcessed(connector.getProcessed());
    execution.setInserted(connector.getInserted());
    execution.setUpdated(connector.getUpdated());
    execution.setDeleted(connector.getDeleted());

    return execution;
  }
}
