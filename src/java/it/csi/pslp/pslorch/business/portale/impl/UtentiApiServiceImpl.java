/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.PrenotazioneDBDef;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.PrenotazioniDTO;
import it.csi.pslp.pslcommonobj.filter.PrenotazioneFilter;
import it.csi.pslp.pslorch.business.portale.UtentiApi;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.QueryResult;
import it.csi.util.performance.StopWatch;

@Component("utentiApi")
public class UtentiApiServiceImpl implements UtentiApi {

    private static final Logger LOGGER = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private DAO dao;

    /**
     * Ricerca tutte le prenotazioni sul portale di un utente.
     */
    @Override
    public Response findPrenotazioni(Long idUtente, String codAmbito, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[UtentiApiServiceImpl::findPrenotazioni] idUtente=" + idUtente + ", codAmbito=" + codAmbito);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            PrenotazioneFilter filter = new PrenotazioneFilter();
            filter.getUtente().getIdUtente().eq(idUtente);
            filter.getSlot().getGiorno().getGiorno().setOrderAsc(1);
            filter.getSlot().getOraInizio().setOrderAsc(2);
            if (codAmbito != null)
                filter.getSlot().getGiorno().getPeriodo().getCalendario().getAmbito().getCodAmbito().eq(codAmbito);
            QueryResult<PrenotazioneDTO> prenotazioni = dao.findAll(PrenotazioneDBDef.class, filter, 0);
            List<PrenotazioneDTO> els = new ArrayList<>();
            for (PrenotazioneDTO prenotazione : prenotazioni)
                els.add(prenotazione);
            PrenotazioniDTO result = new PrenotazioniDTO();
            result.setEls(els);
            return Response.ok(result).build();
        } catch (Exception ex) {
            LOGGER.error("[UtentiApiServiceImpl::findPrenotazioni] idUtente=" + idUtente, ex);
            Error err = new Error(
                    "Si e' verificato un errore nella ricerca delle prenotazioni del Lavoratore con idUtente "
                            + idUtente);
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("UtentiApiServiceImpl", "findPrenotazioni()",
                    "invocazione API UtentiApiServiceImpl.findPrenotazioni", "");
            watcher.stop();
        }
    }

}
