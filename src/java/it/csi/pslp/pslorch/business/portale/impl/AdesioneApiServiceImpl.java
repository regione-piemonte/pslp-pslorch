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
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAdesione;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAppuntamento;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslorch.business.portale.AdesioneApi;
import it.csi.pslp.pslorch.business.portale.CalendarioApi;
import it.csi.pslp.pslorch.business.portale.impl.helper.DecodificheHelper;
import it.csi.pslp.pslorch.business.portale.impl.helper.EntityFinder;
import it.csi.pslp.pslorch.business.portale.impl.helper.NotificaCambioStatoAdesioneHelper;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.silcommon.generic.CallInfoDTO;
import it.csi.silos.silcommon.util.SilTimeUtils;
import it.csi.util.performance.StopWatch;

@Component("adesioneApi")
public class AdesioneApiServiceImpl implements AdesioneApi {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private EntityFinder finder;

    @Autowired
    private NotificaCambioStatoAdesioneHelper helper;

    @Autowired
    private CalendarioApi calendarioApi;

    @Override
    public Response notificaCambioStatoAdesione(ParametriNotificaCambioStatoAdesione parametri,
            SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[AdesioneApiServiceImpl::notificaCambioStatoAdesione] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            EsitoDTO result = new EsitoDTO();

            // 1) CONTROLLI OBBLIGATORIETA' E COERENZA
            helper.validateInputParameters(result, parametri);

            /**
             * Quando ricevo una adesione o notifica, vengono effettuate delle operazioni se
             * trovo un incontro da erogare relativo al lavoratore. - Se il chiamante e'
             * MINISTERO arriva solo CF e dati adesione. DEvo quindi cercare tra gli
             * incontri da erogare su pslp - se chiamante e' SILP arriva gia' l'id incontro,
             * ma meglio controllare che sia effettivamente da erogare
             * 
             * alcune informazioni sull'incontro (utente) devono essere passate per le mail
             * 
             */
            PrenotazioneDTO prenotazione = null;

            if (result.isEsitoPositivo()) {
                log.debug(
                        "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] parametri input ok procedo con notifica cambio stato adesione");

                /*
                 * Per chiamante MINISTERO non ho id contatto e id lavoratore, li faccio
                 * determinare dal sistema. Se assenti non procede con la notifica
                 */
                if (CallInfoDTO.CODICE_APPLICATIVO_MINISTERO.equals(parametri.getCodiceOperatore())) {
                    log.debug(
                            "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] determinazione prenotazione per chiamante MINISTERO e cf="
                                    + parametri.getCodiceFiscaleLavoratore());
                    prenotazione = finder.findPrenotazioneByCFEStatoDal(parametri.getCodiceFiscaleLavoratore(),
                            DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE, parametri.getDataAdesione());
                }
                // Chiamante diverso da MINISTERO, se c'e' id incontro lo carico
                else {
                    log.debug(
                            "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] determinazione prenotazione per chiamante "
                                    + parametri.getCodiceOperatore() + " cf=" + parametri.getCodiceFiscaleLavoratore()
                                    + " idIncontroSilp=" + parametri.getIdIncontroSilp());
                    prenotazione = finder.findPrenotazioneByIdContattoSilp(parametri.getIdIncontroSilp());
                }

                // Per tutti gli stati adesione diversi da quello iniziale si deve aggiornare lo
                // stato di eventuale incontro da erogare
                if (!DecodificheHelper.isStatoAdesioneIniziale(parametri.getCodStatoAdesione())) {
                    // Se trovo un incontro da erogare procedo anche con l'aggiornamento
                    // dell'incontro, altrimenti non c'e' nulla da fare

                    if (prenotazione != null && DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE
                            .equals(prenotazione.getIdStatoAppuntamento())) {
                        log.debug(
                                "[AdesioneApiServiceIndarioApimpl::notificaCambioStatoAdesione] trovata prenotazione con id="
                                        + prenotazione.getIdPrenotazione() + " idContattoSilp="
                                        + prenotazione.getIdSilInContatto());
                        parametri.setIdIncontroSilp(prenotazione.getIdSilInContatto());
                        if (parametri.getCodStatoIncontro() == null) {
                            parametri.setCodStatoIncontro(DecodificheHelper
                                    .getCodStatoIncontroDaStatoAdesione(parametri.getCodStatoAdesione()));
                            log.debug(
                                    "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] posto stato incontro pari a "
                                            + parametri.getCodStatoIncontro() + " in base a stato adesione ="
                                            + parametri.getCodStatoAdesione());
                        }

                        // SE la data contatto e' futura e si sta ponendo lo
                        // stato erogato viene posto invece lo stato DISDETTO
                        if (SilTimeUtils.isData1MaggioreData2(prenotazione.getSlot().getGiorno().getGiorno(),
                                SilTimeUtils.today())
                                && DecodificheHelper.COD_STATO_INCONTRO_EROGATO_ER
                                        .equals(parametri.getCodStatoIncontro())) {
                            log.warn("[AdesioneApiServiceImpl::notificaCambioStatoAdesione] prenotazionefutura  id="
                                    + prenotazione.getIdPrenotazione()
                                    + " stato posto a Disdetto (DI) invece di erogato");
                            parametri.setCodStatoIncontro(DecodificheHelper.COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
                        }

                        log.debug(
                                "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] richiamo notificaCambioStatoAppuntamentoInternal con stato "
                                        + parametri.getCodStatoIncontro());

                        // Assegnamento note per tracciare le info dell'adesione
                        parametri.setNote(buildNotePerIncontro(parametri));

                        // Imposto esplicitamente la data dell'esito che andra' impostata nell'incontro
                        parametri.setDataEsitoIncontro(parametri.getDataStatoAdesione());

                        Response r = calendarioApi.notificaCambioStatoAppuntamento(
                                (ParametriNotificaCambioStatoAppuntamento) parametri, null, null, null);
                        EsitoDTO esitoAggiornamentoAppuntamento = (EsitoDTO) r.getEntity();
                        log.debug(
                                "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] effettuata notifica di cambio stato appuntamento su adesione");
                        CalendarioApiServiceImpl.mergeInfoEsiti(result, esitoAggiornamentoAppuntamento);
                    } else {
                        log.debug(
                                "[AdesioneApiServiceImpl::notificaCambioStatoAdesione] prenotazione in stato da erogare non trovata, gestione incontro/mail non necessaria");
                        result.addMessage("prenotazione in stato da erogare non trovata per cf "
                                + parametri.getCodiceFiscaleLavoratore() + " chiamante "
                                + parametri.getCodiceOperatore() + " data adesione "
                                + SilTimeUtils.convertDataInStringa(parametri.getDataAdesione()));
                    }
                }

                helper.invioEventualeMailPerCambioStatoAdesione(parametri, prenotazione, result);

            }

            if (result.isEsitoPositivo() && result.getCodiceEsito() == null) {
                result.setCodiceEsito(EsitoDTO.E0000_CODICE_ESITO_POSITIVO);
            }

            log.debug("[AdesioneApiServiceImpl::notificaCambioStatoAdesione] END esito=" + result);
            return Response.ok(result).build();
        } catch (Exception ex) {
            log.error("[AdesioneApiServiceImpl::notificaCambioStatoAdesione] parametri=" + parametri, ex);
            Error err = new Error("Si e' verificato un errore nella notifica del cambio stato appuntamento");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("AdesioneApiServiceImpl", "notificaCambioStatoAdesione()",
                    "invocazione API AdesioneApiServiceImpl.notificaCambioStatoAdesione", "");
            watcher.stop();
        }
    }

    private String buildNotePerIncontro(ParametriNotificaCambioStatoAdesione parametri) {
        return "Ricevuto stato " + parametri.getCodStatoAdesione() + " in data "
                + SilTimeUtils.convertDataInStringa(parametri.getDataStatoAdesione()) + " per adesione del "
                + SilTimeUtils.convertDataInStringa(parametri.getDataAdesione());
    }

}
