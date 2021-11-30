/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl.helper;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.business.integration.AdapterSilpsvdeWSImpl;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.silcommon.constants.SilCommonConstants;
import it.csi.silos.silcommon.dati.lavoratore.schedaprofessionale.StatoYouthGuaranteeDTO;
import it.csi.silpcommonobj.dati.incontri.StatoAdesioneSilpDTO;
import it.csi.silpcommonobj.dati.incontri.StatoIncontroSilpDTO;

/**
 * Classe per gestire decodifiche di PSLP o conversioni di decodfiche da silp a PSLP.
 * 
 * 
 * Attualmente molte sono schiantate, sarebbero da caricare tramite i metodi in AdapterSilpsvdeWSImpl e metterli nelle map
 * @author CSI
 *
 */
public class DecodificheHelper {
	
	protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
	//CF finto che uso solo per invocazione di servizi per caricare le mappe di decodifica
	private static String CODFISCDECO_PSLP = "CODFISCDECO_PSLP";

	public static final String COD_STATO_INCONTRO_NON_PRESENTATO_NP = "NP";

	public static final String COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI = "DI";
	
	public static final String COD_STATO_INCONTRO_DISDETTO_DA_CITTADINO_DC = "DC";

	public static final String COD_STATO_INCONTRO_EROGATO_ER = "ER";
	
	public static final String COD_STATO_INCONTRO_DA_EROGARE_DE = "DE";
	
	public static final String COD_STATO_INCONTRO_SPOSTATO_SP = "SP";
	
	//ECC...

	/**
	 * Mappa con i codici stato incontro silp e ministeriale
	 */
	private static HashMap<String,Long> _hmCodStatoIncontroMinIdStatoIncontroSilp = null;
	
	/**
	 * Mappa con i codici ministeriali stato adesione e stato incontro 
	 */
	private static HashMap<String, String> _hmCodStatoAdesioneStatoIncontro = null;
	
	
	/**
	 * Mappa per avere uno stato adesione (con tutte le info reperite sa silp) a partire dal codice ministeriale
	 */
	private static HashMap<String, StatoAdesioneSilpDTO> _hmStatoAdesioneByCod = null;
	
	static {
		try {
			loadMaps();
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Eccezione nel caricamento di una mappa di decodifiche",e);
		}
	}   


	private static void loadMaps() throws Exception {
		log.info("DecodificheHelper.loadMaps inizio caricamento mappe per decodifiche");
		loadMapStatiIncontro();
		loadMapStatoAdesioneStatoIncontro();
		loadMapStatiAdesione();
		log.info("DecodificheHelper.loadMaps fine caricamento mappe per decodifiche");
	}


