/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslorch.business.integration.MailUtils;
import it.csi.pslp.pslorch.business.portale.MailApi;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.util.performance.StopWatch;

@Component("mailApi")
public class MailApiServiceImpl implements MailApi {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private MailUtils mailUtils;

    @Override
    public Response sendMail(ParametriInvioMail parametri, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[MailApiServiceImpl::sendMail] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            EsitoDTO result = new EsitoDTO();
            MessaggioType messaggioType = getMessageTypeByCodiceMail(parametri.getCodiceMail());
            mailUtils.sendMail(parametri, messaggioType, false);

            log.debug("[MailApiServiceImpl::sendMail] END esito=" + result);
            return Response.ok(result).build();
        } catch (Exception ex) {
            log.error("[MailApiServiceImpl::sendMail] parametri=" + parametri, ex);
            Error err = new Error("Si e' verificato un errore nella notifica del cambio stato appuntamento");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("MailApiServiceImpl", "sendMail()", "invocazione API MailApiServiceImpl.sendMail", "");
            watcher.stop();
        }
    }

    /**
     * Ritorna true se il codice ricevuto e ammesso per un invio mail da un
     * chiamante esterno a pslp
     * 
     * @param codiceMail
     * @return
     */
    private MessaggioType getMessageTypeByCodiceMail(String codiceMail) {
        // Qui bisogna gestire tutti i possibii template esistenti
        if (MessaggioType.GGRIC.toString().equals(codiceMail)) {
            return MessaggioType.GGRIC;
        } else if (MessaggioType.RCRIC.toString().equals(codiceMail)) {
            return MessaggioType.RCRIC;
        } else if (MessaggioType.GGR1.toString().equals(codiceMail)) {
            return MessaggioType.GGR1;
        } else if (MessaggioType.GGR2.toString().equals(codiceMail)) {
            return MessaggioType.GGR2;
        } else if (MessaggioType.DIDR1.toString().equals(codiceMail)) {
            return MessaggioType.DIDR1;
        } else if (MessaggioType.DIDR2.toString().equals(codiceMail)) {
            return MessaggioType.DIDR2;
        } else if (MessaggioType.GGENDEXT.toString().equals(codiceMail)) {
            return MessaggioType.GGENDEXT;
        } else if (MessaggioType.PSES.toString().equals(codiceMail)) {
            return MessaggioType.PSES;
        } else if (MessaggioType.L68IN.toString().equals(codiceMail)) {
            return MessaggioType.L68IN;
        } else if (MessaggioType.L68AC.toString().equals(codiceMail)) {
            return MessaggioType.L68AC;
        } else if (MessaggioType.L68AN.toString().equals(codiceMail)) {
            return MessaggioType.L68AN;
        } else if (MessaggioType.L68RI.toString().equals(codiceMail)) {
            return MessaggioType.L68RI;
        } else {
            throw new IllegalArgumentException("codice mail " + codiceMail + " non ancora gestito per chiamanti esterni");
        }
        // ecc...

    }

}
