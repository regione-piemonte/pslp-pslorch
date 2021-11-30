/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.util.List;
import java.util.Map;

/**
 * API response returned by API call.
 *
 */
public class ApiResponse {
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final String data;

  /**
   * @param statusCode The status code of HTTP response
   * @param headers The headers of HTTP response
   */
  public ApiResponse(int statusCode, Map<String, List<String>> headers) {
      this(statusCode, headers, null);
  }

  /**
   * @param statusCode The status code of HTTP response
   * @param headers The headers of HTTP response
   * @param data The object deserialized from response bod
   */
  public ApiResponse(int statusCode, Map<String, List<String>> headers, String data) {
      this.statusCode = statusCode;
      this.headers = headers;
      this.data = data;
  }

  public int getStatusCode() {
      return statusCode;
  }

  public Map<String, List<String>> getHeaders() {
      return headers;
  }

  public String getData() {
      return data;
  }

}
