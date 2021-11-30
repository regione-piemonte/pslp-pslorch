/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.portale.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.pslp.pslcommonobj.dbdef.ChiamataSMSDBDef;
import it.csi.pslp.pslcommonobj.dbdef.ContrattoSMSDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SMSDBDef;
import it.csi.pslp.pslcommonobj.dto.ChiamataSMSDTO;
import it.csi.pslp.pslcommonobj.dto.ContrattoSMSDDTO;
import it.csi.pslp.pslcommonobj.dto.SMSDTO;
import it.csi.pslp.pslcommonobj.dto.SistemaChiamanteDTO;
import it.csi.pslp.pslcommonobj.dto.StatoSMSDTO;
import it.csi.pslp.pslcommonobj.filter.ContrattoSMSFilter;
import it.csi.pslp.pslorch.business.integration.SMSClient;
import it.csi.pslp.pslorch.business.integration.dto.sms.StatoMessaggio;
import it.csi.pslp.pslorch.business.integration.dto.sms.consultazione.richiesta.CONSULTAZIONESMS;
import it.csi.pslp.pslorch.business.integration.dto.sms.consultazione.risposta.ESITOCONSULTAZIONESMS;
import it.csi.pslp.pslorch.business.integration.dto.sms.invio.richiesta.RICHIESTASMS;
import it.csi.pslp.pslorch.business.integration.dto.sms.invio.richiesta.SMS;
import it.csi.pslp.pslorch.business.integration.dto.sms.invio.risposta.ESITOACQUISIZIONESMS;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.GsonUtils;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.silcommon.dati.EsitoValidazioneCellulareMail;
import it.csi.silos.silcommon.util.ValidatoreNumeroCellulare;
import it.csi.util.performance.StopWatch;

@Component("smsUtils")
public class SMSApiServiceUtils {

    protected static final Logger logger = Logger.getLogger(Constants.COMPONENT_NAME);

    public static final String[] SMS_ESITO_OK = { "OK", "Operazione conclusa correttamente" };
    public static final String[] SMS_ESITO_E02 = { "E02", "Nessun fruitore trovato" };
    public static final String[] SMS_ESITO_E03 = { "E03",
            "Interruzione dell'esecuzione della richiesta a causa di un errore di sistema" };
    public static final String[] SMS_ESITO_E04 = { "E04", "Errore restituito da SILP" };

    @Autowired
    private DAO dao;

    private static final String INVIO_SMS_USER_TEST = "INVIO_SMS_USER_TEST";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChiamataSMSDTO traccia(String metodo, String caller, Object input, Long idSMS, Esito esito, String note) {
        try {
            Date now = new Date();
            ChiamataSMSDTO chiamata = new ChiamataSMSDTO();
            chiamata.setMetodoChiamato(metodo);
            SistemaChiamanteDTO sistemaChiamante = new SistemaChiamanteDTO();
            sistemaChiamante.setCodice(caller);
            chiamata.setSistemaChiamante(sistemaChiamante);
            chiamata.setMsgInput(GsonUtils.toGsonString(input));
            SMSDTO sms = new SMSDTO();
            sms.setIdSMS(idSMS);
            chiamata.setSms(sms);
            chiamata.setCodiceEsito(esito.getCodiceEsito());
            chiamata.setDescrizioneEsito(esito.getDescrizioneEsito());
            if (note != null && note.length() > 4000) {
                note = note.substring(0, 3999);
            }
            chiamata.setNote(note);
            chiamata.setOraInizio(now);
            chiamata.setOraFine(now);
            chiamata = dao.insert(ChiamataSMSDBDef.class, chiamata);
            return chiamata;
        } catch (Exception ex) {
            logger.error("[SMSApiServiceImpl::traccia] metodo=" + metodo, ex);
        }
        return null;
    }
    
    /**
     * Verifica la correttezza del numero di cellulare da inviare.
     * Se il numero e' correggibile lo modifica in smsDTO, in caso contrario lo marca come errato.
     * @param smsDTO
     * @return
     */
    private boolean checkNumero(SMSDTO smsDTO) throws Exception {
      EsitoValidazioneCellulareMail esitoValidazione = new EsitoValidazioneCellulareMail();
      String cellulareCorretto = ValidatoreNumeroCellulare.pulisciAndValidaCellulare(smsDTO.getCellulare(), esitoValidazione, "");
      if(esitoValidazione.isElencoNonValidiVoid()) {
        if(!cellulareCorretto.equals(smsDTO.getCellulare())) {
          logger.debug("[SMSApiServiceUtils::checkNumero] Corretto cellulare da "+smsDTO.getCellulare()+" a "+cellulareCorretto); 
          smsDTO.setCellulare(cellulareCorretto);
        }
        return true;
      }
      else {
        smsDTO.setCodiceErrore("E");
        smsDTO.setStato(new StatoSMSDTO(StatoSMSDTO.ERRORE));
        smsDTO.setDescrizioneErrore("Controllo pre-invio: numero cellulare errato: "+smsDTO.getCellulare());
        smsDTO.setCodicePrenotazione("-");
        dao.update(SMSDBDef.class, smsDTO);
        return false;
      }
    }

