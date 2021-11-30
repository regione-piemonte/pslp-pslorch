/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.util.Date;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.silcommon.util.SilTimeUtils;

/**
 * Gestisce i placeholder per mail di adesione garanzia giovani in stato iniziale A
 * 
Gentile {0},
ti comunichiamo che abbiamo ricevuto in data {1,date,dd/MM/yyyy} la tua richiesta di adesione alla Garanzia Giovani in Piemonte con numero progressivo {2}. 
Per completare la tua adesione devi accedere al servizio {3} attraverso {4} (Sistema Pubblico di Identita'' Digitale); 
se sei un Minore, l''accesso deve essere effettuato da un tuo genitore o da chi detiene la patria potesta''. 
Entrato nell''applicazione, puoi visualizzare i dati in possesso della Regione Piemonte e completarli (alcuni dati richiesti sono obbligatori per procedere). Una volta inseriti i dati, potrai verificarne la correttezza e prenotare un appuntamento presso il tuo Centro per l''Impiego che ti verra'' proposto. 
Nel caso in cui non ti presenti al CpI non potrai proseguire il tuo percorso di Garanzia Giovani.

{5}

Questo messaggio e'' stato inviato in modalita'' automatica, ti preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{6}
 * @author 1871
 *
 */
public class MailMessagePlaceholderRCRICHandler extends MailMessagePlaceholderGenericHandler {

	@Override
	protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature)  throws Exception {
		
		Object[] paramValues = new Object[7];
		for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
			paramValues[i] = getValueOfPlaceHolder(dataHolder,i);
		}
		paramValues[3] = getUrlPSLP();
		paramValues[4] = getUrlSpid();
		paramValues[5] = getMessaggioAggiuntivoConfiguratoByCodiceMessaggio(codiceMessaggio);
		paramValues[6] = signature;
		
		return paramValues;
		
	}

  @Override
  public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
    
	//Leggo la data invio della notifica rdc impostata nei parametri  
	Date dataInvioNotifica =SilTimeUtils.convertStringaInData((String)getValueOfPlaceHolder(dataHolder,1));
	  
    //Verifico che la data sia passata
    Date dataInizioInvioMail = SilTimeUtils.convertStringaInData(ParametroUtils.getInstance().getParametro(ParametroUtils.RIC_RDC_DT_MAIL));
    
    boolean invioMailPossibile = dataInizioInvioMail!=null && dataInvioNotifica!=null && SilTimeUtils.isData1MinoreOUgualeData2(dataInizioInvioMail, dataInvioNotifica);
    if(!invioMailPossibile) {
      log.info("MailMessagePlaceholderGGRICHandler.isPossibileInviareMail invio mail bloccato per parametro RIC_GG_DT_MAIL pari a "+dataInizioInvioMail + " e data notifica " +dataInvioNotifica);
      return false;
    }
    return true;
  }



}
