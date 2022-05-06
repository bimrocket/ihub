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
package org.bimrocket.ihub.processors.wfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.processors.Sender;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.GeometryUtils;
import org.xembly.Directives;
import org.xembly.Xembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfiertek-nexus-geographics
 */
public class WfsSender extends Sender
{
  private static final Logger log = LoggerFactory.getLogger(WfsSender.class);

  @ConfigProperty(name = "url",
    description = "Url to wfs server")
  public String url;

  @ConfigProperty(name = "layer",
    description = "Layer to update")
  public String layer;

  @ConfigProperty(name = "namespace",
    description = "XML Namespace to update")
  public String namespace;

  @ConfigProperty(name = "namespaceUrl",
    description = "XML URI Namespace to update")
  public String namespaceURL;

  @ConfigProperty(name = "typeName",
    description = "Wfs entity to update")
  public String typeName;

  @ConfigProperty(name = "authentication",
    description = "Authentication used currently supported only Basic")
  public String auth;

  @ConfigProperty(name = "username",
    description = "User used for basic authentication")
  public String username;

  @ConfigProperty(name = "password",
    description = "Password used for basic authentication",
    secret = true)
  public String password;

  @ConfigProperty(name = "timeout",
    description = "Timeout used to sending to geoserver (if it exceeds timeout then it retries by number of retries set")
  public Integer timeout;

  @Override
  public boolean processObject(ProcessedObject node)
  {
    return send(node);
  }

