/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.log4j.Logger;

import it.csi.pslp.pslcommonobj.dto.CalendarioDTO;
import it.csi.pslp.pslcommonobj.dto.EsitoDTO;
import it.csi.pslp.pslcommonobj.dto.GiornoDTO;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslcommonobj.dto.SlotDTO;
import it.csi.pslp.pslorch.business.portale.impl.helper.DecodificheHelper;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.DateUtils;
import it.csi.pslp.pslorch.util.GsonUtils;
import it.csi.silpcommonobj.dati.ente.EnteSilpHeaderDTO;
import it.csi.silpcommonobj.dati.ente.EsitoFindElencoEntiSilpDTO;
import it.csi.silpcommonobj.dati.incontri.EsitoAppuntamentoSilpDTO;
import it.csi.silpcommonobj.dati.serviziopslp.EsitoFindElencoServizioPSLPDTO;
import it.csi.silpcommonobj.dati.serviziopslp.ServizioPSLPDTO;
import it.csi.silpcommonobj.dati.sms.EsitoSetStatoSmsDTO;
import it.csi.silpsv.silpsvin.cxfclient.AppuntamentoInputParam;
import it.csi.silpsv.silpsvin.cxfclient.ElencoServizioInputParam;
import it.csi.silpsv.silpsvin.cxfclient.FindElencoEntiInputParam;
import it.csi.silpsv.silpsvin.cxfclient.InputSetStatoSmsSilpDTO;
import it.csi.silpsv.silpsvin.cxfclient.ServiziSilpException_Exception;
import it.csi.silpsv.silpsvin.cxfclient.WSContainerDTO;
import it.csi.util.performance.StopWatch;

public class AdapterSilpsvinWSImpl {

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

  public AdapterSilpsvinWSFactory factory = new AdapterSilpsvinWSFactory();

  private static AdapterSilpsvinWSImpl instance = null;

  public static AdapterSilpsvinWSImpl getInstance() {
    if (instance == null) {
      instance = new AdapterSilpsvinWSImpl();
    }
    return instance;
  }

