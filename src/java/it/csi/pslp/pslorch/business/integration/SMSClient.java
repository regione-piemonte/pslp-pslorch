/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SMSClient {
  
  protected static final Logger logger = Logger.getLogger(Constants.COMPONENT_NAME);

  private static final String SMS_URL_PROPERTY = "smsgateway.url";

  private String url;
  
  protected CloseableHttpClient httpClient;

  public SMSClient() throws Exception {
    this.url = getUrl(SMS_URL_PROPERTY);
    httpClient = buildHttpClient(30000);
  }
  
  public String invokeApi(String path, String msg) throws Exception {
    logger.info("[SMSClient::invokeApi] External call started, url= "+ url + path + ",msg="+msg);
    it.csi.util.performance.StopWatch watcher = new it.csi.util.performance.StopWatch(Constants.COMPONENT_NAME);
    watcher.start();

    CloseableHttpResponse response = null;

    try {

      URIBuilder target = new URIBuilder(this.url + path);
      URI uri = target.build();
      HttpEntityEnclosingRequestBase request = new HttpPost(uri);
      List<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
      msg = msg.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n", "");
      postParameters.add(new BasicNameValuePair("xmlSms", msg));
      request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

      request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
      request.addHeader(HttpHeaders.ACCEPT, "application/xml");
      
      logger.info("[SMSClient::invokeApi] request="+EntityUtils.toString(new UrlEncodedFormEntity(postParameters, "UTF-8")));

      response = httpClient.execute(request);
      watcher.dumpElapsed("SMSClient", "httpExecute()", "invocazione http (no token)", "(valore input omesso)");

      int statusCode = response.getStatusLine().getStatusCode();

      logger.info("[SMSClient::invokeAPI] Status Code ritornato " + statusCode);

      String entityResponse = null;

      if (response.getEntity() != null) {
        entityResponse = EntityUtils.toString(response.getEntity());
      }

      if (statusCode == HttpStatus.SC_NO_CONTENT) {
        return null;
      }
      else if (statusCode >= 200 && statusCode < 300) {
        logger.warn("[SMSClient::invokeAPI] response="+entityResponse);
        return entityResponse;
      }
      else {
        logger.warn("[SMSClient::invokeAPI] Servizio ritorna status code:[" + statusCode + "]");
        String message = "error, service returned status code "+statusCode+": "+entityResponse;
        throw new Exception(message);
      }
    }
    catch (Exception e1) {
      logger.error("[SMSClient::invokeAPI] Eccezione:", e1);
      watcher.dumpElapsed("ApiClient", "httpExecute()", "ClientProtocolException", "(valore input omesso)");
      throw new ApiException(500, "Exception " + e1.getMessage());
    }
    finally {
      logger.info("[SMSClient::invokeAPI] External call End " + path + "");
      watcher.stop();
      try {
        if (response != null) response.close();
      }
      catch (Exception e) {
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

  private String getUrl(String propertyName) throws Exception {
    if(url==null){
      Properties properties = new Properties();
      InputStream stream = this.getClass().getResourceAsStream("/wsEndpointUrls.properties");
      properties.load(stream);
      url = properties.getProperty(propertyName);
    }
    return url;
  }

  private static final String ENCRYPTION_KEY = "0!cQfP_@kk01?'XZ";

  private static final String ENCRYPTION_ALGORITHM = "AES";

  public static String encrypt(String plainText) throws Exception {
    Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
    byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }

  private static Cipher getCipher(int cipherMode) throws Exception {
    SecretKeySpec keySpecification = new SecretKeySpec(ENCRYPTION_KEY.getBytes("UTF-8"), ENCRYPTION_ALGORITHM);
    Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
    cipher.init(cipherMode, keySpecification);

    return cipher;
  }

  public static String decrypt(String encrypted) throws Exception {
    Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
    byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));

    return new String(plainBytes);
  }

  public static void main(String[] args) throws Exception {
    String clearPassword = "...";
    String encryptedPassword = encrypt(clearPassword);
    System.out.println("Clear Password     ="+clearPassword);
    System.out.println("Encrypted Password ="+encryptedPassword);
    System.out.println("Decrypted Password ="+decrypt(encryptedPassword));
  }

}
