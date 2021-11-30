/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.GsonUtils;
import it.csi.silos.silcommon.util.SilCommonUtils;
import it.csi.silpcommonobj.dati.redditodicittadinanza.EsitoFindBeneficiarioRdcDTO;
import it.csi.silpcommonobj.dati.schedaprofessionale.sap.SchedaAnagraficoProfessionaleReportDTO;
import it.csi.silpservizi.sesp.bo.sap.SchedaAnagraficoProfessionaleLavoratoreDTO;
import it.csi.silpsv.silpsvsp.cxfclient.SchedaAnagraficoProfessionaleLavoratoreFilter;
import it.csi.silpsv.silpsvsp.cxfclient.WSContainerDTO;
import it.csi.util.performance.StopWatch;

public class AdapterSilpsvspWSImpl {

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

  private static final String COD_MIN_PIEMONTE = "13";

  public AdapterSilpsvspWSFactory factory = new AdapterSilpsvspWSFactory();

  private static AdapterSilpsvspWSImpl instance = null;

  public static AdapterSilpsvspWSImpl getInstance() {
    if (instance == null) {
      instance = new AdapterSilpsvspWSImpl();
    }
    return instance;
  }

  public SchedaAnagraficoProfessionaleReportDTO getReportSAP(Long idSilLavAnagrafica) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start();
    SchedaAnagraficoProfessionaleReportDTO result = new SchedaAnagraficoProfessionaleReportDTO();
    try {
      SchedaAnagraficoProfessionaleLavoratoreFilter filter = new SchedaAnagraficoProfessionaleLavoratoreFilter();
      filter.setIdLavoratore(idSilLavAnagrafica);
      WSContainerDTO sapWS = factory.getService().getSAP(Constants.CLIENT_SILP_NAME, filter);
      SchedaAnagraficoProfessionaleLavoratoreDTO sapLav = (SchedaAnagraficoProfessionaleLavoratoreDTO) GsonUtils.toGsonObject(sapWS.getValue(), SchedaAnagraficoProfessionaleLavoratoreDTO.class);
      result.setSap(sapLav.getSchedaAnagraficaProfessionale());
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvspWSImpl", "getSAP()", "invocazione servizio [SILPSV.silpsvsp::getSAP]", "");
      watcher.stop();
    }
    return result;
  }

  /**
   * Reperisce la sap di un lavoratore dall'id o cf
   */
  public SchedaAnagraficoProfessionaleLavoratoreDTO getSAP(Long idSilLavAnagrafica,String codiceFiscale) throws Exception {
    StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
    watcher.start();
    try {
      if(SilCommonUtils.isVoid(idSilLavAnagrafica) &&  SilCommonUtils.isVoid(codiceFiscale)) {
    	  throw new IllegalArgumentException("Attenzione si sta cercando di richiedere una sap senza id o cf");
      }
      SchedaAnagraficoProfessionaleLavoratoreFilter filter = new SchedaAnagraficoProfessionaleLavoratoreFilter();
      filter.setIdLavoratore(idSilLavAnagrafica);
      filter.setCodiceFiscale(codiceFiscale);
      WSContainerDTO sapWS = factory.getService().getSAP(Constants.CLIENT_SILP_NAME, filter);
      SchedaAnagraficoProfessionaleLavoratoreDTO sap = (SchedaAnagraficoProfessionaleLavoratoreDTO) GsonUtils.toGsonObject(sapWS.getValue(), SchedaAnagraficoProfessionaleLavoratoreDTO.class);
      return sap;
    }
    finally {
      watcher.dumpElapsed("AdapterSilpsvspWSImpl", "getSAP()", "invocazione servizio [SILPSV.silpsvsp::getSAP]", "");
      watcher.stop();
    }
  }

  /**
   * 
   * @param idSilLavAnagrafica
   * @return
   * @throws Exception
   */
  public EsitoFindBeneficiarioRdcDTO getDomandaRDCBySILP(Long idLavoratoreSilp) throws Exception {
      StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
      watcher.start();
      EsitoFindBeneficiarioRdcDTO result = null;
      try {
       WSContainerDTO esitoWSContainer = factory.getService().findBeneficiarioHeader(Constants.CLIENT_SILP_NAME, idLavoratoreSilp);

       result = GsonUtils.toGsonObject(esitoWSContainer.getValue(), EsitoFindBeneficiarioRdcDTO.class);
        return result;
      }
      finally {
        watcher.dumpElapsed("AdapterSilpsvspWSImpl", "getDomandaRDCBySILP()", "invocazione servizio [SILPSV.silpsvsp::getDomandaRDCBySILP]", "");
        watcher.stop();
      }
    }

}
