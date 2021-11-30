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
 * Gestisce i placeholder per mail di domanda reddito di cittadinanza
 * 
Gentile {0},
le comunichiamo che abbiamo ricevuto in data {1,date,dd/MM/yyyy} la notifica della sua domanda per il Reddito di Cittadinanza con numero progressivo {2}. Per iniziare il percorso, dovra'' accedere al servizio {3} attraverso {4} (Sistema Pubblico di Identita'' Digitale). Entrato nell''applicazione, potra'' visualizzare i dati in possesso della Regione Piemonte e completarli (alcuni dati richiesti sono obbligatori per procedere). Una volta inseriti i dati, potra'' verificarne la correttezza e prenotare un appuntamento presso il tuo Centro per l''Impiego che le verra'' proposto. 

{5}

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{6}
 * @author 1871
 *
 */
public class MailMessagePlaceholderGGRICHandler extends MailMessagePlaceholderGenericHandler {

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
	return isParametroInvioMailAttivoAdOggi(ParametroUtils.RIC_GG_DT_MAIL);
  }



}
