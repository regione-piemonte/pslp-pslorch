/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvspWSImpl;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.silcommon.util.SilTimeUtils;
import it.csi.silpcommonobj.dati.ente.EnteSilpHeaderDTO;
import it.csi.silpcommonobj.dati.redditodicittadinanza.EsitoFindBeneficiarioRdcDTO;

public class MailMessagePlaceholderAPRDCAbstractHandler extends MailMessagePlaceholderAPAbstractHandler {
  
  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
  
  @Override
  public String replacePlaceholders(String codiceMessaggio,String bodyTemplate, PrenotazioneDTO dataHolder, String signature) throws Exception {
    EnteSilpHeaderDTO ente = getEnte(dataHolder);
    String cpi = ente.getDescrizioneEnte().replaceAll("CPI DI", "").trim();
    String indirizzoCpi = ente.getIndirizzo();
    
    bodyTemplate = prepareDatePlaceholders(bodyTemplate);
    return MessageFormat.format(bodyTemplate, 
                  getNomeCognomeUtente(dataHolder), 
                  cpi, 
                  SilTimeUtils.convertDataInStringa(dataHolder.getdInserim()), 
                  getCodiceRdC(dataHolder), 
                  getDataIncontro(dataHolder), 
                  getOraIncontro(dataHolder), 
                  indirizzoCpi, 
                  getDurataIncontro(dataHolder), 
                  getMessaggioAggiuntivoConfiguratoByIdCalendario(codiceMessaggio, getIdCalendario(dataHolder)), 
                  signature
                  );
  }


  @Override
  public String[] handleTo(PrenotazioneDTO dataHolder) {
    return new String[] { dataHolder.getUtente().getEmail() };
  }


  @Override
  public boolean isPossibileInviareMail(PrenotazioneDTO dataHolder) throws Exception {
    return true;
  }
  
  protected String getCodiceRdC(PrenotazioneDTO dataHolder) {
    try {
      Long idSilLav = dataHolder.getUtente().getIdSilLavAngrafica();
      EsitoFindBeneficiarioRdcDTO domandaRdC = AdapterSilpsvspWSImpl.getInstance().getDomandaRDCBySILP(idSilLav);
      if(domandaRdC!=null) {
        return domandaRdC.getCodiceProtocollo();
      }
      else {
        return "-";
      }
    }
    catch(Exception ex) {
      log.error("[MailMessagePlaceholderAPRDCAbstractHandler::getCodiceRdC]", ex);
      return "-";
    }
  }

}
