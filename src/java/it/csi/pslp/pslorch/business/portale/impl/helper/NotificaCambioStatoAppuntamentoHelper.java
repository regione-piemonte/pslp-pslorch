/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl.helper;

import org.apache.log4j.Logger;

import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAppuntamento;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.silcommon.util.SilCommonUtils;


/**
 * Classe per gestire logiche dedicate al processo di cambio stato appuntamento
 * @author CSI
 *
 */
public class NotificaCambioStatoAppuntamentoHelper {

	protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
	
	public NotificaCambioStatoAppuntamentoHelper(){
		
	}
	
	/**
	 * Valida obbligatorieta' e coerenza dei parametri in input. Aggiorna l'esito con eventuale codice e messaggi
	 * @param result
	 * @param parametri
	 */
	public void validateInputParameters(EsitoDTO result, ParametriNotificaCambioStatoAppuntamento parametri) {
		log.debug("[NotificaCambioStatoAppuntamentoHelper::validateInputParameters]");
	    if(SilCommonUtils.isVoid(parametri.getIdIncontroSilp()) 
	    		  || SilCommonUtils.isVoid(parametri.getCodiceFiscaleLavoratore()) 
	    		  || SilCommonUtils.isVoid(parametri.getCodStatoIncontro())
	    		  || SilCommonUtils.isVoid(parametri.getCodiceOperatore()))
	    		  {
	    	  result.setCodiceEsito(EsitoDTO.E0008_DATI_OBBLIGATORI_NON_VALORIZZATI);
	    	  result.addErrorMessage("Dati obbligatori mancanti");
	     }
			
	}


}
