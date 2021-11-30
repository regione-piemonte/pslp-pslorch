/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;

@Path("/mail")
@Produces({ "application/json" })
public interface MailApi {

  @POST
  @Path("/send_mail")
  @Produces({ "application/json" })
  public Response sendMail(ParametriInvioMail parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
}
