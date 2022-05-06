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
package org.bimrocket.ihub.web;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Date;
import java.util.List;
import org.bimrocket.ihub.service.LoggingService;
import org.bimrocket.ihub.service.LoggingService.ILoggingEventFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author realor
 */
@Component
@Scope("session")
public class LogsBean
{
  @Autowired
  LoggingService loggingService;

  private boolean autoRefresh;
  private LogFilter filter = new LogFilter();

  public boolean isAutoRefresh()
  {
    return autoRefresh;
  }

  public void setAutoRefresh(boolean autoRefresh)
  {
    this.autoRefresh = autoRefresh;
  }

  public Date getDate(ILoggingEvent event)
  {
    return new Date(event.getTimeStamp());
  }

  public String getLevel(ILoggingEvent event)
  {
    return event.getLevel().toString();
  }

  public List<ILoggingEvent> getEvents()
  {
    return loggingService.getEvents(filter, 50);
  }

  public LogFilter getFilter()
  {
    return filter;
  }

  public void setFilter(LogFilter filter)
  {
    this.filter = filter;
  }

  public static class LogFilter implements ILoggingEventFilter
  {
    String level = "DEBUG";
    String message;
    String threadName;

    public String getLevel()
    {
      return level;
    }

    public void setLevel(String level)
    {
      this.level = level;
    }

    public String getMessage()
    {
      return message;
    }

    public void setMessage(String message)
    {
      this.message = message;
    }

    public String getThreadName()
    {
      return threadName;
    }

    public void setThreadName(String threadName)
    {
      this.threadName = threadName;
    }

    @Override
    public boolean accepts(ILoggingEvent event)
    {
      if (!event.getLevel().isGreaterOrEqual(Level.toLevel(level)))
        return false;

      if (threadName != null
          && !event.getThreadName().contains(threadName.trim()))
        return false;

      if (message != null
          && !event.getFormattedMessage().contains(message.trim()))
        return false;

      return true;
    }
  }
}
