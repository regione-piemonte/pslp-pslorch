/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.util.ParametroUtils;

/**
 * Gestisce i placeholder per mail di rifiuto singolo stato adesione
 * 
 * select * from pslp_d_messaggio t where t.cod_messaggio = 'PSES';
 
Gentile {0},
la informiamo che il suo Patto di Servizio inviato il {1,date,dd/MM/yyyy}, legato alla Dichiarazione di Immediata Disponibilita'' con Numero progressivo {2} del {3,date,dd/MM/yyyy},
presenta il seguente Esito: {4}.

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{5}

{6}
 * @author 1871
 *
 */
public class MailMessagePlaceholderPSESHandler extends MailMessagePlaceholderGenericHandler {

	@Override
	protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature)  throws Exception {
		
		Object[] paramValues = new Object[6];
		for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
			paramValues[i] = getValueOfPlaceHolder(dataHolder,i);
		}
		paramValues[5] = signature;
		
		return paramValues;
		
	}

  @Override
  public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
	  return isParametroInvioMailAttivoAdOggi(ParametroUtils.PSES_DT_MAIL);
  }



}
