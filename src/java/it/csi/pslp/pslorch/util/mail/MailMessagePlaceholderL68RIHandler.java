/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;

/**
 * Gentile{0}, la informiamo che la sua Richiesta di{1}per{2}, inviata il{3},
 * con Numero progressivo{4}, e' stata: RESPINTA.
 * 
 * Il motivo del respingimento e':{5}.
 * 
 * Potra' presentare una nuova Richiesta presso il suo Centro per l'Impiego di
 * competenza, attraverso la stessa funzionalita' esposta nel servizio{6}.
 * 
 * Questo messaggio e' stato inviato in modalita' automatica, la preghiamo di
 * non rispondere a questo indirizzo.
 * 
 * Cordiali Saluti
 * 
 * {7}
 *
 */
public class MailMessagePlaceholderL68RIHandler extends MailMessagePlaceholderGenericHandler {

    @Override
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature) throws Exception {

        Object[] paramValues = new Object[8];
        for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
            paramValues[i] = getValueOfPlaceHolder(dataHolder, i);
        }
        paramValues[6] = getUrlPSLP();
        paramValues[7] = signature;

        return paramValues;

    }
}
