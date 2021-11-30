/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;

import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.silos.silcommon.util.SilTimeUtils;
import it.csi.silpcommonobj.dati.ente.EnteSilpHeaderDTO;

public class MailMessagePlaceholderAPCONHandler extends MailMessagePlaceholderAPAbstractHandler {

	
	


	@Override
	public String replacePlaceholders(String codiceMessaggio,String bodyTemplate, PrenotazioneDTO dataHolder, String signature) throws Exception {
		EnteSilpHeaderDTO ente =  getEnte(dataHolder);
		String cpi = ente.getDescrizioneEnte().replaceAll("CPI DI", "").trim();
		String indirizzoCpi = ente.getIndirizzo();
		
		bodyTemplate = prepareDatePlaceholders(bodyTemplate);
		return MessageFormat.format(bodyTemplate, 
									getNomeCognomeUtente(dataHolder), 
									cpi, 
									SilTimeUtils.convertDataInStringa(dataHolder.getdInserim()), 
									getNumProgressivoAdesione(dataHolder), 
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

	
	
}
