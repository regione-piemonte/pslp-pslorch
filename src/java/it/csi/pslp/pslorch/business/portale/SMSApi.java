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

import it.csi.pslp.pslcommonobj.dto.sms.CancellazioneMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ModificaMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.PrenotazioneMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ReinvioMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.RicercaMessaggiInputDTO;

@Path("/sms")
@Produces({ "application/json" })
public interface SMSApi {
  
  @POST
  @Path("/prenota_sms")
  @Produces({ "application/json" })
  public Response prenotaSMS(PrenotazioneMessaggioInputDTO parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  @POST
  @Path("/modifica_sms")
  @Produces({ "application/json" })
  public Response modificaSMS(ModificaMessaggioInputDTO parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  @POST
  @Path("/reinvia_sms")
  @Produces({ "application/json" })
  public Response reinviaSMS(ReinvioMessaggioInputDTO parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  @POST
  @Path("/cancella_sms")
  @Produces({ "application/json" })
  public Response cancellaSMS(CancellazioneMessaggioInputDTO parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  @POST
  @Path("/cerca_sms")
  @Produces({ "application/json" })
  public Response cercaSMS(RicercaMessaggiInputDTO parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );
  
  @GET
  @Path("/batch/invio_sms")
  @Produces({ "application/json" })
  public Response batchInviaSMS(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );

  //@POST
  //@Path("/send_sms")
  //@Produces({ "application/json" })
  //public Response sendSMS(ParametriInvioSMS parametri,@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders , @Context HttpServletRequest httpRequest );

}
