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
package org.bimrocket.ihub.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig
{
  private static SparkSession session;

  private static boolean enabled;
  private static String sparkUrl;
  private static String sparkTimezone;
  private static String sparkDriverHost;
  private static String sparkDriverPort;
  private static String sparkBlockManagerPort;
  private static String sparkDriverMemory;
  private static String sparkDeployMode;
  private static String sparkExecutorCores;
  private static String sparkExecutorMemory;
  
  public static SparkSession buildContext() throws Exception {
    if (SparkConfig.session != null) {
      return SparkConfig.getSession();
    }
    if (!SparkConfig.enabled) {
      throw new Exception("Spark not enabled");
    }
    SparkConfig.session = SparkSession.builder()
        .master(sparkUrl)
        .config("spark.sql.session.timezone",sparkTimezone)
        .config("spark.blockManager.port", sparkBlockManagerPort)
        .config("spark.driver.port", sparkDriverPort)
        .config("spark.driver.host", sparkDriverHost)
        .config("spark.driver.memory", sparkDriverMemory)
        .config("spark.driver.bindAddress", "0.0.0.0")
        .config("spark.submit.deployMode", sparkDeployMode)
        .config("spark.executor.core",sparkExecutorCores)
        .config("spark.executor.memory", sparkExecutorMemory)
        .getOrCreate();
    return SparkConfig.getSession();
  }
  
  public static SparkSession getSession() {
    return SparkConfig.session;
  }
  
  @Value("${ihub.spark.enabled}")
  public void setEnabled(String enabled) {
    SparkConfig.enabled = Boolean.valueOf(enabled);
  };
  
  @Value("${ihub.spark.url}")
  public void setUrl(String url) {
    SparkConfig.sparkUrl = url;
  };
  
  @Value("${ihub.spark.timezone}")
  public void setTimezone(String sparkTimezone) {
    SparkConfig.sparkTimezone = sparkTimezone;
  };
  
  @Value("${ihub.spark.driver.host}")
  public void setDriverHost(String sparkDriverHost) {
    SparkConfig.sparkDriverHost = sparkDriverHost;
  };

  @Value("${ihub.spark.driver.port}")
  public void setDriverPort(String sparkDriverPort) {
    SparkConfig.sparkDriverPort = sparkDriverPort;
  };
  
  @Value("${ihub.spark.blockmanager.port}")
  public void setBlockmanagerPort(String sparkBlockManagerPort) {
    SparkConfig.sparkBlockManagerPort = sparkBlockManagerPort;
  };
  
  @Value("${ihub.spark.driver.memory}")
  public void setDriverMemory(String sparkDriverMemory) {
    SparkConfig.sparkDriverMemory = sparkDriverMemory;
  };
  
  @Value("${ihub.spark.deploy.mode}")
  public void setDeployMode(String sparkDeployMode) {
    SparkConfig.sparkDeployMode = sparkDeployMode;
  };
  
  @Value("${ihub.spark.executor.cores}")
  public void setExecutorCores(String sparkExecutorCores) {
    SparkConfig.sparkExecutorCores = sparkExecutorCores;
  };
  
  @Value("${ihub.spark.executor.memory}")
  public void setExecutorMemory(String sparkExecutorMemory) {
    SparkConfig.sparkExecutorMemory = sparkExecutorMemory;
  };

  
}
