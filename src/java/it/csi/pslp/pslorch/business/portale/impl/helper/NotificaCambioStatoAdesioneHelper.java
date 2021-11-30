/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl.helper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAdesione;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.UtenteDTO;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvspWSImpl;
import it.csi.pslp.pslorch.business.integration.MailUtils;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.pslp.pslorch.util.mail.ParametriMailGG;
import it.csi.silos.silcommon.dati.anagrafe.DatiAnagraficiPersonaDTO;
import it.csi.silos.silcommon.generic.CallInfoDTO;
import it.csi.silos.silcommon.util.SilCommonUtils;
import it.csi.silpcommonobj.util.SilpCommonUtils;
import it.csi.silpservizi.sesp.bo.sap.SchedaAnagraficoProfessionaleLavoratoreDTO;


/**
 * Classe per accomunare e gestire logiche di supporto dedicate al processo di cambio stato adesione
 * @author 1871
 *
 */
@Component
public class NotificaCambioStatoAdesioneHelper {

	protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
	
	@Autowired
	private MailUtils mailUtils;
	
	@Autowired
	private EntityFinder finder;
	
	@Autowired
	private TracciamentoHelper tracciamentoHelper;
	  
	public NotificaCambioStatoAdesioneHelper(){
		
	}

	
	
	/**
	 * Valida obbligatorieta' e coerenza dei parametri in input. Aggiorna l'esito con eventuale codice e messaggi
	 * @param result
	 * @param parametri
	 */
	public void validateInputParameters(EsitoDTO result, ParametriNotificaCambioStatoAdesione parametri) {
		log.debug("[NotificaCambioStatoAdesioneHelper::validateInputParameters] parametri="+parametri);
	    
		//Qusti sono i dati obbligatori per tutti i chiamanti	
	    if(SilCommonUtils.isVoid(parametri.getCodiceFiscaleLavoratore()) 
	    	||SilCommonUtils.isVoid(parametri.getDataAdesione()) 
	    	|| SilCommonUtils.isVoid(parametri.getCodStatoAdesione()) 
	    	|| SilCommonUtils.isVoid(parametri.getCodiceOperatore())
	    	|| SilCommonUtils.isVoid(parametri.getDataStatoAdesione())
	    		){
	    	  result.setCodiceEsito(EsitoDTO.E0008_DATI_OBBLIGATORI_NON_VALORIZZATI);
	    	  result.addErrorMessage("Dati obbligatori su adesione mancanti");
	    	  return;
	     }
	    
	    //Per chiamanti diversi da ministero i dati incontro sono obbligatori
	    if(!CallInfoDTO.CODICE_APPLICATIVO_MINISTERO.equals(parametri.getCodiceOperatore())){
	    	//
	    	//non e obbligatorio che mi passi un incontro, ovviamente se passa id deve passare anche stato e viceversa.
	    	//Nel caso manchi incontro verra' gestito il cambio stato adesione e relativo invio mail
	    	
	    	if(SilCommonUtils.isVoid(parametri.getIdIncontroSilp()) && SilCommonUtils.isNotVoid(parametri.getCodStatoIncontro())
	    	    || SilCommonUtils.isNotVoid(parametri.getIdIncontroSilp()) && SilCommonUtils.isVoid(parametri.getCodStatoIncontro())
	    	    ) {
	    		result.setCodiceEsito(EsitoDTO.E0008_DATI_OBBLIGATORI_NON_VALORIZZATI);
	    		result.addErrorMessage("Almeno un dato obbligatorio relativi all'incontro mancanti (codice stato o identificativo)");
	    	}
	    }
	    
	}


	 /**
	   * Invia una mail al cittadino nel caso l'adesione sia in uno stato Finale o iniziale. NOn e' piu' necessario che esista un incontro
	 * @param prenotazione 
	 * @param esitoTotale 
	 * @throws Exception 
	   */
	  public void invioEventualeMailPerCambioStatoAdesione(ParametriNotificaCambioStatoAdesione parametriNotificaAdesione, PrenotazioneDTO prenotazione, EsitoDTO esitoTotale) throws Exception {
		 String logMethodName = "[NotificaCambioStatoAdesioneHelper::invioEventualeMailPerCambioStatoAdesione] ";
		 log.debug(logMethodName +"Verifica invio mail per notifica adesione "+parametriNotificaAdesione);
		 
		 MessaggioType mailType = null;
		 //L'utente serve per il nome e cognome e mail, lo si reperisce o dalla prenotazione o dal CF
		 UtenteDTO utente = null;
		
		 if(DecodificheHelper.isStatoAdesioneIniziale(parametriNotificaAdesione.getCodStatoAdesione())){
			 log.debug(logMethodName +"Stato adesione iniziale");
			 mailType = MessaggioType.GGRIC;
			 utente = finder.findUtente(parametriNotificaAdesione.getCodiceFiscaleLavoratore());
			 log.debug(logMethodName +"Identificato utente "+utente);
		 }
		 //Per altri stati se finali e con prenotaizone presente invio mail
		 else if(DecodificheHelper.isStatoAdesioneFinale(parametriNotificaAdesione.getCodStatoAdesione())){
			 log.debug(logMethodName +"Stato adesione finale");
			 mailType = MessaggioType.GGEND;
			 if(prenotazione!=null) {
				 utente = prenotazione.getUtente();
			 }
			 if(utente ==null) {
				 utente = finder.findUtente(parametriNotificaAdesione.getCodiceFiscaleLavoratore());
			 }
			 if(utente == null) {
				 log.debug(logMethodName +" utente non presente in pslp");
			 }
			
		 }
		 else {
			 log.debug(logMethodName +"Stato non iniziale e non finale, invio mail non necessario");
		 }
		 
		 if(mailType!=null) {
			 
			 
			 log.debug(logMethodName +" determinato invio mail di tipo "+mailType);
			 /**
			  * Refactoring per ricavare tutti i dati per invio mail.
			  * Ora non viene inviata solo se l'utente e' censito in pslp ma per tutte le notifiche silp/min
			  */
			 ParametriMailGG parametriMail = buildParametriInvioMail(parametriNotificaAdesione, prenotazione, utente);
			  
			 if(esitoTotale.isEsitoPositivo()) {
				 mailUtils.sendMail(parametriMail, mailType, false);
			 }
			 else {
				 String msgErrore = " Mail di tipo "+mailType+ " e parametri "+parametriMail+" non inviata per presenza di errori pregressi nel processo di notifica cambio stato adesione: "+ esitoTotale.getDettagliEsito();
				 log.debug(logMethodName +msgErrore);
				 tracciamentoHelper.tracciaKo(TracciamentoHelper.NOTIFICA_CAMBIO_STATO_ADESIONE, utente!=null?utente.getIdUtente():null, msgErrore, parametriNotificaAdesione.getCodiceOperatore());
			 }
		 }
	  }


