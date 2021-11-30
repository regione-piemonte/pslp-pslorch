/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.util.Date;

import org.apache.log4j.Logger;

import it.csi.pslp.pslcommonobj.dbdef.MessaggioAggiuntivoDBDef;
import it.csi.pslp.pslcommonobj.dto.MessaggioAggiuntivoDTO;
import it.csi.pslp.pslcommonobj.dto.MessaggioDTO;
import it.csi.pslp.pslcommonobj.filter.MessaggioAggiuntivoFilter;
import it.csi.pslp.pslorch.business.common.PslDAO;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggiUtils;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.silcommon.util.SilTimeUtils;

public abstract class MailMessagePlaceholderAbstractHandler<T> implements MailMessagePlaceholderHandler<T> {

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    /**
     * Ritorna l'url della piattaforma PSLP
     * 
     * @return
     * @throws Exception
     */
    protected String getUrlPSLP() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_PSLP);
    }

    protected String getUrlIoLavoroVirt() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_IO_LAVORO_VIRT);
    }

    protected String getUrlChiamateL68() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_CHIAMATE_L68_99);
    }

    protected String getUrlAreaDisabilita() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_AREA_DISABILITA);
    }

    protected String getUrlBuonoServLav() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_BUONO_SERV_LAV);
    }

    protected String getUrlSpid() throws Exception {
        return ParametroUtils.getInstance().getParametro(ParametroUtils.URL_SPID);
    }

    /**
     * Carica eventuale messaggio aggiuntivo cercando nella tabella
     * PSLP_T_MESSAGGIO_AGGIUNTIVO per calendario , tipo messaggio e operatore,
     * reperisce il testo aggiuntivo
     * 
     * @param codiceMessaggio
     * @param dataHolder
     * @return
     * @throws DAOException
     */
    protected String getMessaggioAggiuntivoConfiguratoByIdCalendario(String codiceMessaggio, Long idCalendario) throws Exception {
        String testoMessaggioAggiuntivo = "";
        log.debug(String.format(this.getClass().getSimpleName() + " ricerca messaggio aggiuntivo per codice %s e idCalendario %s", codiceMessaggio,
                idCalendario));
        // Carico l'id specifico legato al codice del messaggio
        MessaggioDTO m = MessaggiUtils.getInstance().loadMessaggio(codiceMessaggio);
        MessaggioAggiuntivoFilter filter = new MessaggioAggiuntivoFilter();
        filter.getMessaggio().getIdMessaggio().eq(m.getIdMessaggio());
        if (idCalendario != null) {
            filter.getCalendario().getIdCalendario().eq(idCalendario);
        }
//		 filter.getGruppoOperatore().eq(cal.getGruppoOperatore()); 
//		 filter.getCodOperatore().eq(cal.getCodOperatore()); 
//		 filter.getSubcodice().eq(cal.getSubcodice()); 

        MessaggioAggiuntivoDTO ma = PslDAO.getInstance().getDao().findFirst(MessaggioAggiuntivoDBDef.class, filter);

        if (ma != null) {
            log.debug(this.getClass().getSimpleName() + " identificato messaggio aggiuntivo configurato con id=" + ma.getIdMessaggioAggiuntivo());
            testoMessaggioAggiuntivo = ma.getTesto();
        }

        return testoMessaggioAggiuntivo;
    }

    /**
     * Carica un messaggio aggiuntivo configurato solo per il dato codice messaggio
     * 
     * @param codiceMessaggio
     * @return
     * @throws Exception
     */
    protected String getMessaggioAggiuntivoConfiguratoByCodiceMessaggio(String codiceMessaggio) throws Exception {
        return getMessaggioAggiuntivoConfiguratoByIdCalendario(codiceMessaggio, null);
    }

    /**
     * Sostituisce da un template i placeholdere che devono contenere una data,
     * sostituendoli con un normale placeholder java {N}.
     * 
     * @param bodyTemplate
     * @return
     */
    protected String prepareDatePlaceholders(String bodyTemplate) {
        String DATE_PLACEHOLDER_TEMPLATE = ",date,dd/MM/yyyy";
        while (bodyTemplate.indexOf(DATE_PLACEHOLDER_TEMPLATE) > 0) {
            bodyTemplate = bodyTemplate.replaceAll(DATE_PLACEHOLDER_TEMPLATE, "");

        }
        return bodyTemplate;
    }

    protected boolean isParametroInvioMailAttivoAdOggi(String codParametro) throws Exception {
        Date oggi = SilTimeUtils.today();

        Date dataInizioInvioMail = SilTimeUtils.convertStringaInData(ParametroUtils.getInstance().getParametro(codParametro));

        boolean invioMailPossibile = dataInizioInvioMail == null || SilTimeUtils.isData1MinoreOUgualeData2(dataInizioInvioMail, oggi);
        if (!invioMailPossibile) {
            log.info(getClass().getSimpleName() + ".isParametroInvioMailAttivo invio mail bloccato per parametro " + codParametro + " pari a "
                    + dataInizioInvioMail + " e data attuale " + oggi);
            return false;
        }
        return true;
    }

    @Override
    public Long getIdUtente(T dataHolder) {
        return null;
    }

    @Override
    public Long getIdSilRifAmbito(T dataHolder) {
        return null;
    }

    @Override
    public String replacePlaceholdersIntestazione(String intestazione, T dataHolder) {

        return intestazione;
    }

}
