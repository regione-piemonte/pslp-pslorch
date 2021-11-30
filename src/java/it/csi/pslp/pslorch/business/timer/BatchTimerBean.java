/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.business.timer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import it.csi.pslp.pslcommonobj.dbdef.MessaggioUtenteDBDef;
import it.csi.pslp.pslcommonobj.dbdef.PrenotazioneDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SMSDBDef;
import it.csi.pslp.pslcommonobj.dbdef.SistemaChiamanteDBDef;
import it.csi.pslp.pslcommonobj.dto.AmbitoDTO;
import it.csi.pslp.pslcommonobj.dto.MessaggioUtenteDTO;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.SMSDTO;
import it.csi.pslp.pslcommonobj.dto.SistemaChiamanteDTO;
import it.csi.pslp.pslcommonobj.dto.StatoSMSDTO;
import it.csi.pslp.pslcommonobj.filter.MessaggioUtenteFilter;
import it.csi.pslp.pslcommonobj.filter.PrenotazioneFilter;
import it.csi.pslp.pslcommonobj.filter.SMSFilter;
import it.csi.pslp.pslcommonobj.filter.SistemaChiamanteFilter;
import it.csi.pslp.pslorch.business.common.PslorchRuntimeConfig;
import it.csi.pslp.pslorch.business.common.TracciamentoUtils;
import it.csi.pslp.pslorch.business.integration.AdapterSilpsvinWSImpl;
import it.csi.pslp.pslorch.business.integration.MailUtils;
import it.csi.pslp.pslorch.business.integration.dto.sms.StatoMessaggio;
import it.csi.pslp.pslorch.business.portale.impl.SMSApiServiceUtils;
import it.csi.pslp.pslorch.business.portale.impl.SMSApiServiceUtils.Esito;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.jedi.core.StdRowReader;
import it.csi.silos.jedi.engine.DBUtils;

