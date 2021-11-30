/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.common;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.EventoDBDef;
import it.csi.pslp.pslcommonobj.dto.EventoDTO;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.jedi.core.DAO;

@Component("tracciamentoUtils")
public class TracciamentoUtils {
  
  public static final Long BATCH_MAIL = 1000L;
  public static final Long BATCH_SMS = 1001L;
  public static final Long LOAD_DOCUMENTO = 10033L;

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
  
  @Autowired
  private DAO dao;

  public EventoDTO tracciaOk(Long idTipoEvento, Long idUtente, String cfUtente, String note, String codiceAmbito) {
    return traccia("OK", idTipoEvento, idUtente, cfUtente, note, codiceAmbito);
  }
  
  public EventoDTO tracciaKo(Long idTipoEvento,Long idUtente, String cfUtente, String note, String codiceAmbito) {
    return traccia("KO", idTipoEvento, idUtente, cfUtente, note, codiceAmbito);
  }
  
  private EventoDTO traccia(String esito, Long idTipoEvento, Long idUtente, String cfUtente, String note, String codiceAmbito) {
    try {
      EventoDTO evento = new EventoDTO();
      evento.setCodEsito(esito);
      evento.setIdTipoEvento(idTipoEvento);
      evento.setIdUtente(idUtente);
      if(note!=null && note.length()>=4000)
        note = note.substring(0,3999);
      evento.setNote(note);
      evento.setCodiceAmbito(codiceAmbito);
      evento.setCodUserInserim(cfUtente);
      evento.setCodUserAggiorn(cfUtente);
      evento.setDEvento(new Date());
      evento = dao.insert(EventoDBDef.class, evento);
      return evento;
    }
    catch(Throwable ex) {
      log.error("[TracciamentoUtils::traccia]", ex);
      return null;
    }
  }
}
