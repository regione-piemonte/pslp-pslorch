/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 

package it.csi.pslp.pslorch.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import it.csi.pslp.pslorch.business.SpringApplicationContextHelper;

@Provider
public class SpringInjectorInterceptor implements PreProcessInterceptor {

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
    	
		SpringApplicationContextHelper.injectSpringBeansIntoRestEasyService(method.getResourceClass().getName());
        
        return null;
    }
}
