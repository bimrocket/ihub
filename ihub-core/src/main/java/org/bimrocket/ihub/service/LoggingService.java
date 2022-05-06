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
package org.bimrocket.ihub.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 *
 * @author realor
 */
@Service
public class LoggingService
{
  private Level level = Level.DEBUG;
  private final LinkedList<ILoggingEvent> events = new LinkedList<>();

  @Value("${logging.max_events}")
  private int maxEvents;

  @PostConstruct
  public void init()
  {
    LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();

    ch.qos.logback.classic.Logger logger = lc.getLogger(ROOT_LOGGER_NAME);

    LogAppender logAppender = new LogAppender();
    logAppender.setContext(lc);
    logAppender.start();
    logger.addAppender(logAppender);
  }

  public void setLevel(Level level)
  {
    this.level = level;
  }

  public Level getLevel()
  {
    return level;
  }

  public int getMaxEvents()
  {
    return maxEvents;
  }

  public void setMaxEvents(int maxEvents)
  {
    this.maxEvents = maxEvents;
  }

  public List<ILoggingEvent> getEvents(ILoggingEventFilter filter,
    int maxEvents)
  {
    List<ILoggingEvent> filteredEvents = new ArrayList<>();

    synchronized (events)
    {
      Iterator<ILoggingEvent> iter = events.descendingIterator();
      while (iter.hasNext() && filteredEvents.size() < maxEvents)
      {
        ILoggingEvent event = iter.next();
        if (filter.accepts(event))
        {
          filteredEvents.add(event);
        }
      }
    }
    Collections.reverse(filteredEvents);

    return filteredEvents;
  }

  public class LogAppender extends AppenderBase<ILoggingEvent>
  {
    @Override
    protected void append(ILoggingEvent event)
    {
      Level eventLevel = event.getLevel();
      synchronized (events)
      {
        if (eventLevel.isGreaterOrEqual(level))
        {
          events.add(event);
          if (events.size() > maxEvents)
          {
            events.poll();
          }
        }
      }
    }
  }

  public static interface ILoggingEventFilter
  {
    boolean accepts(ILoggingEvent event);
  }
}
