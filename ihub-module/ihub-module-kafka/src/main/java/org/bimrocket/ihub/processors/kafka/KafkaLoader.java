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

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.processors.Loader;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.bimrocket.ihub.connector.ProcessedObject.IGNORE;
import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;

/**
 *
 * @author kfiertek-nexus-geographics
 * @author realor
 */
public abstract class KafkaLoader extends Loader
{
  private static final Logger log =
    LoggerFactory.getLogger(KafkaLoader.class);

  @ConfigProperty(name = "topic",
    description = "Kafka topic from which recover JsonObjects.")
  public String topic;

  @ConfigProperty(name = "bootstrapAddress",
    description = "Kafka bootstrap servers address")
  public String bootstrapAddress = "localhost:9092";

  @ConfigProperty(name = "groudId",
    description = "Number of the kafka group to which the loader belongs.",
    required = false)
  public String groupId;

  @ConfigProperty(name = "offsetReset",
    description = "On which offset start to load records [earliest,latest]")
  public String offsetReset = "latest";

  @ConfigProperty(name = "objectType",
    description = "The object type to load")
  public String objectType;

  @ConfigProperty(name = "pollMillis",
    description = "The poll duration in millis")
  public long pollMillis = 1000;

  @ConfigProperty(name = "maxPollRecords",
    description = "The maximum number of records to get in a poll call")
  public int maxPollRecords = 100;

  private KafkaConsumer<String, String> consumer;
  private final Queue<ConsumerRecord<String, String>> bufferedRecords =
    new LinkedList<>();

  @Override
  public void init() throws Exception
  {
    super.init();

    if (groupId == null) groupId = getConnector().getName();

    var props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    if (topic == null) throw new Exception("topic not specified");

    consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(topic));
  }

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    boolean commitPending = !bufferedRecords.isEmpty();

    while (bufferedRecords.isEmpty())
    {
      ConsumerRecords<String, String> records =
        consumer.poll(Duration.ofMillis(pollMillis));

      log.debug("{} records read from kafka", records.count());

      if (records.count() == 0) break;

      commitPending = true;

      addRecordsToBuffer(records, bufferedRecords);
    }

    ConsumerRecord<String, String> record = bufferedRecords.poll();

    if (bufferedRecords.isEmpty() && commitPending)
    {
      // commit only when all records have been processed
      consumer.commitSync();
      log.debug("kafka consumer committed.");
    }

    if (record == null)
    {
      procObject.setObjectType(objectType);
      procObject.setOperation(IGNORE);
      return false;
    }
    else
    {
      JsonNode globalObject = parseObject(record.value());
      if (globalObject == null) return false;

      procObject.setGlobalObject(globalObject);
      procObject.setOperation(INSERT);
      procObject.setObjectType(objectType);
      return true;
    }
  }

  @Override
  public void end()
  {
    super.end();
    consumer.close();
  }

  protected void addRecordsToBuffer(ConsumerRecords<String, String> records,
    Queue<ConsumerRecord<String, String>> bufferedRecords)
  {
    String inventory = getConnector().getInventory();

    for (ConsumerRecord<String, String> record : records)
    {
      // ignore records generated by the same inventory
      for (Header header : record.headers())
      {
        if (header.key().equals("inventory"))
        {
          String recordInventory = new String(header.value());
          if (!inventory.equals(recordInventory))
          {
            bufferedRecords.add(record);
          }
          break;
        }
      }
    }
  }

  protected abstract JsonNode parseObject(String record);
}
