/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;

/**
 * OGGETTO: Richiesta Collocamento Mirato Piemonte: Accolta
 * 
 * Gentile{0}, la informiamo che la sua Richiesta di{1}per{2}, inviata il{3},
 * con Numero progressivo{4}, e' stata: ACCOLTA.
 * 
 * Pertanto, risulta Iscritto alle liste del Collocamento Mirato e qualora
 * intervengano delle variazioni, potra' aggiornare il Reddito o i Familiari a
 * carico attraverso l'apposita funzionalita' di modifica esposta nel
 * servizio{5}.
 * 
 * Questo messaggio e' stato inviato in modalita' automatica, la preghiamo di
 * non rispondere a questo indirizzo.
 * 
 * Cordiali Saluti
 * 
 * {6}
 *
 */
public class MailMessagePlaceholderL68ACHandler extends MailMessagePlaceholderGenericHandler {

    @Override
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature) throws Exception {

        Object[] paramValues = new Object[11];
        for (int i = 0; i < dataHolder.getParametriMail().size(); i++) {
            paramValues[i] = getValueOfPlaceHolder(dataHolder, i);
        }
        paramValues[5] = getUrlPSLP();
        paramValues[6] = getUrlIoLavoroVirt();
        paramValues[7] = getUrlChiamateL68();
        paramValues[8] = getUrlAreaDisabilita();
        paramValues[9] = getUrlBuonoServLav();

        paramValues[10] = signature;

        return paramValues;

    }
}