  /**
   * Salva un appuntamento su silpsvin
 * @param  
   */
  public EsitoAppuntamentoSilpDTO saveAppuntamento(PrenotazioneDTO prenotazione) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start();
    EsitoAppuntamentoSilpDTO esitoDTO = new EsitoAppuntamentoSilpDTO();
    try {
      SlotDTO slot = prenotazione.getSlot();
      GiornoDTO giorno = slot.getGiorno();
      CalendarioDTO calendario = giorno.getPeriodo().getCalendario();
      AppuntamentoInputParam params = new AppuntamentoInputParam();
      params.setCodiceApplicativoChiamante(Constants.CLIENT_SILP_NAME);
      params.setCodiceFiscaleOperatore(prenotazione.getCodUserAggiorn());
      params.setIdIncontro(prenotazione.getIdSilInContatto());
      params.setIdStatoIncontro("" + DecodificheHelper.getIdSilpStatoIncontro(prenotazione.getIdStatoAppuntamento()));
      params.setIdLavoratore(prenotazione.getUtente().getIdSilLavAngrafica());
      //Long idServizio = calendario.getAmbito().getIdSilTInServizio();
      Long idServizio = findElencoServizioPSLP(calendario.getAmbito().getCodAmbito(),prenotazione.getCodUserAggiorn()).getIdSilInServizio();
      params.setIdServizio(idServizio);
      params.setDataIncontro(DateUtils.toXmlGregorianCalendar(giorno.getGiorno()));
      params.setOrarioInizio(slot.getDescrizioneOraInizio());
      if(DecodificheHelper.COD_STATO_INCONTRO_SPOSTATO_SP.equals(prenotazione.getIdStatoAppuntamento()))
        params.setDurataMinuti(0L);
      else
        params.setDurataMinuti(new Long(slot.getOraFine() - slot.getOraInizio()));
      
      
      params.setNote(prenotazione.getNote());
      params.setGruppoOperatoreEnte(calendario.getGruppoOperatore());
      if (calendario.getCodOperatore() != null) params.setCodiceOperatoreEnte("" + calendario.getCodOperatore());
      if (calendario.getSubcodice() != null) params.setCodiceSedeEnte("" + calendario.getSubcodice());

      esitoDTO = callSaveAppuntamento(params);
      log.info("[AdapterSilpsvinWSImpl::saveAppuntamento] EsitoAppuntamentoSilpDTO=" + esitoDTO.getCodiceEsito() + " - " + esitoDTO.getDescrizioneEsito() + " messaggi:"+esitoDTO.getDettaglioEsitoAsString() + " appuntamento=" + esitoDTO.getAppuntamento() );
 
      return esitoDTO;

    }
    catch (Throwable ex) {
      log.error("[AdapterSilpsvinWSImpl::saveAppuntamento]", ex);
      esitoDTO.addMessageErrorCodiceAndDescrizioneFromCodEsito(EsitoDTO.S0001_ERRORE_DI_SISTEMA);
      return esitoDTO;
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvinWSImpl", "saveAppuntamento()", "invocazione servizio [SILPSV.silpsvin::saveAppuntamentoJSON]", "");
      watcher.stop();
    }
  }

  /**
   * Chiama effettivamente il servizio di silpsvin con i parametri richiesti in
   * input ritornando un esito
   * 
   * @param params
   * @return
   * @throws ServiziSilpException_Exception
   * @throws Exception
   */
  public EsitoAppuntamentoSilpDTO callSaveAppuntamento(AppuntamentoInputParam params) throws Exception {
    EsitoAppuntamentoSilpDTO esitoDTO;
    log.debug("[AdapterSilpsvinWSImpl::callSaveAppuntamento] sto per fare l'effettiva chiamata a silpsvin... params=" + dump(params));
    WSContainerDTO esitoWS = factory.getService().saveAppuntamentoJSON(Constants.CLIENT_SILP_NAME, params);
    log.debug("[AdapterSilpsvinWSImpl::callSaveAppuntamento] chiamata effettuata esitoWS=" + esitoWS.getValue());
    esitoDTO = (EsitoAppuntamentoSilpDTO) GsonUtils.toGsonObject(esitoWS.getValue(), EsitoAppuntamentoSilpDTO.class);
    return esitoDTO;
  }
  
  
  /**
   * Reperisce un elenco di enti da silpsvin
   */
  public EnteSilpHeaderDTO findEnteSilp(String gruppoOperatore,String codiceOperatore, String subcodice,String codiceFiscaleOperatore) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start(); 
    try {
      log.debug(String.format("[AdapterSilpsvinWSImpl::findEnteSilp] ricerca ente per codici %s %s %s",gruppoOperatore,codiceOperatore,subcodice));
      FindElencoEntiInputParam filter = new FindElencoEntiInputParam();
      filter.setCodiceApplicativoChiamante(Constants.CLIENT_SILP_NAME);
      filter.setCodiceFiscaleOperatore(codiceFiscaleOperatore);
      filter.setTipoRicercaEnte("CPI_S");
      WSContainerDTO esitoWS = factory.getService().findElencoEnti(Constants.CLIENT_SILP_NAME, filter);
      EsitoFindElencoEntiSilpDTO esitoDTO = (EsitoFindElencoEntiSilpDTO)GsonUtils.toGsonObject(esitoWS.getValue(),EsitoFindElencoEntiSilpDTO.class);
      
      if(!esitoDTO.isEsitoPositivo()) {
        throw new Exception("Errore nella chiamata a silpsvin.findElencoEnti: "+esitoDTO.getCodiceEsito()+" - "+esitoDTO.getDescrizioneEsito()+" - "+esitoDTO.getDettaglioEsitoAsString());
      }
     
      EnteSilpHeaderDTO enteTrovato = null;
      String keyDaCercare = gruppoOperatore+codiceOperatore+subcodice;
      for(EnteSilpHeaderDTO enteDTO: esitoDTO.getElencoEnti()) {
          String keyEnte = enteDTO.getGruppoOperatoreEnte()+enteDTO.getCodiceOperatoreEnte()+enteDTO.getSubCodiceEnte();
    	  if(keyDaCercare.equals(keyEnte)){
    		  enteTrovato = enteDTO;
    		  break;
    	  }
      }
      log.debug(String.format("[AdapterSilpsvinWSImpl::findEnteSilp] trovato ente %s",enteTrovato));
      return enteTrovato;
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvinWSImpl", "findElencoEnti()", "invocazione servizio [SILPSV.silpsvin::findElencoEnti]", "");
      watcher.stop();
    }
  }
  
  /**
   * Reperisce un elenco di enti da silpsvin
   */
  public ServizioPSLPDTO findElencoServizioPSLP(String codAmbito, String codiceFiscaleOperatore) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start(); 
    try {
      log.debug(String.format("[AdapterSilpsvinWSImpl::findElencoServizioPSLP] codAmbito=%s, codiceFiscaleOperatore=%s",codAmbito,codiceFiscaleOperatore));
      ElencoServizioInputParam filter = new ElencoServizioInputParam();
      filter.setCodOperatore(codiceFiscaleOperatore);
      filter.setTipoServizio(codAmbito);
      WSContainerDTO esitoWS = factory.getService().findElencoServizioPSLP(Constants.CLIENT_SILP_NAME, filter);
      EsitoFindElencoServizioPSLPDTO esitoDTO = (EsitoFindElencoServizioPSLPDTO)GsonUtils.toGsonObject(esitoWS.getValue(),EsitoFindElencoServizioPSLPDTO.class);
      
      if(!esitoDTO.isEsitoPositivo()) {
        throw new Exception("Errore nella chiamata a silpsvin.findElencoServizioPSLP: "+esitoDTO.getCodiceEsito()+" - "+esitoDTO.getDescrizioneEsito()+" - "+esitoDTO.getDettaglioEsitoAsString());
      }
      if(esitoDTO.getElencoServizioPSLP()==null || esitoDTO.getElencoServizioPSLP().size()==0 || esitoDTO.getElencoServizioPSLP().get(0)==null) {
        throw new Exception("Errore nella chiamata a silpsvin.findElencoServizioPSLP: servizio non reperito per ambito "+codAmbito);
      }
      
      ServizioPSLPDTO servizio = esitoDTO.getElencoServizioPSLP().get(0);
      log.debug(String.format("[AdapterSilpsvinWSImpl::findElencoServizioPSLP] trovato servizio %s",servizio));
      return servizio;
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvinWSImpl", "findElencoServizioPSLP()", "invocazione servizio [SILPSV.silpsvin::findElencoServizioPSLP]", "");
      watcher.stop();
    }
  }

  private String dump(AppuntamentoInputParam params) {

			StringBuilder builder = new StringBuilder();
			builder.append("SaveIncontroInputParam [");
			if (params.getCodiceFiscale() != null) {
				builder.append("codiceFiscale=");
				builder.append(params.getCodiceFiscale());
				builder.append(", ");
			}
			if (params.getIdLavoratore() != null) {
				builder.append("idLavoratore=");
				builder.append(params.getIdLavoratore());
				builder.append(", ");
			}
			if (params.getIdIncontro() != null) {
				builder.append("idIncontro=");
				builder.append(params.getIdIncontro());
				builder.append(", ");
			}
			if (params.getIdServizio() != null) {
				builder.append("idServizio=");
				builder.append(params.getIdServizio() );
				builder.append(", ");
			}
			if (params.getIdStatoIncontro() != null) {
				builder.append("idStatoIncontro=");
				builder.append(params.getIdStatoIncontro());
				builder.append(", ");
			}
			if (params.getDataIncontro() != null) {
				builder.append("dataIncontro=");
				builder.append(params.getDataIncontro());
				builder.append(", ");
			}
			if (params.getOrarioInizio() != null) {
				builder.append("orarioInizio=");
				builder.append(params.getOrarioInizio());
				builder.append(", ");
			}
			if (params.getDurataMinuti() != null) {
				builder.append("durataMinuti=");
				builder.append(params.getDurataMinuti() );
				builder.append(", ");
			}
			if (params.getGruppoOperatoreEnte() != null) {
				builder.append("gruppoOperatoreEnte=");
				builder.append(params.getGruppoOperatoreEnte());
				builder.append(", ");
			}
			if (params.getCodiceOperatoreEnte() != null) {
				builder.append("codiceOperatoreEnte=");
				builder.append(params.getCodiceOperatoreEnte());
				builder.append(", ");
			}
			if (params.getCodiceSedeEnte() != null) {
				builder.append("codiceSedeEnte=");
				builder.append(params.getCodiceSedeEnte());
				builder.append(", ");
			}
			if (params.getCodiceApplicativoChiamante() != null) {
				builder.append("codiceApplicativoChiamante=");
				builder.append(params.getCodiceApplicativoChiamante());
			}
			if (params.getCodiceFiscaleOperatore() != null) {
				builder.append("codiceFiscaleOperatore=");
				builder.append(params.getCodiceFiscaleOperatore());
			}
			if (params.getNote() != null) {
				builder.append("note=");
				builder.append(params.getNote());
			}
			builder.append("]");
			return builder.toString();
		}
  
  public String saveStatoSms(Long idSms, String codiceStato, Date dataStato, String codiceErrore) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start(); 
    try {
      log.debug(String.format("[AdapterSilpsvinWSImpl::saveStatoSms] idSms=%s, codiceStato=%s, dataStato=%s, codiceErrore=%s",idSms,codiceStato,dataStato,codiceErrore));
      InputSetStatoSmsSilpDTO params = new InputSetStatoSmsSilpDTO();
      params.setCodiceApplicativoChiamante(Constants.CLIENT_SILP_NAME);
      params.setIdSmsPlsorch(idSms);
      params.setCodStatoSms(codiceStato);
      params.setDtStato(DateUtils.toXmlGregorianCalendar(dataStato));
      params.setCodErrore(codiceErrore);
      WSContainerDTO esitoWS = factory.getService().saveStatoSmsJSON(Constants.CLIENT_SILP_NAME, params);
      EsitoSetStatoSmsDTO esitoDTO = (EsitoSetStatoSmsDTO)GsonUtils.toGsonObject(esitoWS.getValue(),EsitoSetStatoSmsDTO.class);
      
      if(!esitoDTO.isEsitoPositivo()) {
        log.error("[AdapterSilpsvinWSImpl::saveStatoSms] Errore nella chiamata a silpsvin.saveStatoSms: "+esitoDTO.getCodiceEsito()+" - "+esitoDTO.getDescrizioneEsito()+" - "+esitoDTO.getDettaglioEsitoAsString());
        return esitoDTO.getDettaglioEsitoAsString();
      }
      return null;
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvinWSImpl", "saveStatoSms()", "invocazione servizio [SILPSV.silpsvin::saveStatoSms]", "");
      watcher.stop();
    }
  }
 }


