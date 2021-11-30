/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import it.csi.pslp.pslcommonobj.dbdef.AmbitoTipoDocumentoDBDef;
import it.csi.pslp.pslcommonobj.dbdef.DocumentoBlobDBDef;
import it.csi.pslp.pslcommonobj.dbdef.DocumentoDBDef;
import it.csi.pslp.pslcommonobj.dto.AmbitoDTO;
import it.csi.pslp.pslcommonobj.dto.AmbitoTipoDocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.DocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoRicercaDocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoSalvataggioDocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriNotificaCambioStatoDocumento;
import it.csi.pslp.pslcommonobj.dto.ParametriRicercaDocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.StatoDocumentoDTO;
import it.csi.pslp.pslcommonobj.filter.AmbitoTipoDocumentoFilter;
import it.csi.pslp.pslcommonobj.filter.DocumentoBlobFilter;
import it.csi.pslp.pslcommonobj.filter.DocumentoFilter;
import it.csi.pslp.pslorch.business.common.TracciamentoUtils;
import it.csi.pslp.pslorch.business.integration.MailUtils;
import it.csi.pslp.pslorch.business.portale.DocumentiApi;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggiUtils;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.jedi.core.QueryResult;
import it.csi.silos.silcommon.util.SilCommonUtils;
import it.csi.util.performance.StopWatch;

@Component("documentiApi")
public class DocumentiApiServiceImpl implements DocumentiApi {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private DAO dao;

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private MessaggiUtils messaggiUtils;

    @Autowired
    private TracciamentoUtils tracciamentoUtils;

