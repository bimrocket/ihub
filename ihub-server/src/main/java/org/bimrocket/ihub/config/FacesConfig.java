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
package org.bimrocket.ihub.config;

import com.sun.faces.config.ConfigureListener;
import java.util.Arrays;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

/**
 *
 * @author realor
 */
@Configuration
public class FacesConfig implements ServletContextAware
{
  @Bean
  ServletRegistrationBean<FacesServlet> jsfServletRegistration(
    ServletContext servletContext)
  {
    ServletRegistrationBean<FacesServlet> srb = new ServletRegistrationBean<>();
    srb.setServlet(new FacesServlet());
    srb.setUrlMappings(Arrays.asList("*.xhtml"));
    srb.setLoadOnStartup(1);
    return srb;
  }

  @Bean
  public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener()
  {
    ConfigureListener configureListener = new ConfigureListener();
    return new ServletListenerRegistrationBean<>(configureListener);
  }

  @Override
  public void setServletContext(ServletContext servletContext)
  {
    servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS",
      Boolean.TRUE.toString());

    servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD",
      "client");

    servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration",
      Boolean.TRUE.toString());
  }
}
