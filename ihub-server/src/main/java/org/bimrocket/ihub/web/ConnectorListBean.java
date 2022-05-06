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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.event.FacesEvent;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.ProcessorProperty;
import org.bimrocket.ihub.dto.ProcessorSetup;
import org.bimrocket.ihub.dto.ProcessorType;
import org.bimrocket.ihub.exceptions.NotFoundException;
import org.bimrocket.ihub.service.ConnectorMapperService;
import org.bimrocket.ihub.service.ConnectorService;
import org.bimrocket.ihub.service.ProcessorService;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author realor
 */
@Component
@Scope("session")
public class ConnectorListBean
{
  @Autowired
  ConnectorService connectorService;

  @Autowired
  ProcessorService processorService;

  @Autowired
  ConnectorMapperService connectorMapperService;

  @Autowired
  ConnectorBean connectorBean;

  @Autowired
  ProcessorBean processorBean;

  @Autowired
  PropertyBean propertyBean;

  TreeNode<Object> rootNode = new DefaultTreeNode<>("root", "Inventories", null);
  TreeNode<Object> selectedNode;
  String connectorName;
  String operation;
  Set<String> changed = new HashSet<>();

  boolean connectorStarted;

  public void search()
  {
    rootNode = findConnectors(connectorName);
    changed.clear();
  }

  public String getConnectorName()
  {
    return connectorName;
  }

  public void setConnectorName(String connectorName)
  {
    this.connectorName = connectorName;
  }

  public TreeNode<Object> getSelectedNode()
  {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode<Object> selectedNode)
  {
    this.selectedNode = selectedNode;
  }

  public void setNodes(TreeNode<Object> rootNode)
  {
    this.rootNode = rootNode;
  }

  public TreeNode<Object> getNodes()
  {
    return rootNode;
  }

  public boolean isConnectorChanged()
  {
    if (selectedNode == null) return false;

    Object data = selectedNode.getData();
    if (data instanceof ConnectorSetup)
    {
      ConnectorSetup connSetup = (ConnectorSetup)data;
      return changed.contains(connSetup.getName());
    }
    return false;
  }

  public boolean isConnectorChanged(ConnectorSetup connSetup)
  {
    if (connSetup == null) return false;
    return changed.contains(connSetup.getName());
  }

  public boolean isConnectorUnsaved()
  {
    if (selectedNode == null) return false;

    Object data = selectedNode.getData();
    if (data instanceof ConnectorSetup)
    {
      ConnectorSetup connSetup = (ConnectorSetup)data;
      String connName = connSetup.getName();
      try
      {
        return connectorService.getConnector(connName).isUnsaved();
      }
      catch (NotFoundException ex)
      {
      }
    }
    return false;
  }

  public boolean isConnectorUnsaved(ConnectorSetup connSetup)
  {
    if (connSetup == null) return false;
    String connName = connSetup.getName();
    try
    {
      return connectorService.getConnector(connName).isUnsaved();
    }
    catch (NotFoundException ex)
    {
    }
    return false;
  }

  public String getConnectorError(ConnectorSetup connSetup)
  {
    String connName = connSetup.getName();
    try
    {
      Exception lastError =
        connectorService.getConnector(connName).getLastError();
      if (lastError != null)
      {
        return lastError.getMessage();
      }
    }
    catch (NotFoundException ex)
    {
    }
    return null;
  }

  // connector operations

  public void addConnector()
  {
    ConnectorSetup connSetup = new ConnectorSetup();
    connSetup.setWaitMillis(10000L);
    connectorBean.setConnectorSetup(connSetup);
    operation = "add";
  }

  public void editConnector()
  {
    ConnectorSetup connSetup = new ConnectorSetup();
    ConnectorSetup curConnSetup = (ConnectorSetup)selectedNode.getData();
    curConnSetup.copyTo(connSetup);
    connectorBean.setConnectorSetup(connSetup);
    operation = "edit";
  }

  public void putConnector(ConnectorSetup connSetup) throws Exception
  {
    if ("add".equals(operation))
    {
      Connector connector =
        connectorService.createConnector(connSetup.getName());
      connectorMapperService.setConnectorSetup(connector, connSetup);
      createTreeNode(connSetup, rootNode);
    }
    else // edit
    {
      ConnectorSetup curConnSetup = (ConnectorSetup)selectedNode.getData();
      connSetup.copyTo(curConnSetup);
    }
    String connName = connSetup.getName();
    changed.add(connName);
  }

  public void applyConnectorChanges()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      String connName = connSetup.getName();
      Connector connector = connectorService.getConnector(connName);

