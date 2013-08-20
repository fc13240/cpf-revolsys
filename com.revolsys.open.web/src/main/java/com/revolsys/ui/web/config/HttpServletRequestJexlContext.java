/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.web.config;

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl.JexlContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

public class HttpServletRequestJexlContext implements JexlContext {

  private final UrlPathHelper urlPathHelper = new UrlPathHelper();

  private final ServletContext servletContext;

  private final ThreadLocal<Map<String, Object>> localAttributes = new ThreadLocal<Map<String, Object>>();

  public HttpServletRequestJexlContext(final ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public void clearAttributes() {
    localAttributes.remove();
  }

  public Object getAttribute(final String key) {
    final Map<String, Object> attributes = getAttributes();
    return attributes.get(key);
  }

  private Map<String, Object> getAttributes() {
    Map<String, Object> attributes = localAttributes.get();
    if (attributes == null) {
      attributes = new HashMap<String, Object>();
      localAttributes.set(attributes);
    }
    return attributes;
  }

  private HttpServletRequest getRequest() {
    final ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    final HttpServletRequest request = requestAttributes.getRequest();
    return request;
  }

  public Map getVars() {
    return new AbstractMap<String, Object>() {
      @Override
      @SuppressWarnings("unchecked")
      public Set<Entry<String, Object>> entrySet() {
        final HttpServletRequest request = getRequest();
        final Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(request.getParameterMap());
        for (final Enumeration<String> names = request.getAttributeNames(); names.hasMoreElements();) {
          final String name = names.nextElement();
          map.put(name, request.getAttribute(name));
        }
        if (servletContext != null) {
          for (final Enumeration<String> names = servletContext.getAttributeNames(); names.hasMoreElements();) {
            final String name = names.nextElement();
            map.put(name, servletContext.getAttribute(name));
          }
        }
        final Map<String, Object> attributes = localAttributes.get();
        if (attributes != null) {
          map.putAll(attributes);
        }
        map.put("request", request);
        map.put("requestURI", urlPathHelper.getOriginatingRequestUri(request));
        return map.entrySet();
      }

      public Object get(final String key) {
        final HttpServletRequest request = getRequest();
        if (key.equals("request")) {
          return request;
        } else if (key.equals("requestURI")) {
          return urlPathHelper.getOriginatingRequestUri(request);
        }
        Object value = getAttribute(key);
        if (value == null && servletContext != null) {
          value = servletContext.getAttribute(key);
        }
        if (value == null) {
          value = request.getAttribute(key);
          if (value == null) {
            value = request.getParameter(key);
          }
        }
        if (value == null) {
          return "";
        } else {
          return value;
        }
      }
    };
  }

  public void setAttribute(final String key, final Object value) {
    final Map<String, Object> attributes = getAttributes();
    attributes.put(key, value);
  }

  public void setAttributes(final Map<String, ? extends Object> parameters) {
    getAttributes().putAll(parameters);
  }

  public void setVars(final Map arg0) {
  }
}
