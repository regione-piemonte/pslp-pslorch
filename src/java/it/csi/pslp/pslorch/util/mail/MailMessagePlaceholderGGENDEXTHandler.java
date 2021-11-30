/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.business.portale.impl.helper.DecodificheHelper;
import it.csi.pslp.pslorch.util.MessaggioType;

/**
 * Gestisce i placeholder per mail di fine adezione garanzia giovani RICHIAMATA
 * DA SISTEMI ESTERNI Quindi utilizza lo stesso templare del GGEND ma i
 * paraemtri sono forniti dal sistema esterno e di tipo ParametriInvioMail
 * 
 * Gentile {0}, ti comunichiamo che in data {1,date,dd/MM/yyyy} e'' terminato il
 * tuo percorso in Garanzia Giovani con numero progressivo {2} per {3}.
 * 
 * {4}
 * 
 * Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di
 * non rispondere a questo indirizzo.
 * 
 * Cordiali Saluti
 * 
 * {5}
 * 
 * @author 1871
 *
 */
public class MailMessagePlaceholderGGENDEXTHandler extends MailMessagePlaceholderGenericHandler {

    @Override
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature)
            throws Exception {

        // OCCHIO CARICO LO STESSO TEMPLATE DEL CODICE MESSAGGIO GGEND
        Object[] paramValues = new Object[6];

        // nome cognome
        paramValues[0] = getValueOfPlaceHolder(dataHolder, 0);
        // data stato adesione
        paramValues[1] = getValueOfPlaceHolder(dataHolder, 1);
        // id adesione
        paramValues[2] = getValueOfPlaceHolder(dataHolder, 2);
        // codice stato adesione da cui ottengo descrizione
        paramValues[3] = DecodificheHelper.getDescrizioneStatoAdesione(getValueOfPlaceHolder(dataHolder, 3));
        paramValues[4] = getMessaggioAggiuntivoConfiguratoByCodiceMessaggio(MessaggioType.GGEND.toString());
        paramValues[5] = signature;

        return paramValues;

    }

    /**
     * L'invio mail nel caso di termine percorso adesione garanzia giovani e' da
     * inviare se un particolare flg e' abilitato e se la data odierna e' successiva
     * a una data parametrica iniziale
     */
    @Override
    public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
        // Stessa logica ufficiale della fine adesione
        return MailMessagePlaceholderGGENDHandler
                .isPossibileInviareMailFineAdesione("MailMessagePlaceholderGGENDEXTHandler");

    }

}
