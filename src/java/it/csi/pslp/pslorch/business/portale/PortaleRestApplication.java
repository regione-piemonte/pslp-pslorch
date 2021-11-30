/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import it.csi.csi.wrapper.ConfigException;
import it.csi.pslp.pslorch.business.SpringApplicationContextHelper;
import it.csi.pslp.pslorch.business.portale.impl.AdesioneApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.CalendarioApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.DocumentiApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.MailApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.PingApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.SMSApiServiceImpl;
import it.csi.pslp.pslorch.business.portale.impl.UtentiApiServiceImpl;
import it.csi.pslp.pslorch.util.SpringInjectorInterceptor;
import it.csi.pslp.pslorch.util.SpringSupportedResource;

@ApplicationPath("/portale")
public class PortaleRestApplication extends Application{
  private Set<Object> singletons = new HashSet<Object>();
  private Set<Class<?>> empty = new HashSet<Class<?>>();
  public PortaleRestApplication() throws ConfigException{
     singletons.add(new PingApiServiceImpl());
     singletons.add(new UtentiApiServiceImpl());
     singletons.add(new CalendarioApiServiceImpl());
     singletons.add(new AdesioneApiServiceImpl());
     singletons.add(new MailApiServiceImpl());
     singletons.add(new SMSApiServiceImpl());
     singletons.add(new DocumentiApiServiceImpl());
     
     
      singletons.add(new SpringInjectorInterceptor());
       
       for (Object c : singletons) {
    if (c instanceof SpringSupportedResource) {
      SpringApplicationContextHelper.registerRestEasyController(c);
    }
  }
  }
  @Override
  public Set<Class<?>> getClasses() {
       return empty;
  }
  @Override
  public Set<Object> getSingletons() {
       return singletons;
  }

}
