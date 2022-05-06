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

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author realor
 */
@Service
public class InfoService
{
  private Properties gitProperties;

  public String getUpTime()
  {
    RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

    long uptime = rb.getUptime();
    long totalSeconds = uptime / 1000;
    long totalMinutes = totalSeconds / 60;
    long totalHours = totalMinutes / 60;

    long days = totalHours / 24;
    long hours = totalHours % 24;
    long minutes = totalMinutes % 60;
    long seconds = totalSeconds % 60;

    StringBuilder buffer = new StringBuilder();
    if (days > 0) buffer.append(days).append("d");
    if (hours > 0) buffer.append(" ").append(hours).append("h");
    if (minutes > 0) buffer.append(" ").append(minutes).append("m");
    if (seconds > 0) buffer.append(" ").append(seconds).append("s");

    return buffer.toString();
  }

  public Date getStartDate()
  {
    RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

    long startTime = rb.getStartTime();

    return new Date(startTime);
  }

  public String getVersion()
  {
    String version = getGitProperty("git.build.version");
    String tag = getGitProperty("git.closest.tag.name");
    String commitCount = getGitProperty("git.total.commit.count");

    if (!StringUtils.isBlank(tag)) version = tag;

    version += "-r" + commitCount;

    return version;
  }

  public String getBuildTime()
  {
    return getGitProperty("git.build.time");
  }

  public String getGitProperty(String name)
  {
    if (gitProperties == null)
    {
      loadGitProperties();
    }
    return gitProperties.getProperty(name);
  }

  private synchronized void loadGitProperties()
  {
    if (gitProperties != null) return;

    gitProperties = new Properties();

    ClassLoader classLoader = InfoService.class.getClassLoader();
    try (InputStream is = classLoader.getResourceAsStream("git.properties"))
    {
      gitProperties.load(is);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }
}
