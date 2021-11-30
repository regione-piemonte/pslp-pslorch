/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import it.csi.pslp.pslcommonobj.dto.ParametriAnnulloCalendario;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAppuntamento;
import it.csi.pslp.pslcommonobj.dto.ParametriRicercaCalendarioDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriRicercaSlotDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriSalvataggioIncontroDTO;

@Path("/calendario")
@Produces({ "application/json" })
public interface CalendarioApi {

  @POST
  @Path("/slots")
  @Produces({ "application/json" })
  public Response findSlots(ParametriRicercaSlotDTO parametriRicercaSlotDTO,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );

  @POST
  @Path("/intervallo_disponibile")
  @Produces({ "application/json" })
  public Response findIntervalloDisponibile(ParametriRicercaCalendarioDTO parametriRicerca,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );

  @POST
  @Path("/save_incontro")
  @Produces({ "application/json" })
  public Response saveIncontro(ParametriSalvataggioIncontroDTO params,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest);
  
  @POST
  @Path("/notifica_cambio_stato_appuntamento")
  @Produces({ "application/json" })
  public Response notificaCambioStatoAppuntamento(ParametriNotificaCambioStatoAppuntamento parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  

  @GET
  @Path("/batch/invio_mail")
  @Produces({ "text/plain" })
  public Response invioMail(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );

  @POST
  @Path("/annulla_calendario")
  @Produces({ "application/json" })
  public Response annullaCalendario(ParametriAnnulloCalendario params, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  
}
