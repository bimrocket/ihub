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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.util.Types;

import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.dto.ProcessorProperty;
import org.bimrocket.ihub.dto.ProcessorType;
import org.bimrocket.ihub.util.ConfigPropertyHandler;
import org.python.antlr.ast.arguments.vararg_descriptor;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

/**
 *
 * @author realor
 */
@Service
public class ProcessorTypeService
{
  public List<ProcessorType> findProcessorTypes(String className)
  {
    List<ProcessorType> procTypes = new ArrayList<>();

    Reflections reflections = new Reflections("org.bimrocket.ihub.processors");
    Set<Class<? extends Processor>> classSet = reflections
        .getSubTypesOf(Processor.class);
    for (Class<? extends Processor> procClass : classSet)
    {
      if (Modifier.isAbstract(procClass.getModifiers()))
        continue;

      if (className == null || procClass.getName().contains(className))
      {
        procTypes.add(getProcessorType(procClass));
      }
    }
    return procTypes;
  }

  public ProcessorType getProcessorType(Class<? extends Processor> procClass)
  {
    ProcessorType procType = new ProcessorType();
    procType.setClassName(procClass.getName());

    Map<String, ConfigPropertyHandler> propHandlers = ConfigPropertyHandler
        .findProperties(procClass);

    for (ConfigPropertyHandler propHandler : propHandlers.values())
    {
      ProcessorProperty property = new ProcessorProperty();
      property.setName(propHandler.getName());
      property.setDescription(propHandler.getDescription());
      property.setRequired(propHandler.isRequired());
      property.setType(propHandler.getType());
      property.setDefaultValue(propHandler.getDefaultValue());
      procType.getProperties().add(property);
    }
    return procType;
  }
}
