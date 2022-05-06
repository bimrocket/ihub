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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.bimrocket.ihub.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfiertek-nexus-geographics
 *
 */
public class KafkaConsumerRunnable implements Runnable
{
  private static final Logger log =
    LoggerFactory.getLogger(KafkaConsumerRunnable.class);

  private final KafkaConsumer<String, String> consumer;
  private final String topic;
  private final Connector con;
  private boolean end = false;
  private Queue<String> records = new LinkedList<>();
  private boolean insertLock = false;

  public KafkaConsumerRunnable(Connector con, String topic, Properties props)
  {
    this.con = con;
    this.topic = topic;
    this.consumer = new KafkaConsumer<>(props);
  }

  @Override
  public void run()
  {
    try
    {
      log.debug("subscribing to topic {}",
        this.topic);
      consumer.subscribe(new ArrayList<>(Arrays.asList(this.topic)));

      while (con.isStarted() && !end)
      {
        ConsumerRecords<String, String> records = consumer
          .poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records)
        {
          while (insertLock)
          {
            Thread.sleep(1000);
          }
          log.debug("adding record");
          this.records.add(record.value());
        }
      }
    }
    catch (WakeupException | InterruptedException e)
    {
      // ignore for shutdown
    }
    finally
    {
      try
      {
        consumer.close();
      }
      catch (Exception e)
      {

      }
    }
  }

  public void shutdown()
  {
    end = true;
    consumer.wakeup();
  }

  public String getRecord()
  {
    return this.records.poll();
  }

  public String[] getRecords()
  {
    insertLock = true;
    String[] toReturn = this.records.toArray(new String[this.records.size()]);
    this.records.clear();
    insertLock = false;
    return toReturn;
  }
}
