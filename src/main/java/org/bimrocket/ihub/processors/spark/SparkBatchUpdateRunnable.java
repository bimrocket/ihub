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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.bimrocket.ihub.config.SparkConfig;
import org.bimrocket.ihub.util.BatchUpdateRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SparkBatchUpdateRunnable extends BatchUpdateRunnable
{
  private static final Logger log = LoggerFactory
      .getLogger(SparkBatchUpdateRunnable.class);

  SparkSession session;
  SparkContext context;
  Properties sparkProps;

  public SparkBatchUpdateRunnable(ArrayNode all, Properties sparkProps)
  {
    super(all, Integer
        .valueOf((String) sparkProps.get(SparkBatchUpdateProcessor.PROPS_UPDATEABLE_LENGTH)));
    this.sparkProps = sparkProps;
    this.session = SparkConfig.getSession();
    this.context = this.session.sparkContext();
  }

  @Override
  protected void batchUpdate()
  {
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

    ds.write().mode((String) sparkProps.get(SparkBatchUpdateProcessor.PROPS_MODE))
        .format((String) sparkProps.get(SparkBatchUpdateProcessor.PROPS_FORMAT))
        .save((String) sparkProps.get(SparkBatchUpdateProcessor.PROPS_FILE_SYSTEM_PATH)
            + (String) sparkProps.get(SparkBatchUpdateProcessor.PROPS_FILE_NAME));
  }
}