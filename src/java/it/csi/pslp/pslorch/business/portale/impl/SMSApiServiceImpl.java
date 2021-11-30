/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import it.csi.pslp.pslcommonobj.dbdef.ChiamataSMSDBDef;
import it.csi.pslp.pslcommonobj.dbdef.EnteServizioLavoroDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SMSDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SistemaChiamanteDBDef;
import it.csi.pslp.pslcommonobj.dbdef.StatoSMSDBDef;
import it.csi.pslp.pslcommonobj.dto.ChiamataSMSDTO;
import it.csi.pslp.pslcommonobj.dto.EnteServizioLavoroDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriInvioSMS;
import it.csi.pslp.pslcommonobj.dto.SMSDTO;
import it.csi.pslp.pslcommonobj.dto.SistemaChiamanteDTO;
import it.csi.pslp.pslcommonobj.dto.StatoSMSDTO;
import it.csi.pslp.pslcommonobj.dto.sms.CancellazioneMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.CancellazioneMessaggioOutputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.DatiEnteDTO;
import it.csi.pslp.pslcommonobj.dto.sms.MessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ModificaMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ModificaMessaggioOutputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.PrenotazioneMessaggioDatiInvioDTO;
import it.csi.pslp.pslcommonobj.dto.sms.PrenotazioneMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.PrenotazioneMessaggioOutputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ReinvioMessaggioInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.ReinvioMessaggioOutputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.RicercaMessaggiInputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.RicercaMessaggiOutputDTO;
import it.csi.pslp.pslcommonobj.dto.sms.RicercaMessaggiSMSDTO;
import it.csi.pslp.pslcommonobj.filter.EnteServizioLavoroFilter;
import it.csi.pslp.pslcommonobj.filter.SMSFilter;
import it.csi.pslp.pslcommonobj.filter.SistemaChiamanteFilter;
import it.csi.pslp.pslcommonobj.filter.StatoSMSFilter;
import it.csi.pslp.pslorch.business.portale.SMSApi;
import it.csi.pslp.pslorch.business.portale.impl.SMSApiServiceUtils.Esito;
import it.csi.pslp.pslorch.business.timer.BatchTimerBean;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.GsonUtils;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.jedi.core.StdRowReader;
import it.csi.silos.jedi.engine.DBUtils;
import it.csi.silos.silcommon.util.ValidatoreMessaggioSms;
import it.csi.util.performance.StopWatch;

@Component("SMSApiServiceImpl")
public class SMSApiServiceImpl implements SMSApi {

    private static final String[] ESITO_OK = { "OK", "Operazione conclusa correttamente" };

    private static final String[] ESITO_E00 = { "E00", "Errore interno di sistema" };

    private static final String[] ESITO_E02 = { "E02", "E' presente un errore in uno o piu' parametri" };

    private static final String[] ESITO_E03 = { "E03", "Operatore non riconosciuto" };

    private static final String[] ESITO_E04 = { "E04", "Ente proprietario non presente" };

    private static final String[] ESITO_E05 = { "E05",
            "Non e' possibile procedere con l'operazione in quanto l'SMS e' gia' stato inviato" };

    private static final String[] ESITO_E07 = { "E07", "Operatore non associato al contratto richiesto" };

    private static final String[] ESITO_E08 = { "E08", "Elaborazione batch in corso" };

    private static final String[] ESITO_E10 = { "E10", "E' stato utilizzato il numero massimo di caratteri disponibili per il messaggio ed e' presente un carattere due punti non seguito da uno spazio" };

    private static final String[] ESITO_E11 = { "E11", "E' stato utilizzato il numero massimo di caratteri disponibili per il messaggio" };

    private static final String[] ESITO_W01 = { "W01",
            "Operazione conclusa positivamente ma non e' stato trovato nessun SMS" };

    private static final String[] ESITO_W02 = { "W02",
            "SMS non trovato, si procede comunque con la prenotazione del nuovo messaggio" };

