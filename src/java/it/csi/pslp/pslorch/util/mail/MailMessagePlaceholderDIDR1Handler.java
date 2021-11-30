/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.util.ParametroUtils;

/**
 * Gestisce i placeholder per mail di rifiuto totale did
 * 
 * select * from pslp_d_messaggio t where t.cod_messaggio = 'DIDR1';

Gentile {0},
le comunichiamo che la sua Dichiarazione di Immediata Disponibilita' in Piemonte con Numero progressivo {1} presentata il {2,date,dd/MM/yyyy} e'' stata completamente Respinta con la seguente motivazione {3}.

Se possiedi ancora i requisiti, potrai procedere con una nuova Dichiarazione, attraverso il servizio {4}.

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{5}

 * @author 1871
 *
 */
public class MailMessagePlaceholderDIDR1Handler extends MailMessagePlaceholderGenericHandler {

	@Override
	protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature)  throws Exception {
		
		Object[] paramValues = new Object[6];
		for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
			paramValues[i] = getValueOfPlaceHolder(dataHolder,i);
		}
		paramValues[4] = getUrlPSLP();
		paramValues[5] = signature;
		
		return paramValues;
		
	}

  @Override
  public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
	return isParametroInvioMailAttivoAdOggi(ParametroUtils.DIDR1_DT_MAIL);
  }

  //Non so bene a che serva, nelle precedenti traccia l'id dell'entita
  @Override
  public Long getIdSilRifAmbito(ParametriInvioMail dataHolder) {
    try {
      return (Long)dataHolder.getMailPlaceholder(1).getValue();
    }
    catch(Exception ex) {
      log.error("[MailMessagePlaceholderDIDR1Handler::getIdSilRifAmbito]",ex);
      return null;
    }
  }

}
