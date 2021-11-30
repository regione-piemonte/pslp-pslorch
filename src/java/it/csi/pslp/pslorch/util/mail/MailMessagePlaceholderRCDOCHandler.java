/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.AmbitoDTO;
import it.csi.pslp.pslcommonobj.dto.DocumentoDTO;

/**
 * Gestisce i placeholder per mail di fine adezione garanzia giovani
Gentile {0},
la informiamo che il documento {1} inviato il {2} alle ore {3} presso il CPI di {4} in {5} {6}. 

Questo messaggio e'' stato inviato in modalita'' automatica, ti preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{7}


Sembra uguale a quallo per doc di GG , ma credo che siano da sistemare, manca il placeholder per lo stado doc?

 *
 */
public class MailMessagePlaceholderRCDOCHandler extends MailMessagePlaceholderDOCESHandler {

	

	@Override
	public boolean isPossibileInviareMail(DocumentoDTO dataHolder) throws Exception {
		return AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA.equals(dataHolder.getCodAmbito());
	}

	

}