  public boolean send(ProcessedObject procObject)
  {
    boolean insert = procObject.isInsert();
    boolean update = procObject.isUpdate();
    boolean delete = procObject.isDelete();
    JsonNode node = procObject.getLocalObject();

    if (node == null)
    {
      return false;
    }

    Directives dir = new Directives();
    dir.add("Transaction").attr("xmlns", "http://www.opengis.net/wfs")
      .attr("service", "WFS").attr("version", "1.0.0")
      .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
      .attr("xsi:schemaLocation",
        "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
    if (insert)
    {
      JsonNode elem = node;
      log.debug("creating xml insert for layer::{}", layer);
      dir.add("Insert").add(layer);
      dir.attr("xmlns:" + namespace, namespaceURL);
      Iterator<String> keys = elem.fieldNames();
      while (keys.hasNext())
      {
        String key = keys.next();
        JsonNode nodeKey = elem.get(key);
        dir.add(key);
        insertNodeIntoXML(dir, nodeKey);
        dir.up();
      }
    }
    else if (update)
    {
      if (procObject.getLocalId() == null
        || procObject.getLocalId().isBlank())
      {
        log.error(
          "Processed object is update and has no local id defined "
          + "| ProcessedObject:: ",
          procObject.toString());
        procObject.setObjectType(ProcessedObject.IGNORE);
        return false;
      }
      JsonNode elem = node.get("element");
      log.debug("adding update into layer {}", layer);
      dir.add("Update").attr("typeName", typeName);
      dir.attr("xmlns:" + namespace, namespaceURL);
      Iterator<String> keys = elem.fieldNames();
      while (keys.hasNext())
      {
        String key = keys.next();
        JsonNode nodeKey = elem.get(key);
        dir.add("Property");
        dir.add(key);
        insertNodeIntoXML(dir, nodeKey);
        dir.up();
        dir.up();
      }
      dir.add("Filter").attr("xmlns", "http://www.opengis.net/ogc");
      dir.add("FeatureId").attr("fid", procObject.getLocalId());
    }
    else
    {
      if (procObject.getLocalId() == null
        || procObject.getLocalId().isBlank())
      {
        log.error(
          "Processed object is delete and has no local id defined "
          + "| ProcessedObject:: ",
          procObject.toString());
        procObject.setObjectType(ProcessedObject.IGNORE);
        return false;
      }
      log.debug("adding delete of layer ", layer);
      dir.add("Delete").attr("typeName", typeName);
      dir.attr("xmlns:" + namespace, namespaceURL);
      dir.add("Filter").attr("xmlns", "http://www.opengis.net/ogc");
      dir.add("FeatureId").attr("fid", procObject.getLocalId());
    }
    try
    {
      String xml = new Xembler(dir).xml().replaceAll("\r\n", "");
      log.trace("xml which will be sent to WFS Server :: {}", xml);
      String uri = url;
      String bodyResp = sendPostXML(uri, xml);

      if (insert)
      {
        try
        {
          Matcher m = Pattern.compile("fid=\"[A-z_.0-9]+\"")
            .matcher(bodyResp);
          String localId = null;
          while (m.find())
          {
            localId = m.group(0).replaceAll("fid=", "")
              .replaceAll("\"", "");
          }
          if (localId != null)
          {
            procObject.setLocalId(localId);
          }
          else
          {
            log.error("WFS Server didn't return a valid XML response for"
              + " insert, local id not found in response::{}", bodyResp);
            return false;
          }
        }
        catch (Exception e)
        {
          log.error("WFS Server didn't return a valid XML response an"
            + " exception has occurred", e);
          return false;
        }
      }
      else if (update)
      {
        Matcher m = Pattern
          .compile("<wfs:totalUpdated>1</wfs:totalUpdated>")
          .matcher(bodyResp);
        if (!m.find())
        {
          log.error("WFS Server didn't return a valid XML response for"
            + " update, object with localId (Id in WFS Server)::{} "
            + "wasn't updated", procObject.getLocalId());
          return false;
        }
      }
      else if (delete)
      {
        Matcher m = Pattern
          .compile("<wfs:totalDeleted>1</wfs:totalDeleted>")
          .matcher(bodyResp);

        if (!m.find())
        {

          log.error(
            "couldn't do delete transaction, response from geoserver :: {}"
            + ", xml sent :: {}",
            bodyResp, xml);
          return false;
        }
      }
    }
    catch (Exception e)
    {
      log.error("exception while creating xml", e);
      return false;
    }
    return true;
  }

  private boolean insertNodeIntoXML(Directives xml, JsonNode node)
  {
    if (GeometryUtils.validGeojson(node))
    {
      log.debug("adding geom to xml");
      if (node != null && !node.isNull() && node.isObject())
      {
        GeometryUtils.putGeometryObjInXML(xml, node);
      }
      else
      {
        xml.set("");
        log.error("couldn't put geom object into xml jsonNode::{}",
          node);
      }
    }
    else
    {
      log.debug("adding object/text/numeric to xml");
      if (node != null && !node.isNull())
      {
        try
        {
          if (node.isObject())
          {
            xml.set(node.toString());
          }
          else
          {
            xml.set(node.asText());
          }
        }
        catch (Exception e)
        {
          xml.set("");
        }
      }
      else
      {
        xml.set("");
      }
    }
    return true;
  }

  /**
   * Send POST Request
   *
   * @param uri Thirdparty server
   * @param xml XML to send
   * @return XML response
   * @throws Exception
   */
  private String sendPostXML(String uri, String xml)
    throws Exception
  {
    try
    {
      log.debug("init with uri '{}' xml '{}' and timeout '{}'", uri, xml,
        timeout);

      int connectTimeout = timeout * 1000;
      int socketTimeout = timeout * 1000;

      HttpPost httpPost = new HttpPost(uri);
      RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(connectTimeout)
        .setSocketTimeout(socketTimeout).build();
      String authType = auth;
      String authHeader = "";
      if (authType != null && authType.equals("Basic"))
      {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
          auth.getBytes(StandardCharsets.ISO_8859_1));
        authHeader = "Basic " + new String(encodedAuth);
      }
      httpPost.setConfig(requestConfig);
      httpPost.setEntity(new StringEntity(xml, StandardCharsets.UTF_8));
      httpPost.setHeader("Accept", "application/xml");
      httpPost.setHeader("Content-type", "application/xml");
      httpPost.setHeader("Authorization", authHeader);
      HttpResponse resp = HttpClientBuilder.create().build()
        .execute(httpPost);
      StringBuilder sb = new StringBuilder();

      try (InputStreamReader inputStreamReader = new InputStreamReader(
        resp.getEntity().getContent()))
      {
        BufferedReader br = new BufferedReader(inputStreamReader);
        String readLine;
        while (((readLine = br.readLine()) != null))
        {
          sb.append("\n").append(readLine);
        }
      }

      String ret = sb.toString();

      if (log.isDebugEnabled())
      {
        log.debug("response '{}'", ret);
      }
      return ret;
    }
    catch (SocketTimeoutException e)
    {
      log.error(
        "Timeout sending xml to '{}' readTimeout '{}' with xml '{}'",
        uri, timeout, xml);
      throw e;
    }
    catch (Exception e)
    {
      log.error("Error sending xml to '{}' with xml '{}'.", uri, xml, e);
      throw e;
    }
  }
}
