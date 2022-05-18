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
import org.bimrocket.ihub.processors.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author kfiertek-nexus-geographics
 * @author realor
 */
public abstract class KafkaSender extends Sender
{
  private static final Logger log =
    LoggerFactory.getLogger(JsonKafkaSender.class);

  @ConfigProperty(name = "topic",
    description = "Kafka topic name to which send String.")
  public String topic;

  @ConfigProperty(name = "bootstrapAddress",
    description = "Kafka bootstrap servers address")
  public String bootstrapAddress = "localhost:9092";

  protected KafkaTemplate<String, String> template;

  @Override
  public void init() throws Exception
  {
    super.init();
    template = new KafkaTemplate<>(
      new DefaultKafkaProducerFactory<>(producerConfigs()));
  }

  private Map<String, Object> producerConfigs()
  {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class);
    // See https://kafka.apache.org/documentation/#producerconfigs for more
    // properties
    return props;
  }

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    JsonNode globalObject = procObject.getGlobalObject();
    if (globalObject == null || procObject.isIgnore())
    {
      return false;
    }

    var value = formatObject(globalObject);

    log.debug("sending {} json object to topic {}",
      globalObject.toPrettyString(), this.topic);

    this.template.send(this.topic, value);

    return true;
  }

  protected abstract String formatObject(JsonNode globalObject);
}
