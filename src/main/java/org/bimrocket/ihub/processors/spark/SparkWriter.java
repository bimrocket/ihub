/**
BIMROCKET

Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat

This program is licensed and may be used, modified and redistributed under
the terms of the European Public License (EUPL), either version 1.1 or (at
your option) any later version as soon as they are approved by the European
Commission.

Alternatively, you may redistribute and/or modify this program under the
terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either  version 3 of the License, or (at your option)
any later version.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the licenses for the specific language governing permissions, limitations
and more details.

You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
with this program; if not, you may find them at:

https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
http://www.gnu.org/licenses/
and
https://www.gnu.org/licenses/lgpl.txt
**/
package org.bimrocket.ihub.processors.spark;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.bimrocket.ihub.config.SparkConfig;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.processors.Sender;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SparkWriter extends Sender
{
  private static final Logger log =
      LoggerFactory.getLogger(SparkWriter.class);

  public static final String PROPS_FORMAT = "format";
  public static final String PROPS_MODE = "mode";
  public static final String PROPS_FILE_SYSTEM_PATH = "fsPath";
  public static final String PROPS_FILE_NAME= "fileName";
  public static final String PROPS_UPDATEABLE_LENGTH = "updateableLength";

  
  @ConfigProperty(name = SparkWriter.PROPS_FORMAT,
    description = "Spark Format to write in.")
  public String format = "parquet";

  @ConfigProperty(name = SparkWriter.PROPS_MODE,
    description = "Spark write mode to write in")
  public String mode = "append";

  @ConfigProperty(name = SparkWriter.PROPS_FILE_SYSTEM_PATH,
    description = "Path to storage directory can be hdfs (hdfs://server/) or normal fs", required = false)
  public String fsPath = "C:\\";
 
  @ConfigProperty(name = SparkWriter.PROPS_FILE_NAME,
    description = "Name of file to write in")
  public String fileName;
  
  @ConfigProperty(name = SparkWriter.PROPS_UPDATEABLE_LENGTH,
      description = "Amount of records to perform update/save", required = false)
  public Integer updateAmount = 50;

  private ArrayNode all;
  private SparkSession session;
  private SparkContext context;
  private ObjectMapper mapper;
  private boolean save;
  
  
  @Override
  public void init() throws Exception
  {
    super.init();
    this.save = false;
    this.mapper = new ObjectMapper();
    this.all = mapper.createArrayNode();
    this.session = SparkConfig.buildContext();
    this.context = this.session.sparkContext();
  }
  
  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (procObject.isIgnore()) {
      this.batchUpdate();
      return false;
    }
    
    JsonNode toSend = procObject.getGlobalObject() != null ? procObject.getGlobalObject() : procObject.getLocalObject();
    if (toSend == null)
    {
      this.batchUpdate();
      return false;
    }

    all.add(toSend);
    this.batchUpdate();
    log.debug("adding {} json object to all elements array", toSend.toPrettyString());
    
    return true;
  }
  
  private void runSave() {
    this.save = true;
    this.batchUpdate();
    this.cleanUp();
  }

  protected void batchUpdate()
  {
    if ((all.size() > 0 && save) || all.size() > updateAmount) {
      String json = null;
      try
      {
        json = mapper.writeValueAsString(all);
      }
      catch (JsonProcessingException e)
      {
        log.error("While transforming all ArrayNode"
            + " to json String Exception has occurred ", e);
      }
      if (json == null)
        return;
      List<String> data = Arrays.asList(json);

      Dataset<String> ds = session.createDataset(data, Encoders.STRING());
      Dataset<Row> rows = session.read().json(ds);
      
      rows.write().mode(mode)
          .format(format)
          .save(fsPath
              + fileName);
    }
    
  }
  
  private void cleanUp() {
    this.save = false;
    this.all.removeAll();
  }
  
  @Override
  public void afterProcessing() {
    this.runSave();
  }
}