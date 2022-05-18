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
package org.bimrocket.ihub.connector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.service.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bimrocket.ihub.exceptions.NotFoundException;
import org.bimrocket.ihub.exceptions.ProcessorInitException;
import org.springframework.scheduling.support.CronExpression;
import static org.bimrocket.ihub.connector.ProcessedObject.DELETE;
import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;
import static org.bimrocket.ihub.connector.ProcessedObject.UPDATE;

/**
 *
 * @author realor
 */
public class Connector implements Runnable
{
  private static final Logger log =
    LoggerFactory.getLogger(Connector.class);

  public static final String THREAD_PREFIX = "c:";

  protected ConnectorService service;

  protected String name;

  protected String description;

  protected String inventory;

  protected List<Processor> processors = new ArrayList<>();

  protected boolean end;

  protected long waitMillis = 10000;

  protected String crontab = null;

  protected Thread thread;

  protected TimerTask task;

  protected boolean debugEnabled = false;

  protected boolean singleRun = false;

  protected boolean autoStart = false;

  protected Date startDate;

  protected Date endDate;

  protected Date changeDate;

  protected Date nextExecutionDate;

  protected int processed;

  protected int ignored;

  protected int inserted;

  protected int updated;

  protected int deleted;

  protected Exception lastError;

  private boolean unsaved = false;

  private final ProcessedObject procObject = new ProcessedObject();