      connectorMapperService.setConnectorSetup(connector, connSetup);
      changed.remove(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void undoConnectorChanges()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      String connName = connSetup.getName();
      Connector connector = connectorService.getConnector(connName);

      connSetup = connectorMapperService.getConnectorSetup(connector);

      TreeNode<Object> connNode = createTreeNode(connSetup, null);
      connNode.setExpanded(true);

      int index = rootNode.getChildren().indexOf(selectedNode);
      rootNode.getChildren().set(index, connNode);

      changed.remove(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void saveConnector()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      String connName = connSetup.getName();
      Connector connector = connectorService.getConnector(connName);
      connectorMapperService.setConnectorSetup(connector, connSetup);
      connector.save();
      changed.remove(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void restoreConnector()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      String connName = connSetup.getName();

      Connector connector = connectorService.getConnector(connName);
      connector.restore();

      connSetup =
        connectorMapperService.getConnectorSetup(connector);

      TreeNode<Object> connNode = createTreeNode(connSetup, null);
      connNode.setExpanded(true);

      int index = rootNode.getChildren().indexOf(selectedNode);
      rootNode.getChildren().set(index, connNode);

      changed.remove(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void deleteConnector()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      String connName = connSetup.getName();

      connectorService.destroyConnector(connName, true);

      rootNode.getChildren().remove(selectedNode);
      selectedNode = null;

      changed.remove(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public boolean getConnectorStarted()
  {
    Object data = FacesUtils.getExpressionValue("#{data}");
    if (data instanceof ConnectorSetup)
    {
      String name = ((ConnectorSetup)data).getName();
      try
      {
        Connector connector = connectorService.getConnector(name);
        return connector.isStarted();
      }
      catch (NotFoundException ex)
      {
      }
    }
    return false;
  }

  public void setConnectorStarted(boolean started)
  {
    // status change is performed in listener
    connectorStarted = started;
  }

  public void connectorStatusChanged(FacesEvent event)
  {
    ConnectorSetup connSetup =
      (ConnectorSetup)FacesUtils.getExpressionValue("#{data}");

    String name = connSetup.getName();
    try
    {
      Connector connector = connectorService.getConnector(name);
      if (connectorStarted)
      {
        connector.start();
      }
      else
      {
        connector.stop();
      }
    }
    catch (NotFoundException ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public String getSelectedConnectorName()
  {
    if (selectedNode != null)
    {
      if (selectedNode.getData() instanceof ConnectorSetup)
      {
        ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
        return connSetup.getName();
      }
    }
    return null;
  }

  // processor operations

  public String getProcessorDescription(ProcessorSetup procSetup)
  {
    String description = procSetup.getDescription();
    if (description == null || description.length() == 0)
    {
      description = procSetup.getClassName();
    }
    else
    {
      String className = procSetup.getClassName();
      int index = className.lastIndexOf(".");
      if (index > 0)
      {
        className = className.substring(index + 1);
      }
      description += " (" + className + ")";
    }
    return description;
  }

  public boolean isProcessorEnabled(ProcessorSetup procSetup)
  {
    if (procSetup == null || procSetup.getEnabled() == null) return false;
    return procSetup.getEnabled();
  }

  public void addProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setEnabled(Boolean.TRUE);
    processorBean.setProcessorSetup(procSetup);
    operation = "add";
  }

  public void insertProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setEnabled(Boolean.TRUE);
    processorBean.setProcessorSetup(procSetup);
    operation = "insert";
  }

  public void editProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    ProcessorSetup curProcSetup = (ProcessorSetup)selectedNode.getData();
    curProcSetup.copyTo(procSetup);
    processorBean.setProcessorSetup(procSetup);
    operation = "edit";
  }

  public void putProcessor(ProcessorSetup procSetup) throws Exception
  {
    ConnectorSetup connSetup;
    if ("add".equals(operation))
    {
      TreeNode<Object> connNode = selectedNode;
      connSetup = (ConnectorSetup)connNode.getData();

      setDefaultProperties(procSetup);

      connSetup.getProcessors().add(procSetup);

      createTreeNode(procSetup, selectedNode);
      connNode.setExpanded(true);
    }
    else if ("insert".equals(operation))
    {
      TreeNode<Object> connNode = selectedNode.getParent();
      connSetup = (ConnectorSetup)connNode.getData();

      ProcessorSetup selProcSetup = (ProcessorSetup)selectedNode.getData();

      int index = connSetup.getProcessors().indexOf(selProcSetup);

      setDefaultProperties(procSetup);

      connSetup.getProcessors().add(index, procSetup);

      TreeNode<Object> procNode = createTreeNode(procSetup, null);
      connNode.getChildren().add(index, procNode);
    }
    else // edit
    {
      ProcessorSetup curProcSetup = (ProcessorSetup)selectedNode.getData();
      procSetup.copyTo(curProcSetup);

      TreeNode<Object> connNode = selectedNode.getParent();
      connSetup = (ConnectorSetup)connNode.getData();
    }
    String connName = connSetup.getName();
    changed.add(connName);
  }

  public void deleteProcessor()
  {
    try
    {
      TreeNode<Object> connNode = selectedNode.getParent();
      ConnectorSetup connSetup = (ConnectorSetup)connNode.getData();

      ProcessorSetup procSetup = (ProcessorSetup)selectedNode.getData();
      connSetup.getProcessors().remove(procSetup);

      connNode.getChildren().remove(selectedNode);
      selectedNode = null;

      String connName = connSetup.getName();
      changed.add(connName);
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  // property operations

  public String getPropertyValue(TreeNode<Object> propNode)
  {
    TreeNode<Object> procNode = propNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    ProcessorProperty property = (ProcessorProperty)propNode.getData();

    Object value = procSetup.getProperties().get(property.getName());
    if (value == null) return "";

    if (property.isSecret()) return "******";

    String textValue = String.valueOf(value);

    if (textValue.length() > 60)
    {
      textValue = textValue.substring(0, 50) + "...";
    }

    return textValue;
  }

  public void editProperty()
  {
    ProcessorProperty property = (ProcessorProperty)selectedNode.getData();
    TreeNode<Object> procNode = selectedNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    Object value = procSetup.getProperties().get(property.getName());

    propertyBean.setProperty(property);
    propertyBean.setValue(value);
  }

  public void putProperty(Object value)
  {
    TreeNode<Object> propNode = selectedNode;
    ProcessorProperty property = (ProcessorProperty)propNode.getData();
    TreeNode<Object> procNode = propNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    procSetup.getProperties().put(property.getName(), value);
    TreeNode<Object> connNode = procNode.getParent();
    ConnectorSetup connSetup = (ConnectorSetup)connNode.getData();

    String connName = connSetup.getName();
    changed.add(connName);
  }

  // other methods

  public void onNodeExpand(NodeExpandEvent event)
  {
  }

  public void onNodeCollapse(NodeCollapseEvent event)
  {
  }

  private TreeNode<Object> findConnectors(String name)
  {
    rootNode = new DefaultTreeNode<>("root", "Inventories", null);

    ArrayList<Connector> connectors =
      connectorService.getConnectorsByName(name);

    connectors.forEach(connector ->
    {
      ConnectorSetup connSetup =
        connectorMapperService.getConnectorSetup(connector);

      createTreeNode(connSetup, rootNode);
    });
    return rootNode;
  }

  private TreeNode<Object> createTreeNode(ConnectorSetup connSetup,
    TreeNode<Object> rootNode)
  {
    TreeNode<Object> connNode = new DefaultTreeNode<>(connSetup);
    connNode.setType("connector");

    if (rootNode != null)
    {
      rootNode.getChildren().add(connNode);
    }

    List<ProcessorSetup> procSetups = connSetup.getProcessors();
    procSetups.forEach(procSetup ->
    {
      createTreeNode(procSetup, connNode);
    });
    return connNode;
  }

  private TreeNode<Object> createTreeNode(ProcessorSetup procSetup,
    TreeNode<Object> connNode)
  {
    TreeNode<Object> procNode = new DefaultTreeNode<>(procSetup);
    procNode.setType("processor");

    if (connNode != null)
    {
      connNode.getChildren().add(procNode);
    }

    try
    {
      ProcessorType processorType =
        processorService.getProcessorType(procSetup.getClassName());

      List<ProcessorProperty> properties = processorType.getProperties();
      properties.forEach(property ->
      {
        TreeNode<Object> propNode = new DefaultTreeNode<>("property",
          property, procNode);
      });
    }
    catch (InvalidSetupException ex)
    {
      // ignore
    }
    return procNode;
  }

  private void setDefaultProperties(ProcessorSetup procSetup)
  {
    try
    {
      String className = procSetup.getClassName();
      Processor processor = processorService.createProcessor(className);

      procSetup.setProperties(
        connectorMapperService.getProcessorProperties(processor));
    }
    catch (InvalidSetupException ex)
    {
      // ignore
    }
  }
}
