/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;
import java.util.Date;

import it.csi.pslp.pslcommonobj.dto.AmbitoDTO;
import it.csi.pslp.pslcommonobj.dto.DocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.StatoDocumentoDTO;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvinWSImpl;
import it.csi.silos.silcommon.util.SilCommonUtils;
import it.csi.silos.silcommon.util.SilTimeUtils;
import it.csi.silpcommonobj.dati.ente.EnteSilpHeaderDTO;
import it.csi.silpcommonobj.util.SilpCommonUtils;

/**
 * Gestisce i placeholder per mail di fine adezione garanzia giovani
 * 
Gentile {0},
la informiamo che il documento {1} inviato il {2} alle ore {3} presso il CPI di {4} in {5} {6}. 

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{7}
 *
 */
public class MailMessagePlaceholderDOCESHandler extends MailMessagePlaceholderAbstractHandler<DocumentoDTO> {

	@Override
	public String replacePlaceholders(String codiceMessaggio,String bodyTemplate, DocumentoDTO dataHolder, String signature) throws Exception {
		EnteSilpHeaderDTO ente =  getEnte(dataHolder);
		String cpi = ente.getDescrizioneEnte().replaceAll("CPI DI", "").trim();
		String indirizzoCpi = ente.getIndirizzo();
		
		bodyTemplate = prepareDatePlaceholders(bodyTemplate);
		
		Date dataDaUsare = dataHolder.getDataInvio()!=null?dataHolder.getDataInvio():dataHolder.getDataInserimento();
		
		String msgStato = dataHolder.getStatoDocumento().getDescrStatoDocumento();
		if(StatoDocumentoDTO.COD_STATO_DOCUMENTO_NON_ACCETTATO.equals(dataHolder.getStatoDocumento().getCodStatoDocumento())) {
			msgStato = SilpCommonUtils.concat(msgStato, dataHolder.getNoteOperatore()," - ") ;
		}
		
		return MessageFormat.format(bodyTemplate, 
									getNomeCognome(dataHolder), 
									dataHolder.getNomeDocumento(),
									SilTimeUtils.convertDataInStringa(dataDaUsare),
									SilTimeUtils.convertDataEOraInStringa(dataDaUsare).substring(11),
									cpi, 
									indirizzoCpi,
									msgStato,
									signature
									);
	}

	private Object getNomeCognome(DocumentoDTO dataHolder) {
		return dataHolder.getUtente().getNome()+" "+dataHolder.getUtente().getCognome();
	}

	/**
	 * Reperisce un ente da silp in base ai codici gruppo operatore , operatore, subcodice, presenti nel calendario dell'incontro
	 * @param dataHolder
	 * @return
	 * @throws Exception 
	 */
	protected EnteSilpHeaderDTO getEnte(DocumentoDTO dataHolder) throws Exception {
		EnteSilpHeaderDTO ente = AdapterSilpsvinWSImpl.getInstance().findEnteSilp(dataHolder.getGruppoOperatore(),""+dataHolder.getCodOperatore(),""+dataHolder.getSubcodice(),dataHolder.getCodUserAggiorn());
		return ente;
	}
	
	@Override
	public String[] handleTo(DocumentoDTO dataHolder) {
		return new String[] { dataHolder.getUtente().getEmail()};
	}

	
	@Override
	public String getCodiceFiscaleUtente(DocumentoDTO dataHolder) {
		return dataHolder.getUtente().getCfUtente();
	}

	@Override
	public boolean isPossibileInviareMail(DocumentoDTO dataHolder) throws Exception {
		return AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI.equals(dataHolder.getCodAmbito());
	}

	 @Override
	 public String replacePlaceholdersIntestazione(String intestazione,DocumentoDTO dataHolder) {
		 intestazione =  intestazione.replaceAll("<numero doc>", dataHolder.getIdDocumento().toString()); 
		 Date dataDoc = (Date)SilCommonUtils.nvl(dataHolder.getDataInvio(), dataHolder.getDataInserimento());
		 intestazione =  intestazione.replaceAll("<data>",SilTimeUtils.convertDataInStringa(dataDoc)); 
		 return intestazione;
	  }

}
