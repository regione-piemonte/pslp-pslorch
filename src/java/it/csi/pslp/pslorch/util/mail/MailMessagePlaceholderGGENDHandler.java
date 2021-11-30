/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;
import java.util.Date;

import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.silcommon.constants.SilCommonConstants;
import it.csi.silos.silcommon.util.SilTimeUtils;

/**
 * Gestisce i placeholder per mail di fine adezione garanzia giovani
 * 
 * Gentile {0},
ti comunichiamo che in data {1,date,dd/MM/yyyy} e'' terminato il tuo percorso in Garanzia Giovani con numero progressivo {2} per {3}.

{4}

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{5}
 * @author 1871
 *
 */
public class MailMessagePlaceholderGGENDHandler extends MailMessagePlaceholderGGAbstractHandler {

	@Override
	public String replacePlaceholders(String codiceMessaggio,String bodyTemplate, ParametriMailGG dataHolder, String signature) throws Exception {
		
		bodyTemplate = prepareDatePlaceholders(bodyTemplate);
		return MessageFormat.format(bodyTemplate, 
									dataHolder.getNomeCognomeCittadino(), 
									SilTimeUtils.convertDataInStringa(dataHolder.getDataStatoAdesione()),
									dataHolder.getIdAdesione(),
									getDescrizioneStatoAdesione(dataHolder), 
									getMessaggioAggiuntivoConfiguratoByCodiceMessaggio(codiceMessaggio), 
									signature
									);
	}

	@Override
	public String[] handleTo(ParametriMailGG dataHolder) {
		return new String[] { dataHolder.getMail()};
	}

	
	/**
	 * L'invio mail nel caso di termine percorso adesione garanzia giovani e' da inviare se un particolare flg e' abilitato e se la data odierna e' successiva a una data parametrica iniziale
	 */
	@Override
	public boolean isPossibileInviareMail(ParametriMailGG dataHolder) throws Exception {
		
		return isPossibileInviareMailFineAdesione("MailMessagePlaceholderGGENDHandler");
		
	}

	
	/**
	 * Espongo la logica generica per utilizzarla anche in GGENDEXT che e' sempre relativo a fine adesione ma chiamato da esterno
	 * @return
	 * @throws Exception
	 */
	public static boolean isPossibileInviareMailFineAdesione(String className) throws Exception {
		//Verifico che il flag sia S
		String flgInvioMail = ParametroUtils.getInstance().getParametro(ParametroUtils.FINE_GG_FLG);
		boolean invioMailPossibile = SilCommonConstants.S.equals(flgInvioMail);
		if(!invioMailPossibile) {
			log.info(className+".isPossibileInviareMail invio mail bloccato per parametro FINE_GG_FLG pari a "+flgInvioMail);
			return false;
		}
		
		//Verifico che la data sia passata
		Date dataInizioInvioMail = SilTimeUtils.convertStringaInData(ParametroUtils.getInstance().getParametro(ParametroUtils.FINE_GG_DT_MAIL));
		invioMailPossibile = dataInizioInvioMail!=null && SilTimeUtils.isData1MinoreOUgualeData2(dataInizioInvioMail, SilTimeUtils.today());
		if(!invioMailPossibile) {
			log.info(className+".isPossibileInviareMail invio mail bloccato per parametro FINE_GG_DT_MAIL pari a "+dataInizioInvioMail);
			return false;
		}
		
		return true;
	}

}