  public Connector(ConnectorService service, String name)
  {
    this.service = service;
    this.name = name;
    this.changeDate = new Date();
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public Connector setDescription(String description)
  {
    this.description = description;
    return this;
  }

  public String getInventory()
  {
    return inventory;
  }

  public Connector setInventory(String inventory)
  {
    this.inventory = inventory;
    return this;
  }

  public ConnectorService getConnectorService()
  {
    return service;
  }

  public long getWaitMillis()
  {
    return waitMillis;
  }

  public Connector setWaitMillis(long waitMillis)
  {
    this.waitMillis = waitMillis;
    return this;
  }

  public String getCrontab()
  {
    return crontab;
  }

  public Connector setCrontab(String crontab)
  {
    this.crontab = crontab;
    return this;
  }

  public boolean isSingleRun()
  {
    return singleRun;
  }

  public Connector setSingleRun(boolean singleRun)
  {
    this.singleRun = singleRun;
    return this;
  }

  public boolean isAutoStart()
  {
    return autoStart;
  }

  public Connector setAutoStart(boolean autoStart)
  {
    this.autoStart = autoStart;
    return this;
  }

  public boolean isUnsaved()
  {
    return unsaved;
  }

  public void setUnsaved(boolean unsaved)
  {
    this.unsaved = unsaved;
  }

  public boolean isStarted()
  {
    return thread != null || task != null;
  }

  public boolean isRunning()
  {
    return thread != null;
  }

  public boolean isCrontab()
  {
    return !StringUtils.isBlank(crontab);
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public Date getChangeDate()
  {
    return changeDate;
  }

  public Date getNextExecutionDate()
  {
    return nextExecutionDate;
  }

  public int getProcessed()
  {
    return processed;
  }

  public int getIgnored()
  {
    return ignored;
  }

  public int getInserted()
  {
    return inserted;
  }

  public int getUpdated()
  {
    return updated;
  }

  public int getDeleted()
  {
    return deleted;
  }

  public Exception getLastError()
  {
    return lastError;
  }

  public synchronized int getProcessorCount()
  {
    return processors.size();
  }

  public synchronized List<Processor> getProcessors()
  {
    // avoid concurrent modification exceptions iterating over processor list

    return new ArrayList<>(processors);
  }

  public synchronized Connector setProcessors(List<Processor> processors)
  {
    // some changes will not take effect until connector restart

    processors.forEach(processor -> processor.setConnector(this));

    this.processors.clear();
    this.processors.addAll(processors);

    unsaved = true;

    changeDate = new Date();

    return this;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    return buffer.append("Connector")
      .append("{ name: \"")
      .append(name)
      .append("\", description: \"")
      .append(description == null ? "" : description)
      .append("\", inventory: \"")
      .append(inventory)
      .append("\", started: ")
      .append(isStarted())
      .append(", autoStart: ")
      .append(autoStart)
      .append(", singleRun: ")
      .append(singleRun)
      .append(", debug: ")
      .append(debugEnabled)
      .append(", processors: ")
      .append(processors)
      .append(" }")
      .toString();
  }

  @Override
  public void run()
  {
    log.info("{} connector execution started.", name);
    startDate = new Date();
    changeDate = startDate;
    lastError = null;
    nextExecutionDate = null;
    end = false;
    resetStatistics();

    List<Processor> runningProcessors = getProcessors();

    try
    {
      initProcessors(runningProcessors);

      while (!end)
      {
        procObject.reset();

        int processorCount = 0;
        for (var processor : runningProcessors)
        {
          if (processor.isEnabled())
          {
            log.debug("Executing processor {}",
              processor.getClass().getName());

            if (processor.processObject(procObject))
            {
              processorCount++;
            }
            else break;
          }
        }

        if (!procObject.isIgnore())
        {
          updateIdPairRepository(procObject);
          updateStatistics(procObject);
          log.debug("Object processed, type: {}, operation: {}, "
            + "localId: {}, globalId: {}",
            procObject.getObjectType(), procObject.getOperation(),
            procObject.getLocalId(), procObject.getGlobalId());
        }

        if (processorCount == 0)
        {
          // no more data to process
          if (isCrontab() || singleRun)
          {
            end = true;
          }
          else
          {
            waitForNextExecution();
          }
        }
      }
    }
    catch (ProcessorInitException ex)
    {
      lastError = ex;
      log.error(ex.getMessage());
    }
    catch (Exception ex)
    {
      lastError = ex;
      log.error("An error has ocurred: {}", ex.toString());
    }
    finally
    {
      endProcessors(runningProcessors);
    }

    synchronized (this)
    {
      if (task != null)
      {
        if (isCrontab() && lastError == null && !singleRun)
        {
          task = scheduleTask();
        }
        else
        {
          task = null;
        }
      }
    }

    endDate = new Date();
    changeDate = endDate;
    log.info("{} connector execution finished.", name);
    thread = null;
  }

  public synchronized Connector start()
  {
    if (thread == null && task == null)
    {
      log.info("Start connector {}.", name);

      changeDate = new Date();

      if (isCrontab())
      {
        task = scheduleTask();
      }
      else
      {
        nextExecutionDate = null;
        startThread();
      }
    }
    return this;
  }

  public synchronized Connector stop()
  {
    if (task != null || thread != null)
    {
      log.info("Stop connector {}.", name);

      changeDate = new Date();

      if (task != null)
      {
        task.cancel();
        task = null;
        nextExecutionDate = null;
      }

      if (thread != null)
      {
        end = true;
        notify();
      }
    }
    return this;
  }

  public Connector save()
  {
    saveSetup();

    return this;
  }

  public ConnectorSetup saveSetup()
  {
    ConnectorSetup connSetup = service.getConnectorMapperService()
      .getConnectorSetup(this);

    service.getConnectorSetupRepository().save(connSetup);

    unsaved = false;

    log.info("Connector {} saved.", name);

    return connSetup;
  }

  public Connector restore() throws Exception
  {
    restoreSetup();

    log.info("Connector {} restored.", name);

    return this;
  }

  public ConnectorSetup restoreSetup() throws Exception
  {
    Optional<ConnectorSetup> optConnSetup = service
      .getConnectorSetupRepository().findById(name);
    if (optConnSetup.isPresent())
    {
      ConnectorSetup connSetup = optConnSetup.get();
      service.getConnectorMapperService().setConnectorSetup(this, connSetup,
        true);

      unsaved = false;
      lastError = null;
      return connSetup;
    }
    throw new NotFoundException(238, "Connector %s not found", name);
  }

  protected void initProcessors(List<Processor> processors)
    throws ProcessorInitException
  {
    int initialized = 0;
    for (var processor : processors)
    {
      String processorName = processor.getClass().getName();
      try
      {
        processor.init();
        log.debug("Processor #{}: {} initialized.", initialized, processorName);
        initialized++;
      }
      catch (Exception ex)
      {
        log.debug("Failed to initialize processor #{}: {}: {}", initialized,
          processorName, ex.toString());
        lastError = ex;
        break;
      }
    }

    if (lastError != null)
    {
      // remove from list not initialized processors
      while (processors.size() > initialized)
      {
        processors.remove(processors.size() - 1); // remove last
      }
      throw new ProcessorInitException(427,
        "Failed to initialize connector %s: %s", name,
        lastError.getMessage());
    }
  }

  public void endProcessors(List<Processor> processors)
  {
    int ended = 0;
    for (var processor : processors)
    {
      String processorName = processor.getClass().getName();
      try
      {
        processor.end();
        log.debug("Processor #{}: {} ended.", ended, processorName);
        ended++;
      }
      catch (Exception ex)
      {
        log.debug("Failed to end processor #{}: {}: {}", ended,
          processorName, ex.toString());

        if (lastError == null) lastError = ex;
      }
    }
  }

  void updateIdPairRepository(ProcessedObject procObject)
  {
    IdPairRepository idPairRepository = service.getIdPairRepository();

    Optional<IdPair> result = idPairRepository.
      findByInventoryAndObjectTypeAndLocalId(inventory,
        procObject.getObjectType(), procObject.getLocalId());

    if (procObject.isDelete())
    {
      if (result.isPresent())
      {
        IdPair idPair = result.get();
        idPairRepository.delete(idPair);
      }
    }
    else // insert or update
    {
      IdPair idPair;
      if (result.isPresent())
      {
        idPair = result.get();
      }
      else
      {
        idPair = new IdPair();
        idPair.setId(UUID.randomUUID().toString());
      }

      idPair.setInventory(inventory);
      idPair.setObjectType(procObject.getObjectType());
      idPair.setLocalId(procObject.getLocalId());
      idPair.setGlobalId(procObject.getGlobalId());
      idPair.setLastUpdate(new Date());
      idPair.setConnectorName(name);
      idPairRepository.save(idPair);
    }
  }

  void resetStatistics()
  {
    processed = 0;
    ignored = 0;
    inserted = 0;
    updated = 0;
    deleted = 0;
  }

  void updateStatistics(ProcessedObject procObject)
  {
    processed++;
    switch (procObject.getOperation())
    {
      case INSERT:
        inserted++;
        break;
      case UPDATE:
        updated++;
        break;
      case DELETE:
        deleted++;
        break;
      default:
        ignored++;
    }
  }

  void startThread()
  {
    thread = new Thread(this, THREAD_PREFIX + name);
    thread.start();
  }

  TimerTask scheduleTask()
  {
    lastError = null;

    CronExpression cronExpression;
    try
    {
      cronExpression = CronExpression.parse(crontab);
    }
    catch (Exception ex)
    {
      log.error("Invalid crontab expression in {} connector: {} ",
        name, crontab);

      lastError = ex;

      return null;
    }

    LocalDateTime next = cronExpression.next(LocalDateTime.now());

    if (next == null) return null;

    Instant instant = next.atZone(ZoneId.systemDefault()).toInstant();
    nextExecutionDate = Date.from(instant);

    TimerTask nextTask = new TimerTask()
    {
      @Override
      public void run()
      {
        startThread();
      }
    };
    log.info("Scheduling {} connector execution for {}", name, nextExecutionDate);
    getConnectorService().getTimer().schedule(nextTask, nextExecutionDate);

    return nextTask;
  }

  synchronized void waitForNextExecution()
  {
    try
    {
      if (!end && waitMillis > 0)
      {
        wait(waitMillis);
      }
    }
    catch (InterruptedException ex)
    {
    }
  }
}
