/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl.helper;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.PrenotazioneDBDef;
import it.csi.pslp.pslcommonobj.dbdef.UtenteDBDef;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.UtenteDTO;
import it.csi.pslp.pslcommonobj.filter.PrenotazioneFilter;
import it.csi.pslp.pslcommonobj.filter.UtenteFilter;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.silcommon.util.SilTimeUtils;

/**
 * concentro qui un  po di metodi di utilita' generica reperimento di dati da db relativi a utente, prenotazione ecc...
 * per non appesantire troppo il codice con righe di find by filter
 * 
 * @author 1871
 *
 */
@Component("entityFinder")
public class EntityFinder {

	 protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
	  
	 @Autowired
	 private DAO dao;
	
	/**
	 * Ricerca la prenotazione per chiave
	 * @param idPrenotazione
	 * @return
	 * @throws DAOException
	 */
	public PrenotazioneDTO findPrenotazioneByKey(Long idPrenotazione) throws DAOException {
		return findPrenotazioneByFilter(idPrenotazione, null, null,null,null);
	}
	
	/**
	 * Cerca l'ultima prenotazione 
	 * @param codiceFiscale
	 * @param codStatoIncontro
	 * @return
	 * @throws DAOException
	 */
	public PrenotazioneDTO findPrenotazioneByCFEStatoDal(String codiceFiscale,String codStatoIncontro,Date dataDa) throws DAOException {
		return findPrenotazioneByFilter(null, codiceFiscale, codStatoIncontro,null,dataDa);
	}
	
	/**
	 * Cerca l'ultima prenotazione inserita per id utente e stato 
	 * @param idUtente
	 * @param codiceStatoIncontro
	 * @return
	 * @throws DAOException
	 */
	public PrenotazioneDTO findPrenotazioneByIdUtenteCFEStato(Long idUtente, String codiceStatoIncontro) throws DAOException {
		UtenteDTO utente = findUtente(idUtente);
		PrenotazioneDTO p = null;
		if(utente!=null) {
			p = findPrenotazioneByCFEStatoDal(utente.getCfUtente(),codiceStatoIncontro,null);
		}
		 return p;
	}
	
	/**
	 * Cerca per id contatto silp
	 * @param idContattoSilp
	 * @return
	 * @throws DAOException
	 */
	public PrenotazioneDTO findPrenotazioneByIdContattoSilp(Long idContattoSilp) throws DAOException {
		if(idContattoSilp==null) return null;
		return findPrenotazioneByFilter(null, null, null,idContattoSilp,null);
	}
	 
	/**
	 * Ricerca la prenotazione piu' recente in base ad alcuni filtri comuni
	 * @param idPrenotazione
	 * @param codiceFiscale
	 * @param codStatoIncontro
	 * @return
	 * @throws DAOException 
	 */
	private PrenotazioneDTO findPrenotazioneByFilter(Long idPrenotazione,String codiceFiscale,String codStatoIncontro, Long idContattoSilp,Date dataDa) throws DAOException {
		 if(idPrenotazione==null && codiceFiscale==null && idContattoSilp==null) {
			 throw new IllegalArgumentException("findPrenotazioneByFilter richiamata con tutti parametri input nulli");
		 }
		 PrenotazioneFilter filter = new PrenotazioneFilter();
		 if(idPrenotazione!=null) filter.getIdPrenotazione().eq(idPrenotazione);
		 if(codiceFiscale!=null) filter.getUtente().getCfUtente().eq(codiceFiscale);
		 if(codStatoIncontro!=null) filter.getIdStatoAppuntamento().eq(codStatoIncontro);
		 if(idContattoSilp!=null) filter.getIdSilInContatto().eq(idContattoSilp);
		 if(dataDa!=null) filter.getSlot().getGiorno().getGiorno().ge(SilTimeUtils.truncDate(dataDa));
		 filter.getIdPrenotazione().setOrderDesc(1); //Prendo il piu' recente
		 return dao.findFirst(PrenotazioneDBDef.class, filter);
	}
	
	
	public UtenteDTO findUtente(String cf) throws DAOException {
		  return findUtenteByFilter(cf,null);
	}

	public UtenteDTO findUtente(Long idUtente) throws DAOException {
	   return findUtenteByFilter(null,idUtente);
	}

	private UtenteDTO findUtenteByFilter(String cf, Long idUtente) throws DAOException {
		if(cf==null && idUtente==null) return null;
		UtenteFilter filter = new UtenteFilter();
	    if(cf!=null) {
	    	filter.getCfUtente().eq(cf);
	    }
	    if(idUtente!=null) {
	    	filter.getIdUtente().eq(idUtente);
	    }
	    UtenteDTO utente = dao.findFirst(UtenteDBDef.class, filter);
	    return utente;
	}
	
	
	
	private UtenteDTO findDocumentiByFilter(String cf, Long idUtente) throws DAOException {
		if(cf==null && idUtente==null) return null;
		UtenteFilter filter = new UtenteFilter();
	    if(cf!=null) {
	    	filter.getCfUtente().eq(cf);
	    }
	    if(idUtente!=null) {
	    	filter.getIdUtente().eq(idUtente);
	    }
	    UtenteDTO utente = dao.findFirst(UtenteDBDef.class, filter);
	    return utente;
	}
	  
}
