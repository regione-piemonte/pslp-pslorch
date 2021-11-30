/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
 
package it.csi.pslp.pslorch.util;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.MessaggioDBDef;
import it.csi.pslp.pslcommonobj.dto.MessaggioDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslcommonobj.filter.MessaggioFilter;
import it.csi.pslp.pslorch.business.SpringApplicationContextHelper;
import it.csi.pslp.pslorch.business.common.PslDAO;
import it.csi.pslp.pslorch.util.mail.ParametriMailGG;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;

@Component("messaggiUtils")
public class MessaggiUtils {

	protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

	@Autowired
	private DAO dao;

	public static MessaggiUtils getInstance() {
    return (MessaggiUtils)SpringApplicationContextHelper.getBean("messaggiUtils");
	}

	public MessaggioDTO loadMessaggio(MessaggioType codMessaggio) throws Exception {
		String codeDaCaricare = codMessaggio.getCode();
		
		/**
		 * 
		 * Serve per gestire la mail di fine adesione richiamata da silp GGENDEXT rispetto alla mail di fine adesione GGEND chiamata all'interno di processi pslorch
		 * gli handler sono diversi ma il template da caricare e' lo stesso GGEND
		 * Anche gli oggetti come parametri in input sono diversi e difficili da uniformare
		 */
		if(ParametriInvioMail.PSLP_COD_MAIL_RICEZIONE_FINE_ADESIONE_GARANZIA_GIOVANI_GGENDEXT.equals(codeDaCaricare)){
			codeDaCaricare = MessaggioType.GGEND.toString();
	    }
		
		return loadMessaggio(codeDaCaricare);
	}
	/**
	 * Restituisce un messaggio valido per un codice.
	 */
	public MessaggioDTO loadMessaggio(String codMessaggio) throws Exception {
		return loadMessaggioValidoByCodOrId(null,codMessaggio);
	}

	/**
	 * Restituisce un messaggio valido per un dato ID.
	 */
	public MessaggioDTO loadMessaggioById(Long idMessaggio) throws Exception {
		return loadMessaggioValidoByCodOrId(idMessaggio,null);
	}

	private MessaggioDTO loadMessaggioValidoByCodOrId(Long idMessaggio,String codMessaggio) throws DAOException {
		Date now = new Date();
		MessaggioFilter filter1 = new MessaggioFilter();
		if(idMessaggio!=null) {
			filter1.getIdMessaggio().eq(idMessaggio);
		}
		else if(codMessaggio!=null) {
			filter1.getCodMessaggio().eq(codMessaggio);
		}
		else {
			throw new IllegalArgumentException("Parametri errati. Si sta cercando di caricare un messaggio senza id e senza Codice");
		}
		filter1.getDInizio().le(now);
		MessaggioFilter filter2 = new MessaggioFilter();
		filter2.getDFine().eq(null);
		MessaggioFilter filter3 = new MessaggioFilter();
		filter3.getDFine().ge(now);
		MessaggioFilter filter = (MessaggioFilter)filter1.and(filter2.or(filter3));
		MessaggioDTO messaggioDTO = dao.findFirst(MessaggioDBDef.class, filter);
		return messaggioDTO;
	}

}
