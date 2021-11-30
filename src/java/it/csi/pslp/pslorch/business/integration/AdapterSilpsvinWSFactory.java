/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;
import it.csi.silpsv.silpsvin.cxfclient.GestioneIncontri;


public class AdapterSilpsvinWSFactory {

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

  private Service s = null;
  
  private String endpoint;
  
  public AdapterSilpsvinWSFactory() {
  }
  
  public GestioneIncontri getService() throws Exception {
    GestioneIncontri service;
	try {
		if(s==null) {
		  log.info("[AdapterSilpsvinWSFactory::getService] Look up to SILPSV.silpsvin....");
		  s = Service.create(                              
		      getClass().getResource("/silpsv.silpsvin.wsdl"),
		      new QName("urn:silpsvin", "GestioneIncontriService")
		      );
		}
		service = s.getPort(GestioneIncontri.class);
		BindingProvider bp = (BindingProvider) service;        
		Map<String, Object> context = bp.getRequestContext();
		context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());   
		org.apache.cxf.endpoint.Client client = ClientProxy.getClient(service);
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy policy = conduit.getClient();
		policy.setConnectionTimeout(30000);
		policy.setReceiveTimeout(30000);
		log.info("[AdapterSilpsvinWSFactory::getService] Look up to SILPSV.silpsvin completed to endpoint "+endpoint);
	} catch (Throwable e) {
		e.printStackTrace();
		throw e;
	}
    
    return service;
  }

  public String getEndpoint() throws Exception {
    if(endpoint==null){
      Properties properties = new Properties();
      InputStream stream = this.getClass().getResourceAsStream("/wsEndpointUrls.properties");
      properties.load(stream);
      endpoint = properties.getProperty("silpsvin.endpoint");
      log.info("[AdapterSilpsvinWSFactory::getEndpoint] Caricato endpoint "+endpoint);
    }
    return endpoint;
  }

}