    private static final String[] ESITO_W03 = { "W03",
    "Operazione conclusa positivamente ma uno o piu' SMS richiesti erano gia' stati prenotati/inviati" };

    private static final String[] ESITO_E09 = { "E09",
            "Impossibile trasmettere, uno o piu' SMS risultano essere gia' stati prenotati/inviati" };

    private static final Logger LOGGER = Logger.getLogger(Constants.COMPONENT_NAME);

    private static final String CLASSE = "SMSApiServiceImpl";

    @Autowired
    private DAO dao;

    @Autowired
    private SMSApiServiceUtils smsUtils;

    @Autowired
    private BatchTimerBean batchTimerBean;

    @Autowired
    private ParametroUtils parametroUtils;

    @Override
    @Transactional
    public Response prenotaSMS(PrenotazioneMessaggioInputDTO parametri, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[" + SMSApiServiceImpl.CLASSE + "::prenotaSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Esito esito = new Esito();
        PrenotazioneMessaggioOutputDTO result = new PrenotazioneMessaggioOutputDTO();
        try {
            checkParametriComuni(parametri, esito);
            SistemaChiamanteDTO sistemaChiamante = checkSistemaChiamante(parametri.getCaller(),
                    parametri.getCodiceContratto(), esito);
            EnteServizioLavoroDTO ente = checkEnte(parametri.getDatiEnte(), esito);
            Date now = new Date();
            if (ValidatoreMessaggioSms.contaLunghezza(parametri.getMessaggio()) > 160) {
              if(ValidatoreMessaggioSms.contaDuePuntiConNumeri(parametri.getMessaggio())>0) {
                esito.setEsito(ESITO_E10, ": il messaggio supera i 160 caratteri");
              }
              else {
                esito.setEsito(ESITO_E11, ": il messaggio supera i 160 caratteri");
              }
            } else if (StringUtils.isEmpty(parametri.getCodiceContratto())) {
                esito.setEsito(ESITO_E02, ": codice contratto non presente");
            } else if (parametri.getCellulare() == null || parametri.getCellulare().isEmpty()) {
                esito.setEsito(ESITO_E02, ": cellulare non presente");
            } else if (StringUtils.isEmpty(parametri.getMessaggio())) {
                esito.setEsito(ESITO_E02, ": messaggio non presente");
            } else {

                // Legge gli SMS inviati nelle ultime N ore in stato !='E' con testo uguale a
                // quello da inviare
                final Map<String, Long> smsRecenti = new HashMap<>();
                Map<String, Object> params = new HashMap<>();
                params.put("testoMessaggio", parametri.getMessaggio());
                String oreReinvio = ParametroUtils.getInstance().getParametro(ParametroUtils.SMS_ORE_REINVIO, "24");
                params.put("oreReinvio", oreReinvio);
                dao.eachRow("portale/impl/SMSApiServiceImpl.smsRecenti", params, new StdRowReader() {
                    public void stdRowReaded(ResultSet rs) throws DAOException {
                        String cellulare = DBUtils.getString(rs, "NUM_CELL");
                        Long idSMS = DBUtils.getLong(rs, "ID_SMS");
                        smsRecenti.put(cellulare, idSMS);
                    }
                });

                result.setDatiInvio(new ArrayList<>());
                for (String cellulare : parametri.getCellulare()) {
                    PrenotazioneMessaggioDatiInvioDTO datiInvio = new PrenotazioneMessaggioDatiInvioDTO();
                    SMSDTO sms = new SMSDTO();
                    Long idSMS = smsRecenti.get(cellulare);
                    if (idSMS != null) {
                        sms.setIdSMS(idSMS);
                        esito.setEsito(ESITO_W03);
                        datiInvio.setCodiceEsito(ESITO_E09[0]);
                    } else {
                        sms.setCodiceContratto(parametri.getCodiceContratto());
                        sms.setCellulare(cellulare);
                        sms.setDataStato(now);
                        sms.setSistemaChiamante(sistemaChiamante);
                        sms.setEnte(ente);
                        sms.setMessaggio(parametri.getMessaggio());
                        sms.setStato(findStato(SMSDTO.STATO_SALVATO));
                        sms.setDataUltimoAggiornamento(now);
                        sms.setDataInserimento(now);
                        sms.setDataAggiornamento(now);
                        sms = dao.insert(SMSDBDef.class, sms);
                        datiInvio.setCodiceEsito(ESITO_OK[0]);
                    }
                    ChiamataSMSDTO chiamata = new ChiamataSMSDTO();
                    chiamata.setCodiceEsito(ESITO_OK[0]);
                    chiamata.setDescrizioneEsito(ESITO_OK[1]);
                    chiamata.setMetodoChiamato("prenotaSMS");
                    PrenotazioneMessaggioInputDTO parametriTracciamento = new PrenotazioneMessaggioInputDTO();
                    parametriTracciamento.setCaller(parametri.getCaller());
                    List<String> cellulareTracciamento = new ArrayList<>();
                    cellulareTracciamento.add(cellulare);
                    parametriTracciamento.setCellulare(cellulareTracciamento);
                    parametriTracciamento.setDatiEnte(parametri.getDatiEnte());
                    parametriTracciamento.setMessaggio(parametri.getMessaggio());
                    chiamata.setMsgInput(GsonUtils.toGsonString(parametriTracciamento));
                    chiamata.setOraInizio(now);
                    chiamata.setOraFine(now);
                    chiamata.setSistemaChiamante(sistemaChiamante);
                    chiamata.setSms(sms);
                    dao.insert(ChiamataSMSDBDef.class, chiamata);
                    
                    datiInvio.setCellulare(cellulare);
                    datiInvio.setIdSMS(sms.getIdSMS());
                    
                    result.getDatiInvio().add(datiInvio);
                }
                if (esito.getCodiceEsito() == null) {
                    esito.setEsito(ESITO_OK);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::prenotaSMS] parametri=" + parametri, ex);
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            esito.setEsito(ESITO_E00);
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "prenotaSMS()",
                    "invocazione API SMSApiServiceImpl.prenotaSMS", "");
            watcher.stop();
        }
        result.setCodiceEsito(esito.getCodiceEsito());
        result.setDescrizioneEsito(esito.getDescrizioneEsito());
        return Response.ok(result).build();
    }

    @Override
    @Transactional
    public Response modificaSMS(ModificaMessaggioInputDTO parametri, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::modificaSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Esito esito = new Esito();
        ModificaMessaggioOutputDTO result = new ModificaMessaggioOutputDTO();
        try {
            checkParametriComuni(parametri, esito);
            checkSistemaChiamante(parametri.getCaller(), null, esito);
            checkEnte(parametri.getDatiEnte(), esito);
            checkElaborazioneBatchInCorso(parametri.getCaller(), esito);
            SMSDTO sms = checkSMSModificabile(parametri.getIdSMS(), esito);
            if (esito.getCodiceEsito() == null) {
                Date now = new Date();
                sms.setCellulare(parametri.getCellulare());
                sms.setMessaggio(parametri.getMessaggio());
                sms.setDataAggiornamento(now);
                dao.update(SMSDBDef.class, sms);
                esito.setEsito(ESITO_OK);
            }
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::modificaSMS] parametri=" + parametri, ex);
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            esito.setEsito(ESITO_E00);
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "modificaSMS()",
                    "invocazione API SMSApiServiceImpl.modificaSMS", "");
            watcher.stop();
        }
        smsUtils.traccia("modificaSMS", parametri.getCaller(), parametri, parametri.getIdSMS(), esito, null);
        result.setCodiceEsito(esito.getCodiceEsito());
        result.setDescrizioneEsito(esito.getDescrizioneEsito());
        return Response.ok(result).build();
    }

    @Override
    @Transactional
    public Response reinviaSMS(ReinvioMessaggioInputDTO parametri, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::reinviaSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Esito esito = new Esito();
        ReinvioMessaggioOutputDTO result = new ReinvioMessaggioOutputDTO();
        try {
            checkParametriComuni(parametri, esito);
            checkSistemaChiamante(parametri.getCaller(), null, esito);
            checkEnte(parametri.getDatiEnte(), esito);
            checkElaborazioneBatchInCorso(parametri.getCaller(), esito);
            SMSFilter filter = new SMSFilter();
            filter.getIdSMS().eq(parametri.getIdSMS()); 
            SMSDTO sms = dao.findFirst(SMSDBDef.class, filter);
            if (esito.getCodiceEsito() == null) {
                if (sms == null) {
                    esito.setEsito(ESITO_W01);
                } else {
                    Date now = new Date();
                    SMSDTO newsms = new SMSDTO();
                    newsms.setCodiceContratto(sms.getCodiceContratto());
                    newsms.setCellulare(sms.getCellulare());
                    newsms.setDataStato(now);
                    newsms.setEnte(sms.getEnte());
                    newsms.setMessaggio(sms.getMessaggio());
                    newsms.setStato(findStato(SMSDTO.STATO_SALVATO));
                    newsms.setDataUltimoAggiornamento(now);
                    newsms.setDataInserimento(now);
                    newsms.setDataAggiornamento(now);
                    newsms = dao.insert(SMSDBDef.class, newsms);
                    sms.setIdSMSReinoltro(newsms.getIdSMS());
                    sms.setDataUltimoAggiornamento(now);
                    if (SMSDTO.STATO_SALVATO.equals(sms.getStato().getCodice()))
                        sms.setDataCancellazione(now);
                    dao.update(SMSDBDef.class, sms);
                    esito.setEsito(ESITO_OK);
                    result.setIdSMS(newsms.getIdSMS());
                }
            }
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::reinviaSMS] parametri=" + parametri, ex);
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            esito.setEsito(ESITO_E00);
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "reinviaSMS()",
                    "invocazione API SMSApiServiceImpl.reinviaSMS", "");
            watcher.stop();
        }
        smsUtils.traccia("reinviaSMS", parametri.getCaller(), parametri, parametri.getIdSMS(), esito, null);
        result.setCodiceEsito(esito.getCodiceEsito());
        result.setDescrizioneEsito(esito.getDescrizioneEsito());
        return Response.ok(result).build();
    }

    @Override
    @Transactional
    public Response cancellaSMS(CancellazioneMessaggioInputDTO parametri, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::cancellaSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Esito esito = new Esito();
        CancellazioneMessaggioOutputDTO result = new CancellazioneMessaggioOutputDTO();
        try {
            checkParametriComuni(parametri, esito);
            checkSistemaChiamante(parametri.getCaller(), null, esito);
            checkEnte(parametri.getDatiEnte(), esito);
            checkElaborazioneBatchInCorso(parametri.getCaller(), esito);
            SMSDTO sms = checkSMSModificabile(parametri.getIdSMS(), esito);
            if (esito.getCodiceEsito() == null) {
                Date now = new Date();
                sms.setDataCancellazione(now);
                sms.setDataAggiornamento(now);
                dao.update(SMSDBDef.class, sms);
                esito.setEsito(ESITO_OK);
            }
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::cancellaSMS] parametri=" + parametri, ex);
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            esito.setEsito(ESITO_E00);
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "cancellaSMS()",
                    "invocazione API SMSApiServiceImpl.cancellaSMS", "");
            watcher.stop();
        }
        smsUtils.traccia("cancellaSMS", parametri.getCaller(), parametri, parametri.getIdSMS(), esito, null);
        result.setCodiceEsito(esito.getCodiceEsito());
        result.setDescrizioneEsito(esito.getDescrizioneEsito());
        return Response.ok(result).build();
    }

    @Override
    public Response cercaSMS(RicercaMessaggiInputDTO parametri, SecurityContext securityContext,
            HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::cercaSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Esito esito = new Esito();
        RicercaMessaggiOutputDTO result = new RicercaMessaggiOutputDTO();
        try {
            checkParametriComuni(parametri, esito);
            checkSistemaChiamante(parametri.getCaller(), null, esito);
            checkEnte(parametri.getDatiEnte(), esito);
            if (esito.getCodiceEsito() == null) {
                SMSFilter filter = new SMSFilter();
                if (parametri.getIdSMS() != null) {
                    filter.getIdSMS().eq(parametri.getIdSMS());
                }
                result.setSms(new ArrayList<>());
                for (SMSDTO sms : dao.findAll(SMSDBDef.class, filter, 1000)) {
                    RicercaMessaggiSMSDTO r = new RicercaMessaggiSMSDTO();
                    r.setIdSMS(sms.getIdSMS());
                    r.setCellulare(sms.getCellulare());
                    r.setTestoMessaggio(sms.getMessaggio());
                    r.setCodicePrenotazioneGateway(sms.getCodicePrenotazione());
                    DatiEnteDTO datiEnte = new DatiEnteDTO();
                    datiEnte.setGruppoOperatore(sms.getEnte().getGruppoOperatore());
                    datiEnte.setCodiceOperatore(sms.getEnte().getCodiceOperatore());
                    datiEnte.setSubcodice(sms.getEnte().getSubcodice());
                    r.setDatiEnte(datiEnte);
                    r.setCodiceStato(sms.getStato().getCodice());
                    r.setDescrizioneStato(sms.getStato().getDescrizione());
                    r.setDataStato(sms.getDataStato());
                    r.setCodiceErrore(sms.getCodiceErrore());
                    r.setErrore(sms.getDescrizioneErrore());
                    result.getSms().add(r);
                }
                if (result.getSms().isEmpty())
                    esito.setEsito(ESITO_W01);
                else
                    esito.setEsito(ESITO_OK);
            }
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::cercaSMS] parametri=" + parametri, ex);
            esito.setEsito(ESITO_E00);
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "cercaSMS()", "invocazione API SMSApiServiceImpl.cercaSMS", "");
            watcher.stop();
        }
        smsUtils.traccia("cercaSMS", parametri.getCaller(), parametri, parametri.getIdSMS(), esito, null);
        result.setCodiceEsito(esito.getCodiceEsito());
        result.setDescrizioneEsito(esito.getDescrizioneEsito());
        return Response.ok(result).build();
    }

    // @Override
    public Response sendSMS(ParametriInvioSMS parametri, SecurityContext securityContext, HttpHeaders httpHeaders,
            HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::sendSMS] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            List<SMSDTO> bloccoInvii = new ArrayList<>();
            SMSDTO sms = new SMSDTO();
            bloccoInvii.add(sms);
            String result = smsUtils.sendSMS(bloccoInvii);
            return Response.ok(result).build();
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::sendSMS] parametri=" + parametri, ex);
            Error err = new Error("Si e' verificato un errore nell'invio del messaggio");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "sendSMS()", "invocazione API SMSApiServiceImpl.sendSMS", "");
            watcher.stop();
        }
    }

    /**
     * Esegue immediatamente il batch di invio SMS, indipendentemente dalla
     * schedulazione (purche' non ci sia un'esecuzione gia' in corso)
     */
    public Response batchInviaSMS(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        LOGGER.info("[SMSApiServiceImpl::batchInviaSMS]");
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {
            String s = batchTimerBean.invioSMS(false);
            return Response.ok(s).build();
        } catch (Exception ex) {
            LOGGER.error("[SMSApiServiceImpl::sendSMS]");
            Error err = new Error("Si e' verificato un errore nell'esecuzione del batch di invio SMS");
            return Response.serverError().entity(err).status(500).build();
        } finally {
            watcher.dumpElapsed(SMSApiServiceImpl.CLASSE, "batchInviaSMS()",
                    "invocazione API SMSApiServiceImpl.batchInviaSMS", "");
            watcher.stop();
        }
    }

    private SistemaChiamanteDTO findSistemaChiamante(String codice) throws DAOException {
        SistemaChiamanteFilter filter = new SistemaChiamanteFilter();
        filter.getCodice().eq(codice);
        return dao.findFirst(SistemaChiamanteDBDef.class, filter);
    }

    private StatoSMSDTO findStato(String codice) throws DAOException {
        StatoSMSFilter filter = new StatoSMSFilter();
        filter.getCodice().eq(codice);
        return dao.findFirst(StatoSMSDBDef.class, filter);
    }

    private EnteServizioLavoroDTO findEnte(String gruppoOperatore, Integer codiceOperatore, Integer subcodice)
            throws DAOException {
        EnteServizioLavoroFilter filter = new EnteServizioLavoroFilter();
        filter.getGruppoOperatore().eq(gruppoOperatore);
        filter.getCodiceOperatore().eq(codiceOperatore);
        filter.getSubcodice().eq(subcodice);
        EnteServizioLavoroDTO ente = dao.findFirst(EnteServizioLavoroDBDef.class, filter);
        return ente;
    }

    private void checkParametriComuni(MessaggioInputDTO parametri, Esito esito) {
        if (StringUtils.isEmpty(parametri.getCaller())) {
            esito.setEsito(ESITO_E02, ": caller non presente");
            return;
        }
        if (parametri.getDatiEnte() == null || parametri.getDatiEnte().getGruppoOperatore() == null) {
            esito.setEsito(ESITO_E02, ": dati ente non presente");
            return;
        }
    }

    private SistemaChiamanteDTO checkSistemaChiamante(String caller, String contratto, Esito esito)
            throws DAOException {
        Date now = new Date();
        SistemaChiamanteDTO sistemaChiamante = findSistemaChiamante(caller);
        if (sistemaChiamante == null || sistemaChiamante.getDataInizio().after(now)
                || (sistemaChiamante.getDataFine() != null && sistemaChiamante.getDataFine().before(now))) {
            esito.setEsito(ESITO_E03);
            return null;
        }
        if (contratto != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("caller", caller);
            params.put("contratto", contratto);
            Integer n = dao.readInteger("portale/impl/SMSApiServiceImpl.checkContratto", params);
            if (n == null || n == 0) {
                esito.setEsito(ESITO_E07);
                return null;
            }
        }
        return sistemaChiamante;
    }

    private void checkElaborazioneBatchInCorso(String caller, Esito esito) throws Exception {
        String p = parametroUtils.getParametro("SMS_" + caller, "N");
        if ("S".equals(p)) {
            esito.setEsito(ESITO_E08);
        }
    }

    private EnteServizioLavoroDTO checkEnte(DatiEnteDTO datiEnte, Esito esito) throws DAOException {
        EnteServizioLavoroDTO ente = findEnte(datiEnte.getGruppoOperatore(), datiEnte.getCodiceOperatore(),
                datiEnte.getSubcodice());
        if (ente == null) {
            esito.setEsito(ESITO_E04);
            return null;
        }
        return ente;
    }

    private SMSDTO checkSMSModificabile(Long idSMS, Esito esito) throws DAOException {
        SMSFilter filter = new SMSFilter();
        filter.getIdSMS().eq(idSMS);
        SMSDTO sms = dao.findFirst(SMSDBDef.class, filter);
        if (sms == null) {
            esito.setEsito(ESITO_W01);
            return null;
        }
        String stato = sms.getStato().getCodice();
        if (!SMSDTO.STATO_SALVATO.equals(stato)) {
            esito.setEsito(ESITO_E05);
        }
        return sms;
    }

}
