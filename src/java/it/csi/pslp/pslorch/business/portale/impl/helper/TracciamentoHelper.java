/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl.helper;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.pslp.pslcommonobj.dbdef.EventoDBDef;
import it.csi.pslp.pslcommonobj.dto.EventoDTO;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.silcommon.constants.SilCommonConstants;

@Component("tracciamentoHelper")
public class TracciamentoHelper {
  
  public static final Long INVIO_MAIL = 10044L;
  public static final Long NOTIFICA_CAMBIO_STATO_ADESIONE = 10035L;
  
 
  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
  
  @Autowired
  private DAO dao;
  
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public EventoDTO tracciaOk(Long idTipoEvento, Long idUtente, String note,String codUser) {
    return traccia(SilCommonConstants.OK, idTipoEvento, idUtente, note,codUser);
  }
  
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public EventoDTO tracciaKo(Long idTipoEvento, Long idUtente, String note,String codUser) {
    return traccia(SilCommonConstants.KO, idTipoEvento, idUtente,  note,codUser);
  }
  
  private EventoDTO traccia(String esito, Long idTipoEvento, Long idUtente,String note, String codUser) {
    try {
      EventoDTO evento = new EventoDTO();
      evento.setCodEsito(esito);
      evento.setIdTipoEvento(idTipoEvento);
      evento.setIdUtente(idUtente);
      evento.setNote(note);
      evento.setCodUserInserim(codUser);
      evento.setCodUserAggiorn(codUser);
      evento.setDEvento(new Date());
      //evento.setIpChiamante(null);
      evento = dao.insert(EventoDBDef.class, evento);
      return evento;
    }
    catch(Throwable ex) {
      log.error("[TracciamentoHelper::traccia]", ex);
      return null;
    }
  }
}
