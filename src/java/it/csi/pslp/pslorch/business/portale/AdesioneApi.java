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

import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAdesione;

@Path("/adesione")
@Produces({ "application/json" })
public interface AdesioneApi {

  @POST
  @Path("/notifica_cambio_stato_adesione")
  @Produces({ "application/json" })
  public Response notificaCambioStatoAdesione(ParametriNotificaCambioStatoAdesione parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
}
