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
 * select * from pslp_d_messaggio t where t.cod_messaggio = 'GGR2';
 
 Gentile {0},
ti comunichiamo che lo Stato inserito il {1,date,dd/MM/yyyy}, relativo alla tua Adesione a Garanzia Giovani in Piemonte con Numero progressivo {2} presentata il {3,date,dd/MM/yyyy}, e'' stato Respinto con la seguente motivazione {4}.

Lo Stato attualmente in vigore e'' quello inserito il {5,date,dd/MM/yyyy}.

Questo messaggio e'' stato inviato in modalita'' automatica, ti preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{6}

 * @author CSI
 *
 */
public class MailMessagePlaceholderGGR2Handler extends MailMessagePlaceholderGenericHandler {

	@Override
	protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature)  throws Exception {
		
		Object[] paramValues = new Object[7];
		for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
			paramValues[i] = getValueOfPlaceHolder(dataHolder,i);
		}
		paramValues[6] = signature;
		
		return paramValues;
		
	}

  @Override
  public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
	  return isParametroInvioMailAttivoAdOggi(ParametroUtils.GGR2_DT_MAIL);

  }

  

}