    /**
     * Invia un insieme di SMS al gateway. Il cellulare di invio puo' essere
     * overridato dalla property su db INVIO_SMS_USER_TEST; se il cellulare di un
     * SMS (originale o sostituito) vale '-' quel messaggio non viene inviato.
     * 
     * @param bloccoInvii insieme degli SMS da inviare
     */
    public String sendSMS(List<SMSDTO> bloccoInvii) throws Exception {
        logger.info("[SMSApiServiceUtils::sendSMS] Dimensione blocco: "+bloccoInvii.size());
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Date now = new Date();
        String cellulareTest = ParametroUtils.getInstance().getParametro(INVIO_SMS_USER_TEST);
        try {
            RICHIESTASMS richiestaSMS = new RICHIESTASMS();
            richiestaSMS.setREPLYDETAIL("all");
            // richiestaSMS.setREPLYDETAIL("errors");
            String codiceContratto = null;
            Map<Long, ChiamataSMSDTO> mapTracciamenti = new HashMap<>();
            Map<Long, SMSDTO> mapSMSDTO = new HashMap<>();
            for (SMSDTO smsDTO : bloccoInvii) {
              
                if(!checkNumero(smsDTO)) {
                  continue;
                }
              
                mapSMSDTO.put(smsDTO.getIdSMS(), smsDTO);
                SMS sms = new SMS();
                String cellulare = cellulareTest;
                if (cellulare == null) {
                    cellulare = smsDTO.getCellulare();
                }
                if ("-".equals(cellulare)) {
                  smsDTO.setDataUltimoAggiornamento(now);
                  smsDTO.setCodicePrenotazione("Invio Fittizio TEST");
                  smsDTO.setStato(new StatoSMSDTO(StatoSMSDTO.INVIATO));
                  smsDTO.setDataStato(now);
                  smsDTO.setCodiceErrore(null);
                  dao.update(SMSDBDef.class, smsDTO);
                  continue;
                }
                sms.setTELEFONO(cellulare);
                sms.setTESTO(smsDTO.getMessaggio());
                sms.setNOTE(smsDTO.getIdSMS().toString());
                richiestaSMS.getSMS().add(sms);
                codiceContratto = smsDTO.getCodiceContratto();

                Esito esito = new Esito();
                esito.setEsito(SMS_ESITO_E03);
                RICHIESTASMS richiestaSMSTracciamento = new RICHIESTASMS();
                richiestaSMSTracciamento.setREPLYDETAIL("all");
                richiestaSMSTracciamento.getSMS().add(sms);
                ChiamataSMSDTO tracciamento = traccia("sendGatewaySMS", "PSLP", richiestaSMSTracciamento, smsDTO.getIdSMS(), esito,
                        null);
                if (tracciamento != null)
                    mapTracciamenti.put(smsDTO.getIdSMS(), tracciamento);
                smsDTO.setStato(new StatoSMSDTO(SMSDTO.STATO_IN_ATTESA_DI_RISPOSTA));
                smsDTO.setCodiceErrore(null);
                smsDTO.setDataStato(now);
                dao.update(SMSDBDef.class, smsDTO);
            }

            if (codiceContratto != null) {
                ContrattoSMSFilter filter = new ContrattoSMSFilter();
                filter.getCodice().eq(codiceContratto);
                ContrattoSMSDDTO contratto = dao.findFirst(ContrattoSMSDBDef.class, filter);
                richiestaSMS.setCODICEPROGETTO(contratto.getCodiceProgetto());
                richiestaSMS.setUSERNAME(contratto.getUsername());
                richiestaSMS.setPASSWORD(SMSClient.decrypt(contratto.getPassword()));
                SMSClient client = new SMSClient();
                String resultXML = client.invokeApi("/client/pSmsRequest.cgi", toXML(richiestaSMS));
                logger.info("[SMSApiServiceUtils::sendSMS] resultXML=" + resultXML);
                if (resultXML != null) {
                    ESITOACQUISIZIONESMS esitoGateway = (ESITOACQUISIZIONESMS) fromXML(resultXML,
                            ESITOACQUISIZIONESMS.class);
                    for (it.csi.pslp.pslorch.business.integration.dto.sms.invio.risposta.SMS sms : esitoGateway
                            .getSMS()) {
                        SMSDTO smsDTO = bloccoInvii.get(Integer.parseInt(sms.getNUMERO()) - 1);
                        smsDTO.setDataUltimoAggiornamento(now);
                        smsDTO.setCodicePrenotazione(sms.getCODICE());
                        smsDTO.setStato(new StatoSMSDTO(StatoSMSDTO.INVIATO));
                        smsDTO.setDataStato(now);
                        if ("VALIDO".equals(sms.getMESSAGGIO())) {
                            smsDTO.setCodiceErrore(null);
                        } else {
                            smsDTO.setCodiceErrore("E");
                            smsDTO.setStato(new StatoSMSDTO(StatoSMSDTO.ERRORE));
                            smsDTO.setDescrizioneErrore(sms.getMESSAGGIO());
                        }
                        dao.update(SMSDBDef.class, smsDTO);

                        ChiamataSMSDTO tracciamento = mapTracciamenti.get(smsDTO.getIdSMS());
                        tracciamento.setCodiceEsito(smsDTO.getCodiceErrore());
                        tracciamento.setDescrizioneEsito(smsDTO.getDescrizioneErrore());
                        tracciamento.setOraFine(now);
                        dao.update(ChiamataSMSDBDef.class, tracciamento);
                    }
                }
            }
            return "";
        } finally {
            watcher.dumpElapsed("SMSApiServiceUtils", "sendSMS()", "invocazione API SMSApiServiceUtils.sendSMS", "");
            watcher.stop();
        }
    }

