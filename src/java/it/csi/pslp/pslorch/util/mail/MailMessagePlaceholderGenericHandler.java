/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import it.csi.pslp.pslcommonobj.dto.MailPlaceholder;
import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.silcommon.util.SilTimeUtils;

/**
 * Classe gestore generico di sostituzione placeholder per una mail accettando
 * in input un oggetto contenente elenco di placeholder per indice o label
 * popolati dal chiamante. Puo' essere quindi usato per varie tipologie di mail
 * non strettamente legate a un parametro input specifico
 * 
 * @author 1871
 *
 */
public class MailMessagePlaceholderGenericHandler extends MailMessagePlaceholderAbstractHandler<ParametriInvioMail> {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Override
    public String replacePlaceholders(String codiceMessaggio, String bodyTemplate, ParametriInvioMail dataHolder, String signature) throws Exception {

        bodyTemplate = bodyTemplate.replace(",date,dd/MM/yyyy", "");

        return MessageFormat.format(bodyTemplate, buildParamsValues(codiceMessaggio, dataHolder, signature));

    }

    /**
     * Costruisce l'array di valori per formattare il template, le sottoclassi
     * possono ridefinirlo
     * 
     * @param codiceMessaggio
     * @param dataHolder
     * @param signature
     * @return
     * @throws Exception
     */
    protected Object[] buildParamsValues(String codiceMessaggio, ParametriInvioMail dataHolder, String signature) throws Exception {

        int numParametri = dataHolder.getParametriMail().size();
        Object[] paramValues = new Object[numParametri];
        for (int i = 0; i < numParametri; i++) {
            paramValues[i] = getValueOfPlaceHolder(dataHolder, i);
        }
        return paramValues;
    }

    protected String getValueOfPlaceHolder(ParametriInvioMail paramsMail, int index) {
        MailPlaceholder m = paramsMail.getMailPlaceholder(index);
        if (m != null) {
            return m.getValue() != null ? m.getValue().toString() : "";
        } else {
            return "<Value assente per index=" + index + ">";
        }
    }

    @Override
    public String[] handleTo(ParametriInvioMail dataHolder) {
        return new String[] { dataHolder.getMailTo() };
    }

    @Override
    public boolean isPossibileInviareMail(ParametriInvioMail dataHolder) throws Exception {
        return true;
    }

    @Override
    public String getCodiceFiscaleUtente(ParametriInvioMail dataHolder) {
        return dataHolder.getCodiceFiscaleDestinatario();
    }

    @Override
    public Long getIdSilRifAmbito(ParametriInvioMail dataHolder) {
        try {
            if (dataHolder.getMailPlaceholder(2).getValue() instanceof Long) {
                return (Long) dataHolder.getMailPlaceholder(2).getValue();
            } else {
                return 0L;
            }
        } catch (Exception ex) {
            log.error("[MailMessagePlaceholderGenericHandler::getIdSilRifAmbito]", ex);
            return null;
        }
    }

    protected boolean isParametroInvioMailAttivoAdOggi(String codParametro) throws Exception {
        Date oggi = SilTimeUtils.today();

        Date dataInizioInvioMail = SilTimeUtils.convertStringaInData(ParametroUtils.getInstance().getParametro(codParametro));

        boolean invioMailPossibile = dataInizioInvioMail == null || SilTimeUtils.isData1MinoreOUgualeData2(dataInizioInvioMail, oggi);
        if (!invioMailPossibile) {
            log.info(getClass().getSimpleName() + ".isParametroInvioMailAttivo invio mail bloccato per parametro " + codParametro + " pari a "
                    + dataInizioInvioMail + " e data attuale " + oggi);
            return false;
        }
        return true;
    }

}
