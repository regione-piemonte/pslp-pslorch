/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslorch.business.portale.impl.helper.DecodificheHelper;

public abstract class MailMessagePlaceholderGGAbstractHandler extends MailMessagePlaceholderAbstractHandler<ParametriMailGG> {


	/**
	 * Ritorna l'id adesione di silp utilizzata nel processo di prenotazione incontro
	 * @param dataHolder
	 * @return
	 */
	protected Long getNumProgressivoAdesione(ParametriMailGG dataHolder) {
		return dataHolder.getIdAdesione();
	}

	protected String getDescrizioneStatoAdesione(ParametriMailGG dataHolder) {
		return DecodificheHelper.getDescrizioneStatoAdesione(dataHolder.getCodStatoAdesione());
	}
		
  @Override
  public String getCodiceFiscaleUtente(ParametriMailGG dataHolder) {
    return dataHolder.getCodiceFiscaleCittadino();
  }

  @Override
  public Long getIdSilRifAmbito(ParametriMailGG dataHolder) {
    return dataHolder.getIdAdesione();
  }
	
}