	  /**
	   * Costruisco i parametri necessari per l'invio mail di cambio stato adesione deducendoli dai dati trovati in pslp
	   * dai parametri input notifica o richiedendo la sap a silp tramite codice fiscale.
	   * 
	   * - deduce dalla prenotazione se presente
	   * - oppure deduce dall'utente se presente in pslp
	   * - oppure richiede la sap a silp dal cf in input
	   * 
	   * in particolare i dati richiesti sono:
	   * cf cittadino
	   * cognome nome
	   * indirizzo mail
	   * id Adesione
	   * 
	   * dai parametri in input arriva solo il cf e i dati dell'adesione, nome cognome, email devono essere ricavati
	   * es:
	   		  codiceOperatore=MINISTERO, 
    		  dataAdesione=Mon Feb 03 10:00:00 CET 2020, 
    		  codStatoAdesione=D, 
    		  codiceEnte=13, 
    		  dataStatoAdesione=Wed Feb 05 10:00:00 CET 2020, 
    		  idAdesioneSilp=6438, 
    		  codiceFiscaleLavoratore=SLLFNC98B02L219W, 
    		  codiceOperatore=MINISTERO
	   * 
	   * @param parametriNotificaAdesione
	   * @param prenotazione
	   * @param logMethodName
	   * @param utente
	   * @return
	   * @throws Exception
	   */
	private ParametriMailGG buildParametriInvioMail(ParametriNotificaCambioStatoAdesione parametriNotificaAdesione,
			PrenotazioneDTO prenotazione,  UtenteDTO utente) throws Exception {
		String logMethodName = "[NotificaCambioStatoAdesioneHelper::buildParametriInvioMail] ";
		
		
		
		ParametriMailGG parametriMail = new ParametriMailGG();
		parametriMail.setDataStatoAdesione(parametriNotificaAdesione.getDataStatoAdesione());
		parametriMail.setCodStatoAdesione(parametriNotificaAdesione.getCodStatoAdesione());
		parametriMail.setIdAdesione(parametriNotificaAdesione.getIdAdesioneSilp());
		
		String nomeCognome = null;
		String email = null; 
		
		if(prenotazione!=null) {
			nomeCognome = SilpCommonUtils.concat(prenotazione.getUtente().getNome(), prenotazione.getUtente().getCognome());
			email = prenotazione.getUtente().getEmail();
			log.info(logMethodName+"parametri recuperati da prenotazione "+ prenotazione.getIdPrenotazione());
		}
		else if(utente!=null) {
			nomeCognome = SilpCommonUtils.concat(utente.getNome(), utente.getCognome());
			email = utente.getEmail();
			log.info(logMethodName+"parametri recuperati da utente "+ utente.getIdUtente());
		}
		else {
			log.info(logMethodName+" utente non presente in pslp si cerca di dedurre dalla sap per cf="+parametriNotificaAdesione.getCodiceFiscaleLavoratore());
			SchedaAnagraficoProfessionaleLavoratoreDTO sap = AdapterSilpsvspWSImpl.getInstance().getSAP(null, parametriNotificaAdesione.getCodiceFiscaleLavoratore());
			DatiAnagraficiPersonaDTO dp = sap.getSchedaAnagraficaProfessionale().getDatiAnagrafici().getDatiPersonali();
			nomeCognome = SilpCommonUtils.concat(dp.getNome(), dp.getCognome());
			email = sap.getSchedaAnagraficaProfessionale().getDatiAnagrafici().getEmail();
			log.info(logMethodName+"parametri recuperati da sap con id lavoratore "+ sap.getIdLavoratore());
		}
	
		
		 parametriMail.setNomeCognomeCittadino(nomeCognome);
		 parametriMail.setMail(email);
		 
		 parametriMail.setCodiceFiscaleCittadino(parametriNotificaAdesione.getCodiceFiscaleLavoratore());
		 parametriMail.setDataStatoAdesione(parametriNotificaAdesione.getDataStatoAdesione());
		 parametriMail.setCodStatoAdesione(parametriNotificaAdesione.getCodStatoAdesione());
		 parametriMail.setIdAdesione(parametriNotificaAdesione.getIdAdesioneSilp());
		 
		 log.info(logMethodName+" determinati parametri mail "+parametriMail);
			
		 
		return parametriMail;
	}
	  
	  
	  
	
}
