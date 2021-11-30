/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.CalendarioDTO;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvinWSImpl;
import it.csi.silos.silcommon.util.SilTimeUtils;
import it.csi.silpcommonobj.dati.ente.EnteSilpHeaderDTO;



public abstract class MailMessagePlaceholderAPAbstractHandler extends MailMessagePlaceholderAbstractHandler<PrenotazioneDTO> {
  

	public String getNomeCognomeUtente(PrenotazioneDTO dataHolder) {
		return dataHolder.getUtente().getNome() + " " + dataHolder.getUtente().getCognome();
	}
	
  @Override
  public String getCodiceFiscaleUtente(PrenotazioneDTO dataHolder) {
    return dataHolder.getUtente().getCfUtente();
  }

	/**
	 * Torna la data dell'incontro 
	 * @param dataHolder
	 * @return
	 */
	protected String getDataIncontro(PrenotazioneDTO dataHolder) {
		return SilTimeUtils.convertDataInStringa(dataHolder.getSlot().getGiorno().getGiorno());
	}
	
	/**
	 * Torna l'ora di inizio di un incontro nel formato hh:mm
	 * @param dataHolder
	 * @return
	 */
	protected String getOraIncontro(PrenotazioneDTO dataHolder) {
		return dataHolder.getSlot().getDescrizioneOraInizio();
	}

	protected int getDurataIncontro(PrenotazioneDTO dataHolder) {
		return dataHolder.getSlot().getOraFine() - dataHolder.getSlot().getOraInizio();
	}
	
	/**
	 * Ritorna l'id adesione di silp utilizzata nel processo di prenotazione incontro
	 * @param dataHolder
	 * @return
	 */
	protected Long getNumProgressivoAdesione(PrenotazioneDTO dataHolder) {
		return dataHolder.getIdSilRifAmbito();
	}


	/**
	 * Ritorna l'id del calendario di una prenotazione
	 * @param dataHolder
	 * @return
	 */
	protected Long getIdCalendario(PrenotazioneDTO dataHolder) {
		return dataHolder.getSlot().getGiorno().getPeriodo().getCalendario().getIdCalendario();
	}

	
	/**
	 * Reperisce un ente da silp in base ai codici gruppo operatore , operatore, subcodice, presenti nel calendario dell'incontro
	 * @param dataHolder
	 * @return
	 * @throws Exception 
	 */
	protected EnteSilpHeaderDTO getEnte(PrenotazioneDTO dataHolder) throws Exception {
		CalendarioDTO cal = dataHolder.getSlot().getGiorno().getPeriodo().getCalendario();
		EnteSilpHeaderDTO ente = AdapterSilpsvinWSImpl.getInstance().findEnteSilp(cal.getGruppoOperatore(),""+cal.getCodOperatore(),""+cal.getSubcodice(),dataHolder.getCodUserAggiorn());
		return ente;
	}

  @Override
  public Long getIdUtente(PrenotazioneDTO dataHolder) {
    return dataHolder.getUtente().getIdUtente();
  }
  
  @Override
  public Long getIdSilRifAmbito(PrenotazioneDTO dataHolder) {
    return dataHolder.getIdSilRifAmbito();
  }

  @Override
  public String replacePlaceholdersIntestazione(String intestazione,PrenotazioneDTO dataHolder) {
    String oggettoCalendario = dataHolder.getSlot().getGiorno().getPeriodo().getCalendario().getOggettoCalendario();
    if(oggettoCalendario!=null)
      intestazione = intestazione+" "+oggettoCalendario;
    return intestazione;
  }
	
}
