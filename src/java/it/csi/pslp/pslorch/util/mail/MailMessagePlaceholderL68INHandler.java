/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;

/**
 * Richiesta Collocamento Mirato Piemonte: Inviata per la validazione
 * 
 * OGGETTO: Richiesta Collocamento Mirato Piemonte: Inviata per la validazione
 * 
 * Gentile{0}, le comunichiamo che la sua Richiesta di{1}per{2}, con Numero
 * progressivo{3}, e' stata: INVIATA.
 * 
 * Ricevera' l'esito della validazione (Richiesta Accettata o Respinta) tramite
 * un'apposita mail.
 * 
 * Questo messaggio e' stato inviato in modalita' automatica, la preghiamo di
 * non rispondere a questo indirizzo.
 * 
 * Cordiali Saluti
 * 
 * {4}
 *
 * 
 */
public class MailMessagePlaceholderL68INHandler extends MailMessagePlaceholderGenericHandler {

    @Override
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature) throws Exception {

        Object[] paramValues = new Object[5];
        for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
            paramValues[i] = getValueOfPlaceHolder(dataHolder, i);
        }
        paramValues[4] = signature;

        return paramValues;

    }
}
