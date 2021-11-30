/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import it.csi.pslp.pslcommonobj.dbdef.CalendarioDBDef;
import it.csi.pslp.pslcommonobj.dbdef.PrenotazioneDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SlotDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SlotHeaderDBDef;
import it.csi.pslp.pslcommonobj.dto.AmbitoDTO;
import it.csi.pslp.pslcommonobj.dto.CalendarioDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoSalvataggioIncontroDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriAnnulloCalendario;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoAppuntamento;
import it.csi.pslp.pslcommonobj.dto.ParametriRicercaCalendarioDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriRicercaSlotDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriSalvataggioIncontroDTO;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.SlotDTO;
import it.csi.pslp.pslcommonobj.dto.SlotHeaderDTO;
import it.csi.pslp.pslcommonobj.dto.SlotHeadersDTO;
import it.csi.pslp.pslcommonobj.dto.UtenteDTO;
import it.csi.pslp.pslcommonobj.filter.CalendarioFilter;
import it.csi.pslp.pslcommonobj.filter.PrenotazioneFilter;
import it.csi.pslp.pslcommonobj.filter.SlotFilter;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvdeWSImpl;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvinWSImpl;
import it.csi.pslp.pslorch.business.integration.MailUtils;
import it.csi.pslp.pslorch.business.portale.CalendarioApi;
import it.csi.pslp.pslorch.business.portale.impl.helper.DecodificheHelper;
import it.csi.pslp.pslorch.business.portale.impl.helper.EntityFinder;
import it.csi.pslp.pslorch.business.portale.impl.helper.NotificaCambioStatoAppuntamentoHelper;
import it.csi.pslp.pslorch.business.timer.BatchTimerBean;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.jedi.core.NullValue;
import it.csi.silos.jedi.core.QueryResult;
import it.csi.silos.silcommon.util.SilCommonUtils;
import it.csi.silpcommonobj.dati.incontri.EsitoAppuntamentoSilpDTO;
import it.csi.silpcommonobj.dati.incontri.StatoIncontroSilpDTO;
import it.csi.silpcommonobj.util.SilpCommonUtils;
import it.csi.util.performance.StopWatch;

@Component("calendarioApi")
public class CalendarioApiServiceImpl implements CalendarioApi {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private DAO dao;

    @Autowired
    private ParametroUtils parametroUtils;

    @Autowired
    private BatchTimerBean batchTimerBean;

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private EntityFinder finder;

