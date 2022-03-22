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
package org.bimrocket.ihub.processors.kafka;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bimrocket.ihub.processors.Loader;
import org.bimrocket.ihub.util.ConfigProperty;

/**
 *
 * @author kfiertek-nexus-geographics
 */
public abstract class KafkaLoader extends Loader
{
  @ConfigProperty(name = "topicName",
    description = "Kafka topic name from which recover JsonObjects.")
  public String topicName;

  @ConfigProperty(name = "bootstrapAddress",
    description = "Kafka bootstrap servers address")
  public String bootstrapAddress;

  @ConfigProperty(name = "groupId",
    description = "Number of the kafka group to which the loader belongs.")
  public String groupId;

  @ConfigProperty(name = "offsetReset",
    description = "On which offset start to load records [earliest,latest]")
  public String offsetReset = "latest";

  @ConfigProperty(name = "autoCommit",
    description = "Should group offset be commited to kafka db?")
  public boolean autoCommit = true;

  @ConfigProperty(name = "commitInterval",
    description = "Interval every x seconds to commit actual consumer group's offset consumed.")
  public int commitInterval = 5;

  protected KafkaConsumerRunnable runnableKafka;
  protected Thread threatRunner;

  @Override
  public void init() throws Exception
  {
    super.init();
    var props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
      commitInterval * 1000);
    runnableKafka = new KafkaConsumerRunnable(getConnector(), topicName, props);
    threatRunner = new Thread(runnableKafka);
    threatRunner.start();
  }

  protected String getRecord()
  {
    return this.runnableKafka.getRecord();
  }

  @Override
  public void end()
  {
    super.end();
    runnableKafka.shutdown();
    threatRunner.interrupt();
    runnableKafka = null;
    threatRunner = null;
  }
}
