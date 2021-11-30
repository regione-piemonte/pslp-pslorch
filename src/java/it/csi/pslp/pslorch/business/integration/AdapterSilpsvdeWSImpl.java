/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.GsonUtils;
import it.csi.silpcommonobj.dati.incontri.StatoAdesioneSilpDTO;
import it.csi.silpcommonobj.dati.incontri.StatoIncontroSilpDTO;
import it.csi.silpcommonobj.filter.incontri.EsitoFindElencoStatoAdesioneDTO;
import it.csi.silpcommonobj.filter.incontri.EsitoFindElencoStatoIncontroDTO;
import it.csi.silpsv.silpsvde.cxfclient.FindElencoStatoAdesioneInputParam;
import it.csi.silpsv.silpsvde.cxfclient.FindElencoStatoIncontroInputParam;
import it.csi.silpsv.silpsvde.cxfclient.WSContainerDTO;
import it.csi.util.performance.StopWatch;

public class AdapterSilpsvdeWSImpl {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    public AdapterSilpsvdeWSFactory factory = new AdapterSilpsvdeWSFactory();

    private static AdapterSilpsvdeWSImpl instance = null;

    public static AdapterSilpsvdeWSImpl getInstance() {
        if (instance == null) {
            instance = new AdapterSilpsvdeWSImpl();
        }
        return instance;
    }

    public List<StatoIncontroSilpDTO> findStatiIncontro(String codiceFiscaleUtente) throws Exception {
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        List<StatoIncontroSilpDTO> result = new ArrayList<>();
        try {
            FindElencoStatoIncontroInputParam filter = new FindElencoStatoIncontroInputParam();
            filter.setCodOperatore(codiceFiscaleUtente);
            // filter.setDataRiferimento(DateUtils.toXmlGregorianCalendar(new Date()));
            WSContainerDTO esitoWS = factory.getService().findElencoStatoIncontroJSON(Constants.CLIENT_SILP_NAME,
                    filter);
            EsitoFindElencoStatoIncontroDTO esitoDTO = (EsitoFindElencoStatoIncontroDTO) GsonUtils
                    .toGsonObject(esitoWS.getValue(), EsitoFindElencoStatoIncontroDTO.class);
            log.info("[AdapterSilpsvdeWSImpl::findStatiIncontro] esitoDTO=" + esitoDTO.getCodiceEsito() + " - "
                    + esitoDTO.getDescrizioneEsito());
            if (!esitoDTO.isEsitoPositivo()) {
                throw new Exception("Errore nella chiamata a silpsvde.findElencoStatoIncontroJSON: "
                        + esitoDTO.getCodiceEsito() + " - " + esitoDTO.getDescrizioneEsito() + " - "
                        + esitoDTO.getDettaglioEsitoAsString());
            }
            Date now = new Date();
            for (StatoIncontroSilpDTO statoDTO : esitoDTO.getElencoStatoIncontro()) {
                Date dataInizio = statoDTO.getDataInizio();
                Date dataFine = statoDTO.getDataFine();
                if (dataInizio != null && dataInizio.after(now)) {
                    continue;
                }
                if (dataFine != null && dataFine.before(now)) {
                    continue;
                }
                result.add(statoDTO);
            }
        } finally {
            watcher.dumpElapsed("AdapterSilpsvdeWSImpl", "findStatiIncontro()",
                    "invocazione servizio [SILPSV.silpsvde::findElencoStatoIncontroJSON]", "");
            watcher.stop();
        }
        return result;
    }

    public Map<String, StatoIncontroSilpDTO> getMapStatiIncontroPerCodAnpal(String codiceFiscaleUtente)
            throws Exception {
        Map<String, StatoIncontroSilpDTO> result = new HashMap<>();
        List<StatoIncontroSilpDTO> stati = findStatiIncontro(codiceFiscaleUtente);
        for (StatoIncontroSilpDTO stato : stati) {
            result.put(stato.getCodAnpal(), stato);
        }
        return result;
    }

    public List<StatoAdesioneSilpDTO> findStatiAdesione(String codiceFiscaleUtente) throws Exception {
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        List<StatoAdesioneSilpDTO> result = new ArrayList<>();
        try {
            FindElencoStatoAdesioneInputParam filter = new FindElencoStatoAdesioneInputParam();
            filter.setCodOperatore("BRNNDR77T08F952L");
            WSContainerDTO esitoWS = factory.getService().findElencoStatoAdesioneJSON("PSLWEB", filter);
            EsitoFindElencoStatoAdesioneDTO esitoDTO = (EsitoFindElencoStatoAdesioneDTO) GsonUtils
                    .toGsonObject(esitoWS.getValue(), EsitoFindElencoStatoAdesioneDTO.class);
            log.info("[AdapterSilpsvdeWSImpl::findStatiAdesione] esitoDTO=" + esitoDTO.getCodiceEsito() + " - "
                    + esitoDTO.getDescrizioneEsito());
            if (!esitoDTO.isEsitoPositivo()) {
                throw new Exception("Errore nella chiamata a silpsvde.findStatiAdesione: " + esitoDTO.getCodiceEsito()
                        + " - " + esitoDTO.getDescrizioneEsito() + " - " + esitoDTO.getDettaglioEsitoAsString());
            }
            for (StatoAdesioneSilpDTO statoDTO : esitoDTO.getElencoStatoAdesione()) {
                result.add(statoDTO);
            }
        } finally {
            watcher.dumpElapsed("AdapterSilpsvdeWSImpl", "findStatiAdesione()",
                    "invocazione servizio [SILPSV.silpsvde::findStatiAdesione]", "");
            watcher.stop();
        }
        return result;
    }

}