	private static void loadMapStatiIncontro() {
		
		//Sarebbe meglio usare la AdapterSilpsvdeWSImpl.findStatiIncontro
		
		_hmCodStatoIncontroMinIdStatoIncontroSilp = new HashMap<String,Long>();
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_DA_EROGARE_DE,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_DA_EROGARE)); 
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_EROGATO_ER,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_EROGATO));
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_DISDETTO)); //DISDETTO
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_SPOSTATO_SP,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_SPOSTATO)); 
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_NON_PRESENTATO_NP,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_NON_PRESENTATO)); 
		_hmCodStatoIncontroMinIdStatoIncontroSilp.put(COD_STATO_INCONTRO_DISDETTO_DA_CITTADINO_DC,new Long(StatoIncontroSilpDTO.ID_STATO_INCONTRO_DISDETTO_DA_CITTADINO));
	}
	
		
	private static void loadMapStatiAdesione() throws Exception {
		log.info("DecodificheHelper.loadMapStatiAdesione inizio caricamento stati adesione");
		//Non riesco a contattare il servizio riprovero' piu' avanti
		_hmStatoAdesioneByCod = new HashMap<String, StatoAdesioneSilpDTO>();
		List<StatoAdesioneSilpDTO> stati = AdapterSilpsvdeWSImpl.getInstance().findStatiAdesione(CODFISCDECO_PSLP);
		for (StatoAdesioneSilpDTO statoAdesioneSilpDTO : stati) {
			_hmStatoAdesioneByCod.put(statoAdesioneSilpDTO.getCodAdesioneMin(), statoAdesioneSilpDTO);
		}
		
		log.info("DecodificheHelper.loadMapStatiAdesione fine caricamento stati adesione size="+ _hmStatoAdesioneByCod.size());
	}
	
	private static StatoAdesioneSilpDTO buildStatoForTest(String cod, String descr, boolean finale) {
		StatoAdesioneSilpDTO sa = new StatoAdesioneSilpDTO();
		sa.setCodAdesioneMin(cod);
		sa.setDescrizioneMin(descr);
		sa.setFlgStatoFinale(finale?"S":"N");
		return sa;
	}
	
	
	/*
	 * Ritorna il codice stato incontro da impostare in base allo stato di una adesione.
	 * Su silp e' definito nella tabella SIL_CONF_AGGIORN_STATO_INC,
	 * su orchsil lo imposto staticamente per ora, sarebbe da leggere da silp o configurarlo su una tabella di parametri
	 */
	private static void loadMapStatoAdesioneStatoIncontro() {
		 _hmCodStatoAdesioneStatoIncontro = new HashMap<String, String>();
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_C_ADESIONE_CANCELLATA_PER_MANCANZA_DI_REQUISITI,COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_D_ADESIONE_ANNULLATA_DAL_CITTADINO,COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_F_ADESIONE_FINE_PARTECIPAZIONE,COD_STATO_INCONTRO_EROGATO_ER);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_N_ADESIONE_CHIUSURA_DI_UFFICIO_PER_PRESA_IN_CARICO_DA_ALTRA_REGIONE,COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_P_ADESIONE_PRESO_IN_CARICO,COD_STATO_INCONTRO_EROGATO_ER);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_R_ADESIONE_RIFIUTATA_DAL_CITTADINO,COD_STATO_INCONTRO_EROGATO_ER);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_T_ADESIONE_ACCETTAZIONE_POLITICA_ATTIVA,COD_STATO_INCONTRO_EROGATO_ER);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_U_ADESIONE_CHIUSURA_PER_MANCATA_PRESENZA,COD_STATO_INCONTRO_NON_PRESENTATO_NP);
		_hmCodStatoAdesioneStatoIncontro.put(StatoYouthGuaranteeDTO.STATO_X_ADESIONE_PATTO_NON_FIRMATO,COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
	}
	
	/**
	 * Ritorna l'id silp di uno stato incontro corrispondente a un codice ministeriale
	 * @param codStatoIncontroAnpal
	 * @return
	 */
	public static Long getIdSilpStatoIncontro(String codStatoIncontroAnpal) {
		 return _hmCodStatoIncontroMinIdStatoIncontroSilp.get(codStatoIncontroAnpal);
	}

	/**
	 * Ritorna il codice stato incontro da impostare in corrispondenza di un dato codice stato adesione
	 * @param codStatoAdesione
	 * @return
	 */
	public static String getCodStatoIncontroDaStatoAdesione(String codStatoAdesione) {
		 return _hmCodStatoAdesioneStatoIncontro.get(codStatoAdesione);
	}
	
	
	/**
	 * TRue se il codice passato in input rappresenta uno stato finale di una adesione
	 * @param codStatoAdesione
	 * @return
	 */
	public static boolean isStatoAdesioneFinale(String codStatoAdesione) {
		return  _hmStatoAdesioneByCod.containsKey(codStatoAdesione) && SilCommonConstants.S.equals(_hmStatoAdesioneByCod.get(codStatoAdesione).getFlgStatoFinale());
	}
	
	/**
	 * True per lo stato di una adesione iniziale cioe' A Attiva
	 * @param codStatoAdesione
	 * @return
	 */
	public static boolean isStatoAdesioneIniziale(String codStatoAdesione) {
		return StatoYouthGuaranteeDTO.STATO_A_ADESIONE_ATTIVA.equals(codStatoAdesione);
	}
	
	public static String getDescrizioneStatoAdesione(String codStatoAdesione) {
		if(_hmStatoAdesioneByCod.containsKey(codStatoAdesione)) {
			return   _hmStatoAdesioneByCod.get(codStatoAdesione).getDescrizioneMin();
		}
		return null;
	}
	
}