    @Override
    public Response findDocumenti(ParametriRicercaDocumentoDTO parametri, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        log.info("[DocumentiApiServiceImpl::findDocumenti] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        EsitoRicercaDocumentoDTO esito = new EsitoRicercaDocumentoDTO();
        try {

            Vector<DocumentoDTO> elencoDocumenti = findDocumentiPrivate(parametri);

            esito.setElencoDocumenti(elencoDocumenti);
            log.debug("[DocumentiApiServiceImpl::findDocumenti] elencoDocumenti.size=" + elencoDocumenti.size());
            return Response.ok(esito).build();
        } catch (Exception ex) {
            log.error("[DocumentiApiServiceImpl::findDocumenti] parametri=" + parametri, ex);
            // tracciamentoUtils.tracciaKo(TracciamentoUtils.LOAD_DOCUMENTO, httpRequest,
            // null, "Errore di sistema: "+ex.getClass().getName()+";
            // idDocumento="+idDocumento);
            esito.addErrorMessage("Si e' verificato un errore generico di sistema ex=" + ex.getMessage());
            return Response.serverError().entity(esito).build();
        } finally {
            watcher.dumpElapsed("DocumentiApiServiceImpl", "findDocumenti()", "invocazione API DocumentiApiServiceImpl.findDocumenti", "");
            watcher.stop();
        }
    }

    private Vector<DocumentoDTO> findDocumentiPrivate(ParametriRicercaDocumentoDTO parametri) throws DAOException {

        DocumentoFilter filter = new DocumentoFilter();

        /*
         * Nel caso in cui mi viene popolato un elenco di id considero l'elenco
         * altrimenti controllo che il campo id documento sia valorizzato
         */
        if (SilCommonUtils.isNotVoid(parametri.getIdDocumento())) {
            filter.getIdDocumento().eq(parametri.getIdDocumento());
        } else if (SilCommonUtils.isNotVoid(parametri.getElencoId())) {
            filter.getIdDocumento().in(parametri.getElencoId());
        } else {
            // se viene richiesto uno stato specifico filtro su quello, altrimenti forzo la
            // ricerca di quelli non inviati, verso l'esterno solo quelli sono visibili
            if (SilCommonUtils.isNotVoid(parametri.getCodStatoDocumento())) {
                filter.getStatoDocumento().getCodStatoDocumento().eq(parametri.getCodStatoDocumento());
            } else {
                filter.getStatoDocumento().getCodStatoDocumento().ne(StatoDocumentoDTO.COD_STATO_DOCUMENTO_NON_INVIATO);
            }
        }
        if (SilCommonUtils.isNotVoid(parametri.getIdUtente())) {
            filter.getUtente().getIdUtente().eq(parametri.getIdUtente());
        }
        if (SilCommonUtils.isNotVoid(parametri.getIdLavoratore())) {
            filter.getUtente().getIdSilLavAngrafica().eq(parametri.getIdLavoratore());
        }
        if (SilCommonUtils.isNotVoid(parametri.getCodiceFiscale())) {
            filter.getUtente().getCfUtente().like(addPercentForLike(parametri.getCodiceFiscale()));
        }
        if (SilCommonUtils.isNotVoid(parametri.getCognome())) {
            filter.getUtente().getCognome().like(addPercentForLike(parametri.getCognome()));
        }
        if (SilCommonUtils.isNotVoid(parametri.getNome())) {
            filter.getUtente().getNome().like(addPercentForLike(parametri.getNome()));
        }

        ZoneId zoneId = ZoneId.systemDefault();
        if (SilCommonUtils.isNotVoid(parametri.getDa()) && SilCommonUtils.isNotVoid(parametri.getA())) {
            LocalDateTime ldtDa = LocalDateTime.ofInstant(parametri.getDa().toInstant(), zoneId).with(LocalTime.MIN);
            Date da = Date.from(ldtDa.atZone(zoneId).toInstant());

            LocalDateTime ldtA = LocalDateTime.ofInstant(parametri.getA().toInstant(), zoneId).with(LocalTime.MAX);
            Date a = Date.from(ldtA.atZone(zoneId).toInstant());

            filter.getDataInserimento().between(da, a);
        } else {
            if (SilCommonUtils.isNotVoid(parametri.getDa())) {
                LocalDateTime ldtDa = LocalDateTime.ofInstant(parametri.getDa().toInstant(), zoneId).with(LocalTime.MIN);
                Date da = Date.from(ldtDa.atZone(zoneId).toInstant());
                filter.getDataInserimento().ge(da);
            }
            if (SilCommonUtils.isNotVoid(parametri.getA())) {
                LocalDateTime ldtA = LocalDateTime.ofInstant(parametri.getA().toInstant(), zoneId).with(LocalTime.MAX);
                Date a = Date.from(ldtA.atZone(zoneId).toInstant());
                filter.getDataInserimento().le(a);
            }
        }

        QueryResult<DocumentoDTO> qrElencoDocumenti = dao.findAll(DocumentoDBDef.class, filter, 200);
        Vector<DocumentoDTO> elencoDocumenti = new Vector<DocumentoDTO>();
        qrElencoDocumenti.fillVector(elencoDocumenti);

        // Se viene ricercato un idDocumento specifico allora carico anche il clob e lo
        // pongo nel risultato trovato (che dovrebbe essere univoco
        if (parametri.getIdDocumento() != null && elencoDocumenti.size() == 1) {
            DocumentoDTO docConBlob = dao.findFirst(DocumentoBlobDBDef.class, new DocumentoBlobFilter(parametri.getIdDocumento()));
            if (docConBlob != null) {
                elencoDocumenti.firstElement().setDocumento(docConBlob.getDocumento());
            }
        }

        /*
         * Bisogna caricare per ogni documento il flgObbligatorio in base alla coppia
         * ambito-tipoDocumento
         */
        for (DocumentoDTO documentoDTO : elencoDocumenti) {
            if (this.getFlgObbligatorioDocumento(documentoDTO.getCodAmbito(), documentoDTO.getTipoDocumento().getCodTipoDocumento())) {
                documentoDTO.setFlgObbligatorio("S");
            } else {
                documentoDTO.setFlgObbligatorio("N");
            }
        }
        return elencoDocumenti;
    }

    private String addPercentForLike(String s) {
        return "%" + s + "%";
    }

    @Override
    @Transactional
    public Response notificaCambioStatoDocumento(ParametriNotificaCambioStatoDocumento parametri, @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        log.info("[DocumentiApiServiceImpl::notificaCambioStatoDocumento] parametri=" + parametri);
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        EsitoSalvataggioDocumentoDTO esito = new EsitoSalvataggioDocumentoDTO();
        try {

            DocumentoDTO docDaAggiornare = null;

            if (parametri == null || SilCommonUtils.isVoid(parametri.getIdDocumento()) || SilCommonUtils.isVoid(parametri.getIdAnagrafica())
                    || SilCommonUtils.isVoid(parametri.getCodiceFiscaleOperatore()) || SilCommonUtils.isVoid(parametri.getCodStatoDocumento())) {
                return buildResponseWithCodeAndMessage(404,
                        "parametri input mancanti. Obbligatori idDocumento,idAnagrafica,stato documento, codice fiscale operatore ");
            }

            ParametriRicercaDocumentoDTO paramRicerca = new ParametriRicercaDocumentoDTO();
            paramRicerca.setIdLavoratore(parametri.getIdAnagrafica());
            paramRicerca.setIdDocumento(parametri.getIdDocumento());
            Vector<DocumentoDTO> elencoDocumenti = findDocumentiPrivate(paramRicerca);

            if (SilCommonUtils.isVoid(elencoDocumenti)) {
                return buildResponseWithCodeAndMessage(404, "Documento non trovato");
            }

            else if (elencoDocumenti.size() > 1) {
                return buildResponseWithCodeAndMessage(500, "Troppi documenti trovati");
            } else {
                docDaAggiornare = elencoDocumenti.firstElement();
                log.info("[DocumentiApiServiceImpl::notificaCambioStatoDocumento] identificato documento=" + docDaAggiornare);
            }
            // Sicurezza per evitare di caricare una cosa sbagliata
            if (!docDaAggiornare.getIdDocumento().equals(parametri.getIdDocumento())) {
                return buildResponseWithCodeAndMessage(500, "Documento caricato non corrispondente a id=" + parametri.getIdDocumento());
            }

            // Se e' cambiato lo stato segno di mandare la mail
            boolean statoCambiato = !docDaAggiornare.getStatoDocumento().getCodStatoDocumento().equals(parametri.getCodStatoDocumento());

            docDaAggiornare.getStatoDocumento().setCodStatoDocumento(parametri.getCodStatoDocumento());
            docDaAggiornare.setNoteOperatore(parametri.getNoteOperatore());
            docDaAggiornare.setCfOperatore(parametri.getCodiceFiscaleOperatore());
            docDaAggiornare.setCodUserAggiorn(parametri.getCodiceFiscaleOperatore());
            docDaAggiornare.setGruppoOperatore(parametri.getGruppoOperatore());
            docDaAggiornare.setCodOperatore(parametri.getCodiceOperatore());
            docDaAggiornare.setSubcodice(parametri.getSubCodice());
            docDaAggiornare.setDataAggiorn(new Date());
            docDaAggiornare = dao.update(DocumentoDBDef.class, docDaAggiornare);
            // Ricarico per avere la descrizione dello stato modificato per la mail
            DocumentoFilter f = new DocumentoFilter();
            f.getIdDocumento().eq(docDaAggiornare.getIdDocumento());
            docDaAggiornare = dao.findFirst(DocumentoDBDef.class, f);

            if (statoCambiato) {
                invioEventualeMailPerCambioStatoDocumento(docDaAggiornare, esito);
            }

            log.debug("[DocumentiApiServiceImpl::notificaCambioStatoDocumento] documento=" + docDaAggiornare);
            esito.setDocumento(docDaAggiornare);
            return Response.ok(esito).build();
        } catch (Exception ex) {
            log.error("[DocumentiApiServiceImpl::notificaCambioStatoDocumento] parametri=" + parametri, ex);
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            return buildResponseWithCodeAndMessage(500, "Si e' verificato un errore generico di sistema ex=" + ex.getMessage());
        } finally {
            watcher.dumpElapsed("DocumentiApiServiceImpl", "notificaCambioStatoDocumento()",
                    "invocazione API DocumentiApiServiceImpl.notificaCambioStatoDocumento", "");
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
    private void invioEventualeMailPerCambioStatoDocumento(DocumentoDTO documento, EsitoDTO esito) throws Exception {
        String logMethodName = "[DocumentiApiServiceImpl::invioEventualeMailPerCambioStatoDocumento] ";
        log.debug(logMethodName + "Verifica invio mail per documento " + documento);
        MessaggioType mailType = null;
        if (documento != null) {
            switch (documento.getCodAmbito()) {
            case AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI:
                mailType = MessaggioType.DOCES;
                break;
            case AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA:
                mailType = MessaggioType.RCDOC;
                break;
            default:
                log.info("[DocumentiApiServiceImpl::invioEventualeMailPerCambioStatoDocumento] ambito non riconosciuto:" + documento.getCodAmbito()
                        + "; nessuna mail inviata");
            }
        }

        // lo stato dell'incontro richiede un invio mail, se il processo di aggiornameno
        // incontri pslp/silp ha avuto esito positivo
        if (mailType != null) {
            log.debug(logMethodName + "invio mail da effettuare di tipo " + mailType);
            if (esito.isEsitoPositivo()) {
                mailUtils.sendMail(documento, mailType, false);
            } else {
                log.debug(logMethodName + "invio mail non effettuato per esito negativo nel processo di aggiornamento documento pslp/silp");
            }
        } else {
            log.debug(logMethodName + "invio mail non necessario");
        }
    }

    private Response buildResponseWithCodeAndMessage(int code, String message) {
        EsitoSalvataggioDocumentoDTO esitoConErrore = new EsitoSalvataggioDocumentoDTO();
        log.error("[DocumentiApiServiceImpl::buildResponseWithCodeAndMessage] " + message);
        esitoConErrore.addErrorMessage(message);
        return Response.serverError().entity(esitoConErrore).status(code).build();
    }

    /*
     * Metodo per verificare se un documento e' obbligatorio o no in base al codice
     * ambito e codice tipo documento
     */
    private boolean getFlgObbligatorioDocumento(String codAmbito, String codTipoDocumento) throws DAOException {
        // restituisce se il documento e' obbligatorio o no
        AmbitoTipoDocumentoFilter filter = new AmbitoTipoDocumentoFilter();
        filter.getAmbito().getCodAmbito().eq(codAmbito);
        filter.getTipoDocumento().getCodTipoDocumento().eq(codTipoDocumento);
        AmbitoTipoDocumentoDTO ambitoTipoDocumentoDTO = dao.findFirst(AmbitoTipoDocumentoDBDef.class, filter);
        if (null != ambitoTipoDocumentoDTO.getFlgObbligatorio() && !ambitoTipoDocumentoDTO.getFlgObbligatorio().equals("N")) {
            return true;
        }
        return false;
    }

}
