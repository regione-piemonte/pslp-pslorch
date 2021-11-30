/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;

import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;

/**
 * Handler per comporre il messaggio della mail da inviare in caso di
 * appuntamento "Disdetto da Operatore CPI"
 * 
 * vedi testo messaggio nella tabella PSLP_D_MESSAGGIO con cod_messaggio =
 * 'APANN'
 * 
 * @author CSI
 *
 *
 *
 *         Gentile {0}, ti comunichiamo che abbiamo dovuto annullare l''incontro
 *         previsto in data {1,date,dd/MM/yyyy} relativamente alla tua richiesta
 *         di adesione alla Garanzia Giovani in Piemonte con numero progressivo
 *         {2}. Per completare la tua adesione e'' necessario che prenoti un
 *         nuovo appuntamento presso il tuo Centro per l''Impiego attraverso il
 *         servizio {3}.
 * 
 *         {4}
 * 
 *         Questo messaggio e'' stato inviato in modalita'' automatica, la
 *         preghiamo di non rispondere a questo indirizzo.
 * 
 *         Cordiali Saluti
 * 
 *         {5}
 *
 *
 */
public class MailMessagePlaceholderAPANNHandler extends MailMessagePlaceholderAPAbstractHandler {

    @Override
    public String replacePlaceholders(String codiceMessaggio, String bodyTemplate, PrenotazioneDTO dataHolder,
            String signature) throws Exception {

        bodyTemplate = bodyTemplate.replace(",date,dd/MM/yyyy", ""); // TODO: sentire Roberto per modificare i template
        return MessageFormat.format(bodyTemplate, getNomeCognomeUtente(dataHolder),
                getDataIncontro(dataHolder) + " " + getOraIncontro(dataHolder), getNumProgressivoAdesione(dataHolder),
                getUrlPSLP(),
                getMessaggioAggiuntivoConfiguratoByIdCalendario(codiceMessaggio, getIdCalendario(dataHolder)),
                signature);
    }

    @Override
    public String[] handleTo(PrenotazioneDTO dataHolder) {
        return new String[] { dataHolder.getUtente().getEmail() };
    }

    @Override
    public boolean isPossibileInviareMail(PrenotazioneDTO dataHolder) throws Exception {
        return true;
    }

}
