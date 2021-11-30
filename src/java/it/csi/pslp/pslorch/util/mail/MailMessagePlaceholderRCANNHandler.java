/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;

import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslorch.util.ParametroUtils;

public class MailMessagePlaceholderRCANNHandler extends MailMessagePlaceholderAPRDCAbstractHandler {

  @Override
  public String replacePlaceholders(String codiceMessaggio, String bodyTemplate, PrenotazioneDTO dataHolder, String signature) throws Exception {
    
    bodyTemplate = bodyTemplate.replace(",date,dd/MM/yyyy", ""); // TODO: sentire Roberto per modificare i template
    return MessageFormat.format(bodyTemplate, 
                            getNomeCognomeUtente(dataHolder), 
                            getDataIncontro(dataHolder)+" "+getOraIncontro(dataHolder), 
                            getCodiceRdC(dataHolder), 
                            getUrlPSLP(), 
                            getMessaggioAggiuntivoConfiguratoByIdCalendario(codiceMessaggio,getIdCalendario(dataHolder)), 
                            signature
                            );
  }

  @Override
  public String[] handleTo(PrenotazioneDTO dataHolder) {
    return new String[] { dataHolder.getUtente().getEmail() };
  }

  @Override
  public boolean isPossibileInviareMail(PrenotazioneDTO dataHolder) throws Exception {

	  return isParametroInvioMailAttivoAdOggi(ParametroUtils.RCANN_DT_MAIL);
  }

}
