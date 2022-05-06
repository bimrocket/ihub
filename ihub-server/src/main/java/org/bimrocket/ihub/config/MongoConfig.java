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
package org.bimrocket.ihub.config;

import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author realor
 */
@Configuration
@ConditionalOnProperty(prefix = "data", name = "store", havingValue = "mongo")
public class MongoConfig extends AbstractMongoClientConfiguration
{
  private static final Logger log =
    LoggerFactory.getLogger(MongoConfig.class);

  @Value("${spring.data.mongodb.host}")
  String host;

  @Value("${spring.data.mongodb.port}")
  String port;

  @Value("${spring.data.mongodb.database}")
  String databaseName;

  @Override
  protected String getDatabaseName()
  {
    return databaseName;
  }

  @Override
  public MongoClient mongoClient()
  {
    String url = "mongodb://" + host + ":" + port + "/" + databaseName;
    log.info("mongodb connection string: {}", url);
    ConnectionString connectionString = new ConnectionString(url);

    MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
      .applyConnectionString(connectionString).build();

    return MongoClients.create(mongoClientSettings);
  }

  @Override
  public Collection<String> getMappingBasePackages()
  {
    return Collections.singleton("org.bimrocket.ihub");
  }

  @Autowired
  public void setMapKeyDotReplacement(MappingMongoConverter mongoConverter)
  {
    mongoConverter.setMapKeyDotReplacement("#");
  }
}
