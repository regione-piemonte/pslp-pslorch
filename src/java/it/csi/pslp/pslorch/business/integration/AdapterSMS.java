/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.business.integration.dto.sms.invio.richiesta.RICHIESTASMS;
import it.csi.pslp.pslorch.business.integration.dto.sms.invio.risposta.ESITOACQUISIZIONESMS;
import it.csi.pslp.pslorch.util.Constants;

public class AdapterSMS {

  private static Logger logger = Logger.getLogger(Constants.COMPONENT_NAME);

  private JAXBContext jcInvioRichiesta;

  private JAXBContext jcInvioRisposta;

  private String url;

  public AdapterSMS() {
  }

  public void inviaSMS(RICHIESTASMS richiesta) throws Exception {
    if (jcInvioRichiesta == null) {
      jcInvioRichiesta = JAXBContext.newInstance("it.csi.pslp.pslorch.business.integration.dto.sms.invio.richiesta");
    }
    Marshaller m = jcInvioRichiesta.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    StringWriter sw = new StringWriter();
    m.marshal(richiesta, sw);

    HttpClient httpclient = HttpClients.createDefault();
    String url = getUrl("smsgateway.url") + "/pSmsRequest.cgi";
    logger.info("[AdapterSMS::inviaSMS] Invocazione di " + url);
    HttpPost httppost = new HttpPost(url);
    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
    params.add(new BasicNameValuePair("xmlSms", sw.toString()));
    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
    HttpResponse response = httpclient.execute(httppost);
    int statusCode = response.getStatusLine().getStatusCode();
    logger.info("[AdapterSMS::inviaSMS] Status Code ritornato " + statusCode);
    String entityResponse = null;
    if (response.getEntity() != null) {
      entityResponse = EntityUtils.toString(response.getEntity());
    }

    if (jcInvioRisposta == null) {
      jcInvioRisposta = JAXBContext.newInstance("it.csi.pslp.pslorch.business.integration.dto.sms.invio.risposta");
    }
    Unmarshaller u = jcInvioRisposta.createUnmarshaller();
    StringReader reader = new StringReader(entityResponse);
    ESITOACQUISIZIONESMS esito = (ESITOACQUISIZIONESMS) u.unmarshal(reader);
  }

  public String getUrl(String propertyName) throws Exception {
    if (url == null) {
      Properties properties = new Properties();
      InputStream stream = this.getClass().getResourceAsStream("/wsEndpointUrls.properties");
      properties.load(stream);
      url = properties.getProperty(propertyName);
    }
    return url;
  }

  public static void main(String[] args) throws Exception {
    AdapterSMS a = new AdapterSMS();
    RICHIESTASMS richiesta = new RICHIESTASMS();
    richiesta.setCODICEPROGETTO("PSLP");
    a.inviaSMS(richiesta);
  }
}
