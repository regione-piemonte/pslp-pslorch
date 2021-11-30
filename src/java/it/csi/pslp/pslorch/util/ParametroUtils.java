/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.ParametroDBDef;
import it.csi.pslp.pslcommonobj.dto.ParametroDTO;
import it.csi.pslp.pslcommonobj.filter.ParametroFilter;
import it.csi.pslp.pslorch.business.SpringApplicationContextHelper;
import it.csi.silos.jedi.core.DAO;

@Component("parametroUtils")
public class ParametroUtils {

    public static final String MITTENTE = "MITTENTE";
    public static final String MITTENTE_ALIAS = "MITTENTE_ALIAS";
    public static final String DESTINATARIO_TEST = "DESTINATARIO_TEST";
    public static final String FIRMA = "FIRMA";
    public static final String URL_PSLP = "URL_PSLP";
    public static final String URL_SPID = "URL_SPID";
    public static final String PROMEM_MARGINE_ORE = "PROMEM_MARGINE_ORE";
    public static final String FINE_GG_FLG = "FINE_GG_FLG";
    public static final String FINE_GG_DT_MAIL = "FINE_GG_DT_MAIL";
    public static final String RIC_GG_DT_MAIL = "RIC_GG_DT_MAIL";
    public static final String PSES_DT_MAIL = "PSES_DT_MAIL";
    public static final String DIDR1_DT_MAIL = "DIDR1_DT_MAIL";
    public static final String GGR1_DT_MAIL = "GGR1_DT_MAIL";
    public static final String GGR2_DT_MAIL = "GGR2_DT_MAIL";
    public static final String RCANN_DT_MAIL = "RCANN_DT_MAIL";
    public static final String DIDR2_DT_MAIL = "DIDR2_DT_MAIL";
    public static final String GG_INVIO_MAIL_KO = "GG_INVIO_MAIL_KO";
    public static final String RIC_RDC_DT_MAIL = "RIC_RDC_DT_MAIL";
    public static final String INVIO_MAIL_ASINCRONO = "INVIO_MAIL_ASINCRONO";
    public static final String SMS_PASSWORD = "SMS_PASSWORD";
    public static final String SMS_ORE_REINVIO = "SMS_ORE_REINVIO";

    public static final String URL_IO_LAVORO_VIRT = "URL_IO_LAVORO_VIRT";
    public static final String URL_CHIAMATE_L68_99 = "URL_CHIAMATE_L68_99";
    public static final String URL_AREA_DISABILITA = "URL_AREA_DISABILITA";
    public static final String URL_BUONO_SERV_LAV = "URL_BUONO_SERV_LAV";

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private DAO dao;

    // private static final ParametroUtils instance = new ParametroUtils();

    public static ParametroUtils getInstance() {
        return (ParametroUtils) SpringApplicationContextHelper.getBean("parametroUtils");
    }

    public String getParametro(String codice, String defaultValue) throws Exception {
        String value = getParametro(codice);
        if (value == null)
            value = defaultValue;
        return value;
    }

    /**
     * Ritorna il valore di un paramero identificato tramite codice. Viene data
     * precedenza al valore presente nel campo clob (se significativo), altrimenti
     * quello piu' breve descrittivo
     * 
     * @param codice
     * @return
     * @throws Exception
     */
    public String getParametro(String codice) throws Exception {
        Date now = new Date();
        ParametroFilter filter1 = new ParametroFilter();
        filter1.getCodParametro().eq(codice);
        filter1.getDtInizio().le(now);
        ParametroFilter filter2 = new ParametroFilter();
        filter2.getDtFine().eq(null);
        ParametroFilter filter3 = new ParametroFilter();
        filter3.getDtFine().ge(now);
        ParametroFilter filter = (ParametroFilter) filter1.and(filter2.or(filter3));
        ParametroDTO parametroDTO = dao.findFirst(ParametroDBDef.class, filter);
        if (parametroDTO != null) {
            if (parametroDTO.getValoreParametroExt() != null && !"<CLOB>".equals(parametroDTO.getValoreParametroExt())) {
                return parametroDTO.getValoreParametroExt();
            } else {
                return parametroDTO.getValoreParametro();
            }
        }
        return null;
    }

}
