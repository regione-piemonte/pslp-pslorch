/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;

public class ApiClient {

  private static final Logger LOGGER = Logger.getLogger(Constants.COMPONENT_NAME);

  protected Map<String, String> defaultHeaderMap = new HashMap<>();

  protected CloseableHttpClient httpClient;
  
  private String url;

  public ApiClient(String propertyName, int timeout) throws Exception {
    this.url = getUrl(propertyName);
    httpClient = buildHttpClient(timeout);
  }

  public String getUrl() {
    return url;
  }

  /**
   * Escape the given string to be used as URL query value.
   * 
   * @param str
   *          String
   * @return Escaped string
   */
  public String escapeString(String str) {
    try {
      return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }

  /**
   * Invoke API by sending HTTP request with the given options.
   *
   * @param <T>
   *          Type
   * @param path
   *          The sub-path of the HTTP URL
   * @param method
   *          The request method, one of "GET", "POST", "PUT", "HEAD" and
   *          "DELETE"
   * @param queryParams
   *          The query parameters
   * @param body
   *          The request body object as string
   * @param headerParams
   *          The header parameters
   * @param formParams
   *          The form parameters
   * @param accept
   *          The request's Accept header
   * @param contentType
   *          The request's Content-Type header
   * @param authNames
   *          The authentications to apply
   * @param returnType
   *          The return type into which to deserialize the response
   * @return The response body in type of string
   * @throws ApiException
   *           API exception
   */
  public ApiResponse invokeAPI(String path, String method, List<Pair> queryParams, String body, Map<String, String> headerParams, String accept, String contentType) throws ApiException {

    LOGGER.info("[ApiClient::invokeAPI] External call started " + method + " " + path + "");
    it.csi.util.performance.StopWatch watcher = new it.csi.util.performance.StopWatch(Constants.COMPONENT_NAME);
    // inizio misurazione
    watcher.start();

    CloseableHttpResponse response = null;

    try {

      URIBuilder target = new URIBuilder(this.url + path);

      if (queryParams != null) {
        for (Pair queryParam : queryParams) {
          if (queryParam.getValue() != null) {
            target.addParameter(queryParam.getName(), queryParam.getValue());
          }
        }
      }

      URI uri = target.build();
      StringEntity entity = null;
      if (body != null) {
    	  entity = new StringEntity(body);
      }

      HttpRequestBase request = null;

      if ("GET".equalsIgnoreCase(method)) {
        request = new HttpGet(uri);
      } else if ("POST".equalsIgnoreCase(method)) {
        HttpEntityEnclosingRequestBase httpEntityRequest = new HttpPost(uri);
        if (entity != null) {
        	httpEntityRequest.setEntity(entity);
        }
        request = httpEntityRequest;
      } else if ("PUT".equalsIgnoreCase(method)) {
        HttpEntityEnclosingRequestBase httpEntityRequest = new HttpPut(uri);
        if (entity != null) {
        	httpEntityRequest.setEntity(entity);
        }
        request = httpEntityRequest;
      } else if ("DELETE".equalsIgnoreCase(method)) {
        request = new HttpDelete(uri);
      } else if ("PATCH".equalsIgnoreCase(method)) {
        HttpEntityEnclosingRequestBase httpEntityRequest = new HttpPatch(uri);
        if (entity != null) {
        	httpEntityRequest.setEntity(entity);
        }
        request = httpEntityRequest;
      } else if ("HEAD".equalsIgnoreCase(method)) {
        request = new HttpHead(uri);
      } else {
        LOGGER.error("[ApiClient::invokeAPI] Richiesto metodo HTTP non esistente:" + method);
        throw new ApiException(500, "unknown method type " + method);
      }

      request.addHeader(HttpHeaders.ACCEPT, accept);
      request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);

      for (Entry<String, String> entry : headerParams.entrySet()) {
        String value = entry.getValue();
        if (value != null) {
          request.addHeader(entry.getKey(), value);
        }
      }

      for (Entry<String, String> entry : defaultHeaderMap.entrySet()) {
        String key = entry.getKey();
        if (!headerParams.containsKey(key)) {
          String value = entry.getValue();
          if (value != null) {
            request.addHeader(key, value);
          }
        }
      }

      response = httpClient.execute(request);
      watcher.dumpElapsed("ApiClient", "httpExecute()", "invocazione http (no token)", "(valore input omesso)");

      int statusCode = response.getStatusLine().getStatusCode();

      LOGGER.info("[ApiClient::invokeAPI] Status Code ritornato " + statusCode);

      String entityResponse = null;

      if (response.getEntity() != null) {
        entityResponse = EntityUtils.toString(response.getEntity());
      }
      Map<String, List<String>> responseHeaders = buildResponseHeaders(response);

      if (statusCode == HttpStatus.SC_NO_CONTENT) {
        return new ApiResponse(statusCode, responseHeaders);
      } else if (statusCode >= 200 && statusCode < 300) {
        if (entityResponse == null) {
          return new ApiResponse(statusCode, responseHeaders);
        } else {
          return new ApiResponse(statusCode, responseHeaders, entityResponse);
        }
      } else {
        LOGGER.warn("[ApiClient::invokeAPI] Servizio ritorna status code:[" + statusCode + "]");
        String message = "error, service returned status code "+statusCode;
        throw new ApiException(statusCode, message, buildResponseHeaders(response), entityResponse);
      }
    } catch (ClientProtocolException e1) {
      LOGGER.error("[ApiClient::invokeAPI] Eccezione:", e1);
      watcher.dumpElapsed("ApiClient", "httpExecute()", "ClientProtocolException", "(valore input omesso)");
      throw new ApiException(500, "Exception " + e1.getMessage());
    } catch (IOException e1) {
      LOGGER.error("[ApiClient::invokeAPI] Eccezione:", e1);
      watcher.dumpElapsed("ApiClient", "httpExecute()", "IOException", "(valore input omesso)");
      throw new ApiException(500, "Exception " + e1.getMessage());
    } catch (URISyntaxException e1) {
      LOGGER.error("[ApiClient::invokeAPI] Eccezione:", e1);
      watcher.dumpElapsed("ApiClient", "httpExecute()", "URISyntaxException", "(valore input omesso)");
      throw new ApiException(500, "Exception " + e1.getMessage());
    } catch (ApiException e1) {
      watcher.dumpElapsed("ApiClient", "httpExecute()", "ApiException", "(valore input omesso)");
      throw e1;
    } finally {
      LOGGER.info("[ApiClient::invokeAPI] External call End " + method + " " + path + "");
      watcher.stop();
      try {
        if (response != null) {
        	response.close();
        }
      } catch (Exception e) {
        // it's not critical, since the response object is local in method
        // invokeAPI;
        // that's fine, just continue
      }
    }
  }

  /**
   * Build the Client used to make HTTP requests.
   * 
   * @param timeout
   * @return Client
   */
  protected CloseableHttpClient buildHttpClient(int timeout) {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(200);
    cm.setDefaultMaxPerRoute(50);
    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(30 * timeout).build();
    return HttpClients.custom().useSystemProperties().setConnectionManager(cm).setDefaultRequestConfig(config).build();
  }

  protected Map<String, List<String>> buildResponseHeaders(CloseableHttpResponse response) {
    Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
    for (Header entry : response.getAllHeaders()) {
      HeaderElement[] values = entry.getElements();
      List<String> headers = new ArrayList<>();
      for (HeaderElement o : values) {
        headers.add(o.getValue());
      }
      responseHeaders.put(entry.getName(), headers);
    }
    return responseHeaders;
  }

  public String getUrl(String propertyName) throws Exception {
    if(url==null){
      Properties properties = new Properties();
      InputStream stream = this.getClass().getResourceAsStream("/wsEndpointUrls.properties");
      properties.load(stream);
      url = properties.getProperty(propertyName);
    }
    return url;
  }

}