    public Map<Long, StatoMessaggio> consultaSMS(List<SMSDTO> elencoSMS) throws Exception {
        logger.info("[SMSApiServiceUtils::consultaSMS]");
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        Map<Long, StatoMessaggio> result = new HashMap<>();
        try {
            for (SMSDTO smsDTO : elencoSMS) {
                result.put(smsDTO.getIdSMS(), new StatoMessaggio(smsDTO, null, null));
                CONSULTAZIONESMS richiestaSMS = new CONSULTAZIONESMS();
                richiestaSMS.setNOTE(smsDTO.getIdSMS().toString());
                String cellulare = ParametroUtils.getInstance().getParametro(INVIO_SMS_USER_TEST);
                if (cellulare == null)
                    cellulare = smsDTO.getCellulare();
                richiestaSMS.setTELEFONO(cellulare);
                String codiceContratto = smsDTO.getCodiceContratto();
                ContrattoSMSFilter filter = new ContrattoSMSFilter();
                filter.getCodice().eq(codiceContratto);
                ContrattoSMSDDTO contratto = dao.findFirst(ContrattoSMSDBDef.class, filter);
                richiestaSMS.setCODICEPROGETTO(contratto.getCodiceProgetto());
                richiestaSMS.setUSERNAME(contratto.getUsername());
                richiestaSMS.setPASSWORD(SMSClient.decrypt(contratto.getPassword()));
                SMSClient client = new SMSClient();
                String resultXML = client.invokeApi("/consult/consulta.cgi", toXML(richiestaSMS));
                logger.info("[SMSApiServiceUtils::consultaSMS] resultXML=" + resultXML);
                if (resultXML != null) {
                    if (!resultXML.startsWith("ESITO_CONSULTAZIONE_SMS")) {
                        continue;
                    }
                    ESITOCONSULTAZIONESMS esitoGateway = (ESITOCONSULTAZIONESMS) fromXML(resultXML,
                            ESITOCONSULTAZIONESMS.class);
                    for (it.csi.pslp.pslorch.business.integration.dto.sms.consultazione.risposta.SMS sms : esitoGateway
                            .getSMS()) {
                        logger.info("[SMSApiServiceUtils::consultaSMS] " + sms);
                        smsDTO.setCodicePrenotazione(sms.getCODMAM());
                        StatoMessaggio statoMessaggio = new StatoMessaggio(smsDTO, sms.getCODIFICA(),
                                sms.getSTATOMAM());
                        result.put(statoMessaggio.getSms().getIdSMS(), statoMessaggio);
                    }
                }
            }
            return result;
        } finally {
            watcher.dumpElapsed("SMSApiServiceUtils", "consultaSMS()", "invocazione API SMSApiServiceUtils.consultaSMS",
                    "");
            watcher.stop();
        }
    }

    private String toXML(Object o) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(o.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(o, sw);
        String xmlContent = sw.toString();
        return xmlContent;
    }

    private Object fromXML(String s, Class c) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(c);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return jaxbUnmarshaller.unmarshal(new StringReader(s));
    }

    public static final class Esito {
        private String[] esito;

        private String msg = "";

        public Esito() {
        }

        public String getCodiceEsito() {
            if (esito == null)
                return null;
            return esito[0];
        }

        public String getDescrizioneEsito() {
            if (esito == null)
                return null;
            return esito[1] + msg;
        }

        public void setEsito(String[] esito) {
            this.esito = esito;
        }

        public void setEsito(String[] esito, String msg) {
            this.esito = esito;
            this.msg = msg;
        }
    }

}
