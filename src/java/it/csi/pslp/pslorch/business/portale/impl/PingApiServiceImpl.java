/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.csi.pslp.pslorch.business.portale.PingApi;
import it.csi.pslp.pslorch.util.Constants;

@Component("pingApi")
public class PingApiServiceImpl implements PingApi{

  protected static final Logger LOGGER = Logger.getLogger(Constants.COMPONENT_NAME);

  @Override
  @Transactional
  public Response ping(SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
    LOGGER.info("[PingApiServiceImpl::ping] ping");
    return Response.ok("PING OK!").header("someheader", ""+System.currentTimeMillis()).build();
  }

}
