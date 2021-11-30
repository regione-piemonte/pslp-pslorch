/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;

/**
 * OGGETTO: Richiesta Collocamento Mirato Piemonte: Annullata
 * 
 * Gentile{0}, le comunichiamo che la sua Richiesta di {1}per{2}, con Numero
 * progressivo{3} e' stata: ANNULLATA.
 * 
 * Potra' presentare una nuova Richiesta presso il suo Centro per l'Impiego di
 * competenza, attraverso la stessa funzionalita' esposta nel servizio{4}.
 * 
 * Questo messaggio e' stato inviato in modalita' automatica, la preghiamo di
 * non rispondere a questo indirizzo.
 * 
 * Cordiali Saluti
 * 
 * {5}
 *
 */

public class MailMessagePlaceholderL68ANHandler extends MailMessagePlaceholderGenericHandler {

    @Override
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature) throws Exception {

        Object[] paramValues = new Object[6];
        for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
            paramValues[i] = getValueOfPlaceHolder(dataHolder, i);
        }
        paramValues[4] = getUrlPSLP();
        paramValues[5] = signature;

        return paramValues;

    }
}
