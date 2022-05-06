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
package org.bimrocket.ihub.processors.gemweb;

import org.bimrocket.ihub.processors.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bimrocket.ihub.util.ConfigProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfiertek-nexus-geographics
 * @author realor
 *
 */
public class GemwebLoader extends FullScanLoader
{
  private static final Logger log =
    LoggerFactory.getLogger(GemwebLoader.class);

  public static final String AUTH_BEARER = "Bearer";

  @ConfigProperty(name = "url",
    description = "Gemweb api url")
  public String url = "https://api.gemweb.es";

  @ConfigProperty(name = "clientId",
    description = "Used to obtain token")
  public String clientId;

  @ConfigProperty(name = "clientSecret",
    description = "Secret used for authentication",
    secret = true)
  public String clientSecret;

  @ConfigProperty(name = "category",
    description = "Category to load from gemweb")
  public String category = "subministraments";

  @ConfigProperty(name = "authentication",
    description = "Authentication used currently supported only Bearer")
  public String auth = AUTH_BEARER;

  @ConfigProperty(name = "timeout",
    description = "Timeout for uri request in seconds")
  public Integer timeoutSec = 60;

  @ConfigProperty(name = "recordsPath",
    description = "Path to records inside json response")
  public String recordsPath = "/subministrament";

  @Override
  protected Iterator<JsonNode> fullScan()
  {
    log.debug("init with timeout '{}'", timeoutSec);

    try
    {
      log.debug("execute httpClient built");
      var httpPost = new HttpPost(url);
      String accessToken = getAccessToken();
      if (accessToken == null || accessToken.isBlank())
      {
        log.error("access token is empty {} returning empty iterator",
          accessToken);
        return Collections.emptyIterator();
      }
      String authHeader = this.getAuthHeader(accessToken);
      List<NameValuePair> nvps = new ArrayList<>();
      nvps.add(new BasicNameValuePair("request", "get_inventory"));
      nvps.add(new BasicNameValuePair("access_token", accessToken));
      nvps.add(new BasicNameValuePair("category", category));

      httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
      httpPost.setHeader("Accept", "application/xml");
      httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
      httpPost.setHeader("Authorization", authHeader);
      HttpResponse resp = buildHttpClient().execute(httpPost);

      try (InputStream content = resp.getEntity().getContent())
      {
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode rootNode = xmlMapper.readTree(content);

        JsonNode all = rootNode.at(recordsPath);
        if (all.isArray())
        {
          return ((ArrayNode)all).iterator();
        }
        else
        {
          log.error("all is not a ArrayNode, all::{}", all.toPrettyString());
          return Collections.emptyIterator();
        }
      }
    }
    catch (ConnectTimeoutException | SocketTimeoutException ex)
    {
      log.error("timeout while sending petition : ", ex);
    }
    catch (IOException ex)
    {
      log.error("exception while sending petition : ", ex);
    }
    return Collections.emptyIterator();
  }

  @Override
  protected String getLocalId(JsonNode localObject)
  {
    return localObject.get("id").asText();
  }

  private String getAccessToken()
  {
    try
    {
      var httpPost = new HttpPost(url);

      List<NameValuePair> nvps = new ArrayList<>();
      nvps.add(new BasicNameValuePair("request", "get_token"));
      nvps.add(new BasicNameValuePair("client_id", clientId));
      nvps.add(new BasicNameValuePair("client_secret", clientSecret));
      nvps.add(new BasicNameValuePair("grant_type", "client_credentials"));

      httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
      httpPost.setHeader("Accept", "application/xml");
      httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

      HttpResponse resp = buildHttpClient().execute(httpPost);

      XmlMapper xmlMapper = new XmlMapper();

      try (InputStream content = resp.getEntity().getContent())
      {
        JsonNode node = xmlMapper.readTree(content);

        if (node.get("error") != null)
        {
          log.error("error in authentication, error responded : {}",
            node.get("resultat").get("error"));
        }
        JsonNode token = node.get("access_token");
        if (token == null || token.isNull())
        {
          log.error("no access token");

          return null;
        }

        String accessToken = token.asText();

        log.info("got access token: {}", accessToken);

        return accessToken;
      }
    }
    catch (IOException e)
    {
      log.error("exception in getting accessToken", e);
      return null;
    }
  }

  private HttpClient buildHttpClient()
  {
    int timeoutMs = timeoutSec * 1000;
    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeoutMs);
    requestBuilder.setConnectionRequestTimeout(timeoutMs);

    HttpClientBuilder clientBuilder = HttpClients.custom();
    clientBuilder.setDefaultRequestConfig(requestBuilder.build());
    return clientBuilder.build();
  }

  private String getAuthHeader(String accessToken)
  {
    String authHeader = "";
    if (auth != null && auth.equals(AUTH_BEARER))
    {
      authHeader = AUTH_BEARER + " " + accessToken;
    }
    return authHeader;
  }
}