@Component("batchTimerBean")
public class BatchTimerBean {

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);
  
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private DAO dao;
  
  @Autowired
  private MailUtils mailUtils;
  
  @Autowired
  private TracciamentoUtils tracciamentoUtils;

  @Autowired
  private SMSApiServiceUtils smsUtils;

  private static final String INVIO_MAIL_SEMAPHORE = "INVIO_MAIL_SEMAPHORE";
  private static final String INVIO_MAIL_RANGE = "INVIO_MAIL_RANGE";
  private static final String INVIO_SMS_SEMAPHORE = "INVIO_SMS_SEMAPHORE";
  private static final String INVIO_SMS_RANGE = "INVIO_SMS_RANGE";
  private static final String INVIO_SMS_MAX_BATCH = "INVIO_SMS_MAX_BATCH";
  private static final String INVIO_SMS_MAX_GTEWAY = "INVIO_SMS_MAX_GTEWAY";


  /*
   * Metodo schedulato mediante Timer Bean per l'invio delle mail.
   * Il Timer Bean si attiva ogni ora e esegue l'elaborazione se
   *   - non c'e' gia' un'altra elaborazione in corso
   *   - l'ora corrente e' inclusa in 'INVIO_MAIL_RANGE' (nel formato 'ora1,ora2,...')
   */
  @Scheduled(cron = "0 0 */1 * * *")
  public void invioMailBatch() {
    invioMail(true);
  }
  
  public String invioMail(boolean batchMode) {
    StringBuilder esito = new StringBuilder();
    long t1 = System.currentTimeMillis();
    boolean err = false;
    try {
      if (!applicationContext.getBean(BatchTimerBean.class).startSemaphore(INVIO_MAIL_SEMAPHORE)) {
        log.info("[BatchTimerBean::invioMail] invioMail non attivo o gia' in corso");
        return "InvioMail gia' in corso";
      }
      
      // Verifica se l'esecuzione del batch e' attiva
      if(batchMode) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String range = ParametroUtils.getInstance().getParametro(INVIO_MAIL_RANGE, "22");
        if (!checkRange(range,hour)) {
          log.debug("[BatchTimerBean::invioMail] Elaborazione non attiva, range:" + range + " hour:" +hour);
          return "Elaborazione non attiva, range:" + range + " hour:" +hour;
        }
      }
      
      String from = ParametroUtils.getInstance().getParametro(ParametroUtils.MITTENTE);
      String fromAlias = ParametroUtils.getInstance().getParametro(ParametroUtils.MITTENTE_ALIAS);

      // Invia le mail di promemoria
      log.info("[BatchTimerBean::invioMail] Invio mail promemoria...");
      Map<String,Object> filter = new HashMap<>();
      String margineOre = ParametroUtils.getInstance().getParametro(ParametroUtils.PROMEM_MARGINE_ORE,"12");
      filter.put("margineOre",Integer.parseInt(margineOre));
      final Set<Long> mailPromemoriaInviate = new HashSet<>();
      final Set<Long> mailPromemoriaErrate = new HashSet<>();
      esito.append("Invio mail di promemoria:\n");
      dao.eachRow("portale/impl/CalendarioApiServiceImpl.findPrenotazioniPerMail", filter, new StdRowReader() {
        public void stdRowReaded(ResultSet rs) throws DAOException {
          Long idPrenotazione = DBUtils.getLong(rs, "ID_PRENOTAZIONE");
          try {
            PrenotazioneFilter prenotazioneFilter = new PrenotazioneFilter();
            prenotazioneFilter.getIdPrenotazione().eq(idPrenotazione);
            PrenotazioneDTO prenotazione = dao.findFirst(PrenotazioneDBDef.class, prenotazioneFilter);
            MessaggioType t = null;
            String codAmbito = prenotazione.getSlot().getGiorno().getPeriodo().getCalendario().getAmbito().getCodAmbito();
            switch(codAmbito) {
              case AmbitoDTO.COD_AMBITO_GG_GARANZIA_GIOVANI: t = MessaggioType.APPRO; break;
              case AmbitoDTO.COD_AMBITO_RDC_REDDITO_DI_CITTADINANZA: t = MessaggioType.RCPRO; break;
              default: log.info("[BatchTimerBean::invioMail] ambito non riconosciuto:"+codAmbito+"; nessuna mail inviata");
            }
            if(t!=null) {
              mailUtils.sendMail(prenotazione, t, true);
              esito.append("  Inviata mail di promemoria per l'ID prenotazione "+idPrenotazione+"\n");
              prenotazione.setFlgInviatoPromemoria("S");
              dao.update(PrenotazioneDBDef.class, prenotazione);
              mailPromemoriaInviate.add(idPrenotazione);
            }
          }
          catch(Throwable ex) {
            log.error("[BatchTimerBean::invioMail]", ex);
            mailPromemoriaErrate.add(idPrenotazione);
            esito.append("  Errore nell'invio mail di promemoria per l'ID prenotazione "+idPrenotazione+": "+ex.getClass().getName()+"\n");
          }
        }
      });
      esito.append("Mail di promemoria inviate: "+mailPromemoriaInviate.size()+"\n");
      esito.append("Mail di promemoria errate: "+mailPromemoriaErrate.size()+"\n");
      
      // Reinvia le mail non inviate correttamente
      log.info("[BatchTimerBean::invioMail] Reinvio mail:");
      Date now = new Date();
      long giorniInvioMailKO = Long.parseLong(ParametroUtils.getInstance().getParametro(ParametroUtils.GG_INVIO_MAIL_KO,"3"));
      MessaggioUtenteFilter filterMessaggi = new MessaggioUtenteFilter();
      filterMessaggi.getDInvio().eq(null);
      filterMessaggi.getEvento().getDEvento().ge(new Date(now.getTime()-giorniInvioMailKO*24*3600*1000));
      filterMessaggi.getIdMessaggio().setOrderAsc(1);
      int mailRecuperoInviate = 0;
      int mailRecuperoErrate = 0;
      esito.append("Invio mail di recupero...\n");
      for(MessaggioUtenteDTO messaggio: dao.findAll(MessaggioUtenteDBDef.class, filterMessaggi, 1000)) {
        try {
          log.debug("[BatchTimerBean::invioMail] Recupero mail con idMessaggio "+messaggio.getIdMessaggio());
          String[] to = messaggio.getEmailDestinatario().split(",");
          mailUtils.sendMail(from, fromAlias, to, null, messaggio.getOggetto(), messaggio.getTesto(), messaggio);
          mailRecuperoInviate++;
          esito.append("  Inviata mail di recupero per l'ID messaggio utente "+messaggio.getIdMessaggioUtente()+", ID evento "+messaggio.getEvento().getIdEvento()+"\n");
        }
        catch(Throwable ex) {
          log.error("[BatchTimerBean::invioMail]", ex);
          mailRecuperoErrate++;
          esito.append("  Errore nell'invio mail di recupero per l'ID messaggio utente "+messaggio.getIdMessaggioUtente()+", ID evento "+messaggio.getEvento().getIdEvento()+"\n");
        }
      }
      esito.append("Mail di recupero inviate: "+mailRecuperoInviate+"\n");
      esito.append("Mail di recupero errate: "+mailRecuperoErrate+"\n");
    }
    catch (Exception ex) {
      esito.append("Errore nell'esecuzione del batch: "+ex.getClass().getName()+"\n");
      err = true;
      log.error("[BatchTimerBean::invioMail]", ex);
    }
    finally {
      applicationContext.getBean(BatchTimerBean.class).stopSemaphore(INVIO_MAIL_SEMAPHORE);
    }

    log.info("[BatchTimerBean::invioMail] completato ");
    long t2 = System.currentTimeMillis();
    esito.append("Esecuzione completata in "+(t2-t1)+" ms.\n");
    if(err)
      tracciamentoUtils.tracciaKo(TracciamentoUtils.BATCH_MAIL, null, "Batch", esito.toString(), null);
    else
      tracciamentoUtils.tracciaOk(TracciamentoUtils.BATCH_MAIL, null, "Batch", esito.toString(), null);
    
    return esito.toString();
  }
  
  /*
   * Metodo schedulato mediante Timer Bean per l'invio delle mail.
   * Il Timer Bean si attiva ogni ora e esegue l'elaborazione se
   *   - non c'e' gia' un'altra elaborazione in corso
   *   - l'ora corrente e' inclusa in 'INVIO_SMS_RANGE' (nel formato 'ora1,ora2,...')
   */
  @Scheduled(cron = "0 0 */1 * * *")
  public void invioSMSBatch() {
    invioSMS(true);
  }
  
  public String invioSMS(boolean batchMode) {
    log.info("[BatchTimerBean::invioSMS] batchMode="+batchMode);
    StringBuilder note = new StringBuilder();
    Esito esito = new Esito();
    esito.setEsito(SMSApiServiceUtils.SMS_ESITO_E02);
    long t1 = System.currentTimeMillis();
    int numSms = 0;
    try {
      //verifica che non sia attivo il batch di invio SMS 	
      if (!applicationContext.getBean(BatchTimerBean.class).startSemaphore(INVIO_SMS_SEMAPHORE)) {
        log.info("[BatchTimerBean::invioSMS] invioSMS non attivo o gia' in corso");
        return "invioSMS gia' in corso";
      }
      
      // Verifica se l'esecuzione del batch e' attiva
      if(batchMode) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String range = ParametroUtils.getInstance().getParametro(INVIO_SMS_RANGE, "22");
        if (!checkRange(range,hour)) {
          log.debug("[BatchTimerBean::invioSMS] Elaborazione non attiva, range:" + range + " hour:" +hour);
          return "Elaborazione non attiva, range:" + range + " hour:" +hour;
        }
      }
      
      int numMaxEstratti = Integer.parseInt(ParametroUtils.getInstance().getParametro(INVIO_SMS_MAX_BATCH, "100000"));
      int numMaxGateway = Integer.parseInt(ParametroUtils.getInstance().getParametro(INVIO_SMS_MAX_GTEWAY, "100"));
      Date now = new Date();
      SistemaChiamanteFilter filterSistemi = new SistemaChiamanteFilter();
      filterSistemi.getCodice().ne("PSLP");
      filterSistemi.getDataInizio().lt(now);
      for(SistemaChiamanteDTO sistema: dao.findAll(SistemaChiamanteDBDef.class, filterSistemi, 0)) {
        if(sistema.getDataFine()!=null && sistema.getDataFine().before(now)) continue;
        
        // Inserisce il blocco delle modifiche agli SMS per quel sistema chiamante
        applicationContext.getBean(BatchTimerBean.class).startSemaphoreSistemaChiamante(sistema.getCodice());
        
        try {
        
          esito.setEsito(SMSApiServiceUtils.SMS_ESITO_OK);
          String s = "Invio SMS per il Sistema "+sistema.getCodice();
          log.debug("[BatchTimerBean::invioSMS] "+s);
          note.append(s+":\n");
          SMSFilter filterSMS = new SMSFilter();
          filterSMS.getCodicePrenotazione().eq(null);
          filterSMS.getDataCancellazione().eq(null);
          filterSMS.getStato().getCodice().in(new String[]{StatoSMSDTO.SALVATO, StatoSMSDTO.IN_ATTESA_DI_RISPOSTA_DAL_GATEWAY});
          filterSMS.getSistemaChiamante().getCodice().eq(sistema.getCodice());
          Map<String,List<SMSDTO>> smsDaInviare = new HashMap<>();
          List<SMSDTO> smsDaVerificare = new ArrayList<>();
          if(PslorchRuntimeConfig.isLocalExecution()) {
        	  //filterSMS.getIdSMS().eq(93855L);
        	  filterSMS.getIdSMS().eq(73613L);
          }
          
          for(SMSDTO sms: dao.findAll(SMSDBDef.class, filterSMS, numMaxEstratti)) {
            log.debug("[BatchTimerBean::invioSMS] Verifico SMS "+sms.getIdSMS()+" con stato "+sms.getStato().getCodice());
            if(StatoSMSDTO.SALVATO.equals(sms.getStato().getCodice())) {
              addSMSDaInviare(smsDaInviare, sms);
            }
            else if(StatoSMSDTO.IN_ATTESA_DI_RISPOSTA_DAL_GATEWAY.equals(sms.getStato().getCodice())) {
              smsDaVerificare.add(sms);
            }
          }
          // scorre gli SMS da verificare
          Map<Long, StatoMessaggio> statiSMS = smsUtils.consultaSMS(smsDaVerificare);
          for(Map.Entry<Long, StatoMessaggio> statoSMS: statiSMS.entrySet()) {
            if(statoSMS.getValue().getStatoGateway()==null) { // l'SMS non risulta sul gateway
              addSMSDaInviare(smsDaInviare, statoSMS.getValue().getSms());
            }
            else {
              SMSDTO smsDTO = statoSMS.getValue().getSms();
              smsDTO.setCodiceErrore(null);
              smsDTO.setStato(new StatoSMSDTO(SMSDTO.STATO_INVIATO));
              dao.update(SMSDBDef.class, smsDTO);
            }
          }
          
          if(smsDaInviare.size()==0) {
            note.append("  Nessun SMS da inviare\n");
          }
          List<SMSDTO> bloccoInvii = new ArrayList<>(numMaxGateway);
          for(List<SMSDTO> listSMS: smsDaInviare.values()) {
            for(SMSDTO sms: listSMS) {
              bloccoInvii.add(sms);
              numSms++;
              if(bloccoInvii.size()>=numMaxGateway) {
                inviaBloccoSMS(bloccoInvii);
                bloccoInvii.clear();
              }
            }
            inviaBloccoSMS(bloccoInvii);
          }
          note.append("  SMS prenotati sul gateway: "+numSms+"\n");
        }
        finally {
          // Rimuove il blocco delle modifiche agli SMS per quel sistema chiamante
          applicationContext.getBean(BatchTimerBean.class).stopSemaphoreSistemaChiamante(sistema.getCodice());
        }

      }
      
      // Aggiorna lo stato SMS su SILP
      SMSFilter filterSMS = new SMSFilter();
      filterSMS.getStato().getCodice().in(new String[] {StatoSMSDTO.INVIATO, StatoSMSDTO.ERRORE});
      filterSMS.getDataAggiornamentoSistemaChiamante().eq(null);
      for(SMSDTO sms: dao.findAll(SMSDBDef.class, filterSMS, numMaxEstratti)) {
        Map<String,Object> input = new HashMap<>();
        input.put("idSMS",sms.getIdSMS());
        input.put("stato",sms.getStato().getCodice());
        input.put("dataStato",sms.getDataStato());
        input.put("codiceErrore",sms.getCodiceErrore());
        Esito esitoSilp = new Esito();
        try {
          String esitoNotificaSilp = AdapterSilpsvinWSImpl.getInstance().saveStatoSms(sms.getIdSMS(), sms.getStato().getCodice(), sms.getDataStato(), sms.getCodiceErrore());
          if(esitoNotificaSilp==null) {
            sms.setDataAggiornamentoSistemaChiamante(new Date());
            dao.update(SMSDBDef.class, sms);
            esitoSilp.setEsito(SMSApiServiceUtils.SMS_ESITO_OK);
            smsUtils.traccia("sendSilpSMS", "PSLP", input, sms.getIdSMS(), esitoSilp, "Esito aggiornato su SILP");      
          }
          else {
            esitoSilp.setEsito(SMSApiServiceUtils.SMS_ESITO_E04);
            smsUtils.traccia("sendSilpSMS", "PSLP", input, sms.getIdSMS(), esitoSilp, esitoNotificaSilp);      
          }
        }
        catch(Exception ex) {
          log.error("[BatchTimerBean::invioSMS]", ex);
          esitoSilp.setEsito(SMSApiServiceUtils.SMS_ESITO_E03);
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          ex.printStackTrace(pw);
          smsUtils.traccia("sendSilpSMS", "PSLP", input, sms.getIdSMS(), esitoSilp, pw.toString());      
        }
      }
    }
    catch (Exception ex) {
      note.append("Errore nell'esecuzione del batch: "+ex.getClass().getName()+"\n");
      log.error("[BatchTimerBean::invioSMS]", ex);
    }
    finally {
      applicationContext.getBean(BatchTimerBean.class).stopSemaphore(INVIO_SMS_SEMAPHORE);
    }

    log.info("[BatchTimerBean::invioSMS] completato ");
    long t2 = System.currentTimeMillis();
    note.append("Esecuzione completata in "+(t2-t1)+" ms.\n");
    smsUtils.traccia("batchInvioSMS", "PSLP", null, null, esito, note.toString());
    //if(err)
    //  tracciamentoUtils.tracciaKo(TracciamentoUtils.BATCH_SMS, null, "Batch", esito.toString());
    //else
    //  tracciamentoUtils.tracciaOk(TracciamentoUtils.BATCH_SMS, null, "Batch", esito.toString());
    
    return note.toString();
  }
  
  private void addSMSDaInviare(Map<String,List<SMSDTO>> smsDaInviare, SMSDTO sms) {
    List<SMSDTO> listSMS = smsDaInviare.get(sms.getCodiceContratto());
    if(listSMS==null) {
      listSMS = new ArrayList<>();
      smsDaInviare.put(sms.getCodiceContratto(), listSMS);
    }
    listSMS.add(sms);
  }
  
  private String inviaBloccoSMS(List<SMSDTO> bloccoInvii) throws Exception {
    Date now = new Date();
    StringBuilder note = new StringBuilder();
    smsUtils.sendSMS(bloccoInvii);
    return note.toString();
  }

  /**
   * Verifica se il range (nel formato "ora1,ora2,..." ) include l'ora corrente.
   */
  private boolean checkRange(String range, int hourNow) {
    if(range==null) return false;
    for(String hour: range.split(",")) {
      if(hourNow==Integer.parseInt(hour)) return true;
    }
    return false;
  }
  
  //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean startSemaphore(String semaphoreName) throws Exception {
    Map<String,Object> params = new HashMap<>(1);
    params.put("codParametro", semaphoreName);
    try {
      dao.execute("SELECT * FROM PSLP_D_PARAMETRO WHERE COD_PARAMETRO=:codParametro FOR UPDATE NOWAIT", params);
    }
    catch(Throwable ex) {
      log.info("[BatchTimerBean::startSemaphore] locked");
      TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
      return false;
    }
    int nrows = dao.execute("UPDATE PSLP_D_PARAMETRO SET valore_parametro='N' where COD_PARAMETRO=:codParametro AND VALORE_PARAMETRO='S'", params);
    log.info("[BatchTimerBean::startSemaphore] nrows="+nrows);
    return nrows>0;
  }

  //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void stopSemaphore(String semaphoreName) {
    try {
      Map<String,Object> params = new HashMap<>(1);
      params.put("codParametro", semaphoreName);
      try {
        dao.execute("SELECT * FROM PSLP_D_PARAMETRO WHERE COD_PARAMETRO=:codParametro FOR UPDATE NOWAIT", params);
      }
      catch(Throwable ex) {
        log.info("[BatchTimerBean::stopSemaphore] locked");
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        return;
      }
      dao.execute("UPDATE PSLP_D_PARAMETRO SET VALORE_PARAMETRO='S' WHERE COD_PARAMETRO=:codParametro",params);
    }
    catch (Exception ex) {
      log.error("[BatchTimerBean::stopSemaphore]", ex);
      TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean startSemaphoreSistemaChiamante(String codiceSistemaChiamante) throws Exception {
    Map<String,Object> params = new HashMap<>(1);
    params.put("codParametro", "SMS_"+codiceSistemaChiamante);
    try {
      dao.execute("SELECT * FROM PSLP_D_PARAMETRO WHERE COD_PARAMETRO=:codParametro FOR UPDATE NOWAIT", params);
    }
    catch(Throwable ex) {
      log.info("[BatchTimerBean::startSemaphoreSistemaChiamante] locked");
      TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
      return false;
    }
    int nrows = dao.execute("UPDATE PSLP_D_PARAMETRO SET valore_parametro='S' where COD_PARAMETRO=:codParametro AND VALORE_PARAMETRO='N'", params);
    log.info("[BatchTimerBean::startSemaphoreSistemaChiamante] nrows="+nrows);
    return nrows>0;
  }

  //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void stopSemaphoreSistemaChiamante(String codiceSistemaChiamante) {
    try {
      Map<String,Object> params = new HashMap<>(1);
      params.put("codParametro", "SMS_"+codiceSistemaChiamante);
      try {
        dao.execute("SELECT * FROM PSLP_D_PARAMETRO WHERE COD_PARAMETRO=:codParametro FOR UPDATE NOWAIT", params);
      }
      catch(Throwable ex) {
        log.info("[BatchTimerBean::stopSemaphoreSistemaChiamante] locked");
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        return;
      }
      dao.execute("UPDATE PSLP_D_PARAMETRO SET VALORE_PARAMETRO='N' WHERE COD_PARAMETRO=:codParametro",params);
    }
    catch (Exception ex) {
      log.error("[BatchTimerBean::stopSemaphoreSistemaChiamante]", ex);
      TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
    }
  }
}
