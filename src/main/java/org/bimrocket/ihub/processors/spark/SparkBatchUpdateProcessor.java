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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.processors.Sender;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SparkBatchUpdateProcessor extends Sender
{
  private static final Logger log =
      LoggerFactory.getLogger(SparkBatchUpdateProcessor.class);

  public static final String PROPS_FORMAT = "format";
  public static final String PROPS_MODE = "mode";
  public static final String PROPS_FILE_SYSTEM_PATH = "fs.path";
  public static final String PROPS_FILE_NAME= "file.name";
  public static final String PROPS_UPDATEABLE_LENGTH = "updateable.length";

  
  @ConfigProperty(name = SparkBatchUpdateProcessor.PROPS_FORMAT,
    description = "Spark Format to write in.")
  public String format = "parquet";

  @ConfigProperty(name = SparkBatchUpdateProcessor.PROPS_MODE,
    description = "Spark write mode to write in")
  public String mode = "append";

  @ConfigProperty(name = SparkBatchUpdateProcessor.PROPS_FILE_SYSTEM_PATH,
    description = "Path to storage directory can be hdfs (hdfs://server/) or normal fs", required = false)
  public String fsPath = "C:\\";
 
  @ConfigProperty(name = SparkBatchUpdateProcessor.PROPS_FILE_NAME,
    description = "Name of file to write in")
  public String fileName;
  
  @ConfigProperty(name = SparkBatchUpdateProcessor.PROPS_UPDATEABLE_LENGTH,
      description = "Amount of records to perform update/save", required = false)
  public Integer updateAmount = 50;

  private ArrayNode all;
  private ExecutorService executor;
  private SparkBatchUpdateRunnable batchUpdater;
  
  
  @Override
  public void init() throws Exception
  {
    super.init();
    ObjectMapper mapper = new ObjectMapper();
    this.all = mapper.createArrayNode();
    this.executor = Executors.newSingleThreadExecutor();
    this.batchUpdater = new SparkBatchUpdateRunnable(this.all, this.getProperties());
    executor.submit(this.batchUpdater);
  }
  
  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (procObject.isIgnore()) {
      this.batchUpdater.runSave();
      return false;
    }
    
    JsonNode toSend = procObject.getGlobalObject() != null ? procObject.getGlobalObject() : procObject.getLocalObject();
    if (toSend == null)
    {
      this.batchUpdater.runSave();
      return false;
    }

    all.add(toSend);
    synchronized (this) {
      executor.notifyAll();
    }
    log.debug("adding {} json object to all elements array", toSend.toPrettyString());
    
    return true;
  }
  
  @Override
  public void end() {
    log.debug("ending SparkBatchUpdaterProcessor");
    super.end();
    executor.shutdown();
  }
  
}