    @Override
    public Response findSlots(ParametriRicercaSlotDTO parametriRicercaSlotDTO, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[CalendarioApiServiceImpl::findSlots] parametriRicercaSlotDTO=" + parametriRicercaSlotDTO);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("gruppoOperatore",
                    parametriRicercaSlotDTO.getParametriRicercaCalendarioDTO().getGruppoOperatore());
            params.put("codOperatore", parametriRicercaSlotDTO.getParametriRicercaCalendarioDTO().getCodOperatore());
            params.put("subcodice", getLongValueConsideringNull(
                    parametriRicercaSlotDTO.getParametriRicercaCalendarioDTO().getSubcodice()));
            params.put("codAmbito", parametriRicercaSlotDTO.getParametriRicercaCalendarioDTO().getCodAmbito());
            params.put("daGiorno", parametriRicercaSlotDTO.getDaGiorno());
            params.put("aGiorno", parametriRicercaSlotDTO.getAGiorno());
            int margine = Integer.parseInt(parametroUtils.getParametro("APPUNT_MARGINE_GG", "1"));
            params.put("margine", margine);
            QueryResult<SlotHeaderDBDef> qr = dao.findAll(SlotHeaderDBDef.class,
                    "portale/impl/CalendarioApiServiceImpl.findSlots", params, 0);
            SlotHeadersDTO result = new SlotHeadersDTO();
            for (SlotHeaderDTO slot : qr) {
                result.getEls().add(slot);
            }
            return Response.ok(result).build();
        } catch (Exception ex) {
            log.error("[CalendarioApiServiceImpl::findSlots] parametriRicercaSlotDTO=" + parametriRicercaSlotDTO, ex);
            Error err = new Error("Si e' verificato un errore nella ricerca degli slot");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("CalendarioApiServiceImpl", "findSlots()",
                    "invocazione API CalendarioApiServiceImpl.findSlots", "");
            watcher.stop();
        }
    }

    @Override
    public Response findIntervalloDisponibile(ParametriRicercaCalendarioDTO parametriRicerca,
            SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[CalendarioApiServiceImpl::findIntervalloDisponibile] parametriRicerca=" + parametriRicerca);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("gruppoOperatore", parametriRicerca.getGruppoOperatore());
            params.put("codOperatore", parametriRicerca.getCodOperatore());
            params.put("subcodice", getLongValueConsideringNull(parametriRicerca.getSubcodice()));
            params.put("codAmbito", parametriRicerca.getCodAmbito());
            int margine = Integer.parseInt(parametroUtils.getParametro("APPUNT_MARGINE_GG", "1"));
            params.put("margine", margine);
            String primoGiorno = dao.readString("portale/impl/CalendarioApiServiceImpl.findFirstFreeSlotDay", params);
            String ultimoGiorno = dao.readString("portale/impl/CalendarioApiServiceImpl.findLastFreeSlotDay", params);
            String[] result = { primoGiorno, ultimoGiorno };
            return Response.ok(result).build();
        } catch (Exception ex) {
            log.error("[CalendarioApiServiceImpl::findIntervalloDisponibile] parametriRicerca=" + parametriRicerca, ex);
            Error err = new Error(
                    "Si e' verificato un errore nella ricerca del primo giorno di calendario disponibile");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("CalendarioApiServiceImpl", "findIntervalloDisponibile()",
                    "invocazione API CalendarioApiServiceImpl.findIntervalloDisponibile", "");
            watcher.stop();
        }
    }

    /**
     * Prepara un parametro per le hashmap delle query di jedi
     * 
     * @param value
     * @return
     */
    private Object getLongValueConsideringNull(Object value) {
        if (value != null) {
            return value;
        } else {
            return NullValue.NULL_LONG;
        }
    }

    @Override
    @Transactional
    public Response notificaCambioStatoAppuntamento(ParametriNotificaCambioStatoAppuntamento parametri,
            SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[CalendarioApiServiceImpl::notificaCambioStatoAppuntamento] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            EsitoDTO result = notificaCambioStatoAppuntamentoInternal(parametri);

            if (!result.isEsitoPositivo()) {
                TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            }

            return Response.ok(result).build();
        } catch (Exception ex) {
            log.error("[CalendarioApiServiceImpl::notificaCambioStatoAppuntamento] parametri=" + parametri, ex);
            Error err = new Error("Si e' verificato un errore nella notifica del cambio stato appuntamento");
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("CalendarioApiServiceImpl", "notificaCambioStatoAppuntamento()",
                    "invocazione API CalendarioApiServiceImpl.notificaCambioStatoAppuntamento", "");
            watcher.stop();
        }
    }

    /**
     * Metodo che effetta le operazion a partire solo dai parametri e fornisce un
     * esito.
     * 
     * @param parametri
     * @return
     * @throws Exception
     */
    public EsitoDTO notificaCambioStatoAppuntamentoInternal(ParametriNotificaCambioStatoAppuntamento parametri)
            throws Exception {
        EsitoDTO result = new EsitoDTO();

        NotificaCambioStatoAppuntamentoHelper helper = new NotificaCambioStatoAppuntamentoHelper();

        // 1) CONTROLLI OBBLIGATORIETA' E COERENZA
        helper.validateInputParameters(result, parametri);

        // 2) AGGIORNAMENTO STATO INCONTRO su DB DI PSLP e chiamata a A SILP PER
        // AGGIORNAMENO STATO INCONTRO (metodo saveIncontro)
        if (result.isEsitoPositivo()) {
            EsitoSalvataggioIncontroDTO esitoSalvataggioIncontroPslpESilp = saveIncontro(null, parametri);
            // Ribalto eventuali errori nell'esito totale
            mergeInfoEsiti(result, esitoSalvataggioIncontroPslpESilp);

            // Se il salvataggio incontro su entrambi i sistemi e' andato bene si invia
            // eventuale mail
            invioEventualeMailPerCambioStatoAppuntamento(esitoSalvataggioIncontroPslpESilp.getPrenotazione(), result);

        }

        return result;
    }

    @Override
    @Transactional
    public Response annullaCalendario(ParametriAnnulloCalendario params, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[CalendarioApiServiceImpl::annullaCalendario] params=" + params);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            Date now = new Date();
            CalendarioFilter calendarioFilter = new CalendarioFilter();
            calendarioFilter.getIdCalendario().eq(params.getIdCalendario());
            CalendarioDTO calendario = dao.findFirst(CalendarioDBDef.class, calendarioFilter);
            calendario.setDAnnullamento(now);
            calendario.setDAggiorn(now);
            calendario.setCodUserAggiorn(params.getCodiceFiscaleOperatore());
            dao.update(CalendarioDBDef.class, calendario);
            PrenotazioneFilter filter = new PrenotazioneFilter();
            filter.getSlot().getGiorno().getPeriodo().getCalendario().getIdCalendario().eq(params.getIdCalendario());
            filter.getIdStatoAppuntamento().eq(DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE);
            EsitoDTO esito = new EsitoDTO();
            esito.setCodiceEsito(EsitoDTO.E0000_CODICE_ESITO_POSITIVO);
            for (PrenotazioneDTO prenotazione : dao.findAll(PrenotazioneDBDef.class, filter, 0)) {
                prenotazione.setIdStatoAppuntamento(DecodificheHelper.COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI);
                prenotazione.setdAggiorn(now);
                prenotazione.setCodUserAggiorn(params.getCodiceFiscaleOperatore());
                dao.update(PrenotazioneDBDef.class, prenotazione);
                invioEventualeMailPerCambioStatoAppuntamento(prenotazione, esito);
            }
            if (!esito.isEsitoPositivo()) {
                TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            }
            return Response.ok(esito).build();
        } catch (Exception ex) {
            log.error("[CalendarioApiServiceImpl::annullaCalendario] params=" + params, ex);
            Error err = new Error("Si e' verificato un errore nell'annullamento del calendario");
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("CalendarioApiServiceImpl", "annullaCalendario()",
                    "invocazione API CalendarioApiServiceImpl.annullaCalendario", "");
            watcher.stop();
        }
    }

    /**
     * Invia una mail al cittadino in caso di incontro "disdetto da operatore" o
     * "Non presentato"
     * 
     * @param prenotazione
     * @param esito
     * @throws Exception
     */
    private void invioEventualeMailPerCambioStatoAppuntamento(PrenotazioneDTO prenotazione, EsitoDTO esito)
            throws Exception {
        String logMethodName = "[CalendarioApiServiceImpl::invioEventualeMailPerCambioStatoAppuntamento] ";
        log.debug(logMethodName + "Verifica invio mail per appuntamento " + prenotazione);
        MessaggioType mailType = null;
        if (prenotazione != null) {
            String codAmbito = prenotazione.getSlot().getGiorno().getPeriodo().getCalendario().getAmbito()
                    .getCodAmbito();
            if (DecodificheHelper.COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI
                    .equals(prenotazione.getIdStatoAppuntamento())) {
                switch (codAmbito) {
                case AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI:
                    mailType = MessaggioType.APANN;
                    break;
                case AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA:
                    mailType = MessaggioType.RCANN;
                    break;
                default:
                    log.info(
                            "[CalendarioApiServiceImpl::invioEventualeMailPerCambioStatoAppuntamento] ambito non riconosciuto:"
                                    + codAmbito + "; nessuna mail inviata");
                }
            } else if (DecodificheHelper.COD_STATO_INCONTRO_NON_PRESENTATO_NP
                    .equals(prenotazione.getIdStatoAppuntamento())) {
                switch (codAmbito) {
                case AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI:
                    mailType = MessaggioType.APNPR;
                    break;
                case AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA:
                    mailType = MessaggioType.RCNPR;
                    break;
                default:
                    log.info(
                            "[CalendarioApiServiceImpl::invioEventualeMailPerCambioStatoAppuntamento] ambito non riconosciuto:"
                                    + codAmbito + "; nessuna mail inviata");
                }
            }
        }

        // lo stato dell'incontro richiede un invio mail, se il processo di aggiornameno
        // incontri pslp/silp ha avuto esito positivo
        if (mailType != null) {
            log.debug(logMethodName + "invio mail da effettuare di tipo " + mailType);
            if (esito.isEsitoPositivo()) {
                mailUtils.sendMail(prenotazione, mailType, false);
            } else {
                log.debug(logMethodName
                        + "invio mail non effettuato per esito negativo nel processo di aggiornamento incontro pslp/silp");
            }
        } else {
            log.debug(logMethodName + "invio mail non necessario");
        }
    }

    /**
     * Metodo pubblico di salvataggio prenotazione incontro esposto verso il
     * portale.
     */
    @Override
    @Transactional
    public Response saveIncontro(ParametriSalvataggioIncontroDTO params, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        log.info("[CalendarioApiServiceImpl::saveIncontro] params=" + params);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            EsitoSalvataggioIncontroDTO esito = null;

            // Spostamento incontro
            if (params.getIdPrenotazioneOld() != null) {
                ParametriSalvataggioIncontroDTO params1 = new ParametriSalvataggioIncontroDTO();
                params1.setIdPrenotazione(params.getIdPrenotazioneOld());
                params1.setCodiceFiscaleUtenteCollegato(params.getCodiceFiscaleUtenteCollegato());
                params1.setIdStatoAppuntamento(DecodificheHelper.COD_STATO_INCONTRO_SPOSTATO_SP);
                esito = saveIncontro(params1, null);
                if (esito.isEsitoPositivo()) {
                    esito = saveIncontro(params, null);
                }
            }
            // Salvataggio incontro
            else {
                esito = saveIncontro(params, null);
            }

            if (!esito.isEsitoPositivo()) {
                log.info("[CalendarioApiServiceImpl::saveIncontro] Rollback");
                TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            }

            return Response.ok(esito).build();
        } catch (Exception ex) {
            log.error("[CalendarioApiServiceImpl::saveIncontro] params=" + params, ex);
            Error err = new Error("Si e' verificato un errore nel salvataggio dell'incontro");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed("CalendarioApiServiceImpl", "saveIncontro()",
                    "invocazione API CalendarioApiServiceImpl.saveIncontro", "");
            watcher.stop();
        }
    }

    /**
     * Flusso eseguito a fronte di un inserimento / modifica di una prenotazione
     * incontro.
     */
    private EsitoSalvataggioIncontroDTO saveIncontro(ParametriSalvataggioIncontroDTO parametriSalvataggio,
            ParametriNotificaCambioStatoAppuntamento parametriNotifica) throws Exception {
        // Salva la prenotazione incontro su PSLP
        EsitoSalvataggioIncontroDTO esito = saveIncontroSuPortale(parametriSalvataggio, parametriNotifica);

        if (esito.isEsitoPositivo()) {
            // Salva l'appuntamento su silpsvin
            EsitoAppuntamentoSilpDTO esitoSilp = AdapterSilpsvinWSImpl.getInstance()
                    .saveAppuntamento(esito.getPrenotazione());
            if (esitoSilp.isEsitoPositivo()) {
                PrenotazioneDTO prenotazione = esito.getPrenotazione();
                prenotazione.setIdSilInContatto(esitoSilp.getAppuntamento().getIdIncontro());
                dao.update(PrenotazioneDBDef.class, prenotazione);
            } else {
                esito.setCodiceEsito(esitoSilp.getCodiceEsito());
                esito.addErrorMessage(esitoSilp.getDescrizioneEsito() + " - " + esitoSilp.getDettaglioEsitoAsString());
            }

            // Invia la mail solo per il caso di salvataggio di un nuovo appuntamento se
            // FLG_INVIO_CONFERMA_PRENOTAZ per il calendario vale 'S'
            if (esito.isEsitoPositivo() && parametriSalvataggio != null) {
                if (parametriSalvataggio.getIdPrenotazione() == null && "S".equals(esito.getPrenotazione().getSlot()
                        .getGiorno().getPeriodo().getCalendario().getFlgInvioConfermaPrenotaz())) {
                    MessaggioType t = null;
                    String codAmbito = esito.getPrenotazione().getSlot().getGiorno().getPeriodo().getCalendario()
                            .getAmbito().getCodAmbito();
                    switch (codAmbito) {
                    case AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI:
                        t = MessaggioType.APCON;
                        break;
                    case AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA:
                        t = MessaggioType.RCCON;
                        break;
                    default:
                        log.info("[CalendarioApiServiceImpl::saveIncontro] ambito non riconosciuto:" + codAmbito
                                + "; nessuna mail inviata");
                    }
                    if (t != null) {
                        mailUtils.sendMail(esito.getPrenotazione(), t,
                                parametriSalvataggio.getCodiceFiscaleUtenteCollegato(), false);
                    }
                }
                // else if ...
            }
            // else if ...
        }
        return esito;
    }

    /**
     * Salva un incontro (nuovo o per una modifica dello stato).
     * params.idPrenotazione deve essere null per un incontro non ancora esistente.
     * 
     * @param params
     * @return
     * @throws Exception
     */
    private EsitoSalvataggioIncontroDTO saveIncontroSuPortale(ParametriSalvataggioIncontroDTO parametriSalvataggio,
            ParametriNotificaCambioStatoAppuntamento parametriNotifica) throws Exception {
        EsitoSalvataggioIncontroDTO esito = new EsitoSalvataggioIncontroDTO();
        PrenotazioneDTO prenotazione = null;
        if (parametriSalvataggio != null) {
            prenotazione = gestioneSalvataggioDaPortaleWeb(parametriSalvataggio, esito);
        } else if (parametriNotifica != null) {
            prenotazione = gestioneSalvataggioDaNotificaEsterna(parametriNotifica, esito);
        }
        esito.setPrenotazione(prenotazione);
        return esito;
    }

    /**
     * Gestione delle azioni necessarie per salvare un incontro quando l'evento
     * proviene da una notifica esterna es: notifica di cambio stato adesione
     * 
     * @param parametriNotifica
     * @param esito
     * @return
     * @throws DAOException
     */
    private PrenotazioneDTO gestioneSalvataggioDaNotificaEsterna(
            ParametriNotificaCambioStatoAppuntamento parametriNotifica, EsitoSalvataggioIncontroDTO esito)
            throws DAOException {
        PrenotazioneDTO prenotazione;
        prenotazione = finder.findPrenotazioneByIdContattoSilp(parametriNotifica.getIdIncontroSilp());
        if (prenotazione == null || prenotazione.getIdSilInContatto() == null) {
            String err = "Prenotazione con identificativo silp " + parametriNotifica.getIdIncontroSilp()
                    + " per codice fiscale " + parametriNotifica.getCodiceFiscaleLavoratore() + " non presente";
            esito.impostaEsitoDiErrore(EsitoDTO.E0009_PARAMETRI_NON_CORRETTI, err);
        } else {
            if (!prenotazione.getUtente().getCfUtente().equals(parametriNotifica.getCodiceFiscaleLavoratore())) {
                String err = "Codice fiscale utente non corrispondente per la prenotazione "
                        + prenotazione.getIdPrenotazione();
                esito.impostaEsitoDiErrore(EsitoDTO.E0009_PARAMETRI_NON_CORRETTI, err);
            }
        }
        if (esito.isEsitoPositivo()) {
            log.debug("Trovata prenotazione id=" + prenotazione.getIdPrenotazione() + " per IdSilInContatto="
                    + parametriNotifica.getIdIncontroSilp());
            liberaPostoSlotSeNecessario(prenotazione, parametriNotifica.getCodStatoIncontro());
            prenotazione.setIdStatoAppuntamento(parametriNotifica.getCodStatoIncontro());
            // Assegno o aggiungo le note alla prenotazione
            setNoteInPrenotazione(prenotazione, parametriNotifica.getNote());
            prenotazione.setCodUserAggiorn(parametriNotifica.getCodiceOperatore());
            dao.update(PrenotazioneDBDef.class, prenotazione);
            log.debug("Aggiornata Prenotazione per notifica cambio stato " + prenotazione);
        }
        return prenotazione;
    }

    /**
     * Gestione delle azioni necessarie per salvare un incontro quando l'evento
     * proviene da pslp portale web
     * 
     * @param parametriSalvataggio
     * @param esito
     * @return
     * @throws DAOException
     * @throws Exception
     */
    private PrenotazioneDTO gestioneSalvataggioDaPortaleWeb(ParametriSalvataggioIncontroDTO parametriSalvataggio,
            EsitoSalvataggioIncontroDTO esito) throws DAOException, Exception {
        PrenotazioneDTO prenotazione;
        String statoIncontro = parametriSalvataggio.getIdStatoAppuntamento();
        if (parametriSalvataggio.getIdPrenotazione() == null) {

            // Controlli sulla prenotazione e sullo slot
            SlotFilter slotFilter = new SlotFilter();
            slotFilter.getIdSlot().eq(parametriSalvataggio.getIdSlot());
            SlotDTO slot = dao.findFirst(SlotDBDef.class, slotFilter);
            if ((slot.getNumMaxPrenotazioni() - slot.getNumPrenotazioniValide()) <= 0) {
                esito.impostaEsitoDiErrore(EsitoDTO.E0100_DISPONIBILITA_ESAURITA,
                        "Disponibilita' esaurita per lo slot " + slot.getIdSlot());
            }
            prenotazione = new PrenotazioneDTO();
            prenotazione.setSlot(slot);
            String[] check = checkIncontro(parametriSalvataggio, prenotazione);
            if (check != null) {
                esito.impostaEsitoDiErrore(check[0], check[1]);
            }
            if (esito.isEsitoPositivo()) {
                slot.setNumPrenotazioniValide(slot.getNumPrenotazioniValide() + 1);
                slot.setNumeroLock(slot.getNumeroLock() + 1);
                Map<String, Object> params = new HashMap<>(3);
                params.put("numeroLock", slot.getNumeroLock());
                params.put("numPrenotazioniValide", slot.getNumPrenotazioniValide());
                params.put("idSlot", slot.getIdSlot());
                // dao.update(SlotDBDef.class, slot);
                int n = dao.execute("portale/impl/CalendarioApiServiceImpl.updateSlot", params);
                if (n == 0) {
                    log.error(
                            "[CalendarioApiServiceImpl::gestioneSalvataggioDaPortaleWeb] Si e' verificato un update in concorrenza sullo slot "
                                    + slot.getIdSlot());
                    esito.impostaEsitoDiErrore(EsitoDTO.E0009_PARAMETRI_NON_CORRETTI,
                            "Si e' verificato un errore nella memorizzazione della prenotazione, si prega di riprovare.");
                } else {
                    prenotazione.setCodUserAggiorn(parametriSalvataggio.getCodiceFiscaleUtenteCollegato());
                    prenotazione.setCodUserInserim(parametriSalvataggio.getCodiceFiscaleUtenteCollegato());
                    prenotazione.setIdStatoAppuntamento(statoIncontro);
                    prenotazione.setIdSilRifAmbito(parametriSalvataggio.getIdAdesione());
                    prenotazione.setNote(parametriSalvataggio.getNote());
                    prenotazione.setUtente(new UtenteDTO(parametriSalvataggio.getIdUtente()));
                    prenotazione = dao.insert(PrenotazioneDBDef.class, prenotazione);
                    prenotazione = finder.findPrenotazioneByKey(prenotazione.getIdPrenotazione());
                    log.debug("[CalendarioApiServiceImpl::gestioneSalvataggioDaPortaleWeb] Inserita nuova Prenotazione "
                            + prenotazione);
                }
            }
        } else {
            prenotazione = finder.findPrenotazioneByKey(parametriSalvataggio.getIdPrenotazione());
            if (prenotazione != null) {
                String[] check = checkIncontro(parametriSalvataggio, prenotazione);
                if (check != null) {
                    esito.impostaEsitoDiErrore(check[0], check[1]);
                }
                if (esito.isEsitoPositivo()) {
                    liberaPostoSlotSeNecessario(prenotazione, statoIncontro);
                    prenotazione.setIdStatoAppuntamento(statoIncontro);
                    setNoteInPrenotazione(prenotazione, parametriSalvataggio.getNote());
                    dao.update(PrenotazioneDBDef.class, prenotazione);
                    log.debug("[CalendarioApiServiceImpl::gestioneSalvataggioDaPortaleWeb] Aggiornata Prenotazione "
                            + prenotazione);
                }
            } else {
                String err = "Prenotazione con Id=" + parametriSalvataggio.getIdPrenotazione() + " non trovata";
                log.error(err);
                esito.impostaEsitoDiErrore(EsitoDTO.E0009_PARAMETRI_NON_CORRETTI, err);
            }
        }
        return prenotazione;
    }

    /**
     * SE l'incontro e' in stato da erogare e si sta cercando di aggiornarne lo
     * stato in disdetto, spostato , non presentato , libera un posto dello slot
     * diminuendo di 1 le prenotazioni valide
     * 
     * @param prenotazione
     * @param statoIncontroDaSalvare
     * @throws DAOException
     */
    private void liberaPostoSlotSeNecessario(PrenotazioneDTO prenotazione, String statoIncontroDaSalvare)
            throws DAOException {
        if (DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE.equals(prenotazione.getIdStatoAppuntamento())
                && prenotazione.getSlot().getNumPrenotazioniValide() > 0) {
            if (SilCommonUtils.in(statoIncontroDaSalvare, DecodificheHelper.COD_STATO_INCONTRO_DISDETTO_OPERATORE_DI,
                    DecodificheHelper.COD_STATO_INCONTRO_DISDETTO_DA_CITTADINO_DC,
                    DecodificheHelper.COD_STATO_INCONTRO_NON_PRESENTATO_NP,
                    DecodificheHelper.COD_STATO_INCONTRO_SPOSTATO_SP)) {
                prenotazione.getSlot().setNumPrenotazioniValide(prenotazione.getSlot().getNumPrenotazioniValide() - 1);
                dao.update(SlotDBDef.class, prenotazione.getSlot());
                log.debug("[CalendarioApiServiceImpl::liberaPostoSlotSeNecessario] Aggiornato Slot liberando un posto "
                        + prenotazione.getSlot());
            }
        }
    }

    /**
     * Verifica che lo stato di un incontro sia compatibile con il salvataggio - un
     * incontro non puo' essere erogato nel futuro - un incontro non puo' essere
     * modificato se lo stato e' finale - ...
     * 
     * @param slot
     * @return l'eventuale codice di errore da restituire ed un messaggio di errore
     *         specifico
     */
    private String[] checkIncontro(ParametriSalvataggioIncontroDTO parametriSalvataggioIncontroDTO,
            PrenotazioneDTO prenotazione) throws Exception {

        String codiceStatoIncontro = parametriSalvataggioIncontroDTO.getIdStatoAppuntamento();

        // Verifica che non venga erogato un incontro in una data futura
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (prenotazione.getSlot().getDataOra().after(now)
                && DecodificheHelper.COD_STATO_INCONTRO_EROGATO_ER.equals(codiceStatoIncontro)) {
            return new String[] { EsitoDTO.E0101_EROGAZIONE_IN_DATA_FUTURA,
                    "Impossibile impostare in erogato un incontro con data futura "
                            + df.format(prenotazione.getSlot().getDataOra()) };
        }

        // Verifica che un incontro da erogare non sia nel passato
        if (prenotazione.getSlot().getDataOra().before(now)
                && DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE.equals(codiceStatoIncontro)) {
            return new String[] { EsitoDTO.E0010_ERRORE_ESECUZIONE,
                    "Impossibile impostare un incontro da erogare con data passata "
                            + df.format(prenotazione.getSlot().getDataOra()) };
        }

        // Verifica che lo stato corrente dell'incontro non sia finale
        if (prenotazione.getIdPrenotazione() != null && !parametriSalvataggioIncontroDTO.getIdStatoAppuntamento()
                .equals(prenotazione.getIdStatoAppuntamento())) {
            Map<String, StatoIncontroSilpDTO> statiIncontro = AdapterSilpsvdeWSImpl.getInstance()
                    .getMapStatiIncontroPerCodAnpal(parametriSalvataggioIncontroDTO.getCodiceFiscaleUtenteCollegato());
            StatoIncontroSilpDTO statoIncontro = statiIncontro.get(prenotazione.getIdStatoAppuntamento());
            if (statoIncontro != null && "S".equals(statoIncontro.getFlgFinale())) {
                return new String[] { EsitoDTO.E0102_MODIFICA_INCONTRO_IN_STATO_FINALE,
                        "Impossibile modificare lo stato della prenotazione " + prenotazione.getIdPrenotazione()
                                + " perche' gia' in stato finale (" + prenotazione.getIdStatoAppuntamento() + ")" };
            }
        }

        // altro controllo di sicurezza per evitare di salvare un
        // incontro da erogare in presenza di un altro
        if (prenotazione.getIdPrenotazione() == null && parametriSalvataggioIncontroDTO.getIdPrenotazioneOld() != null
                && DecodificheHelper.COD_STATO_INCONTRO_DA_EROGARE_DE.equals(codiceStatoIncontro)) {
            PrenotazioneDTO prenotazioneDaErogare = finder.findPrenotazioneByIdUtenteCFEStato(
                    parametriSalvataggioIncontroDTO.getIdUtente(), codiceStatoIncontro);
            if (prenotazioneDaErogare != null) {
                return new String[] { EsitoDTO.E0010_ERRORE_ESECUZIONE,
                        "Impossibile procedere, e' presente un incontro da erogare il "
                                + df.format(prenotazioneDaErogare.getSlot().getDataOra()) };
            }
        }

        return null;
    }

    /**
     * Imposta nell'oggetto prenotazione delle note, se assenti, o concatena a
     * quelle presenti
     * 
     * @param p
     * @param note
     */
    private void setNoteInPrenotazione(PrenotazioneDTO p, String note) {
        if (p.getNote() == null) {
            p.setNote(note);
        } else if (note != null && p.getNote().indexOf(note) <= 0) {
            p.setNote(SilpCommonUtils.concat(p.getNote(), note));

        }

    }

    /**
     * Adegua i messaggi e eventualmente codice errore e descrizione nell'esito
     * Totale in base alla situazione dell'esito attuale - ribalta tutti i messaggi
     * dall'attuale al totale - se l'attuale e' negativo e il totale no lo rende
     * negativo impostando il codice di errore e messaggio
     * 
     * @param esitoTotale
     * @param esitoSilp
     */
    public static void mergeInfoEsiti(EsitoDTO esitoTotale, EsitoDTO esitoSilp) {
        // Ribalto i messaggi
        for (String messaggio : esitoSilp.getDettagliEsito()) {
            esitoTotale.addMessage(messaggio);
        }

        // Ribalto il codice esito e eventuale descrizione
        if (esitoTotale.isEsitoPositivo() && !esitoSilp.isEsitoPositivo()) {
            esitoTotale.setEsitoPositivo(false);
            esitoTotale.setCodiceEsito(esitoSilp.getCodiceEsito());
            esitoTotale.setDescrizioneEsito(esitoSilp.getDescrizioneEsito());
            // e per qualche motivo la descrizione generica dell'esito non e' valorizzata ,
            // imposto il primo messaggio (almeno e' qualcosa di comprensibile)
            if (esitoTotale.getDescrizioneEsito() == null) {
                esitoTotale.setDescrizioneEsito(esitoTotale.getDettagliEsito().iterator().next());
            }
        }

        // Imposto il codice esito positivo di default nel caso questo manchi
        if (esitoTotale.isEsitoPositivo() && esitoTotale.getCodiceEsito() == null) {
            esitoTotale.setCodiceEsito(EsitoDTO.E0000_CODICE_ESITO_POSITIVO);
        }
    }

    @Override
    public Response invioMail(SecurityContext securityContext, HttpHeaders httpHeaders,
            HttpServletRequest httpRequest) {
        String esito = batchTimerBean.invioMail(false);
        return Response.ok(esito).build();
    }

}
