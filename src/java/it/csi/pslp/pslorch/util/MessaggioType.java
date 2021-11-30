/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
 
package it.csi.pslp.pslorch.util;

import it.csi.pslp.pslcommonobj.dto.DocumentoDTO;
import it.csi.pslp.pslcommonobj.dto.ParametriInvioMail;
import it.csi.pslp.pslcommonobj.dto.PrenotazioneDTO;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderAPANNHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderAPCONHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderAPNPRHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderAPPROHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderDIDR1Handler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderDIDR2Handler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderDOCESHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderGGENDEXTHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderGGENDHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderGGR1Handler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderGGR2Handler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderGGRICHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderL68ACHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderL68ANHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderL68INHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderL68RIHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderPSESHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCANNHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCCONHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCDOCHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCNPRHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCPROHandler;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderRCRICHandler;
import it.csi.pslp.pslorch.util.mail.ParametriMailGG;

@SuppressWarnings("unchecked")
public enum MessaggioType {

    /** Mail per completare adesione Garanzia Giovani */
    GGRIC("GGRIC") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderGGRICHandler();
        }
    },
    /** Mail per confermare la prenotazione GG */
    APCON("APCON") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderAPCONHandler();
        }
    },
    /** Mail per confermare la prenotazione RDC */
    RCCON("RCCON") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderRCCONHandler();
        }
    },
    /** Mail per promemoria prenotazione */
    APPRO("APPRO") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderAPPROHandler();
        }
    },
    /** Mail per promemoria prenotazione */
    RCPRO("RCPRO") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderRCPROHandler();
        }
    },
    /** Mail in caso di annullo prenotazione da parte di operatore CPI */
    APANN("APANN") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderAPANNHandler();
        }
    },
    /** Mail in caso di annullo prenotazione da parte di operatore CPI */
    RCANN("RCANN") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderRCANNHandler();
        }
    },
    /** Mail in caso di mancata presentazione del giovane all'appuntamento */
    APNPR("APNPR") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderAPNPRHandler();
        }
    },
    /** Mail in caso di mancata presentazione del giovane all'appuntamento */
    RCNPR("RCNPR") {
        @Override
        public MailMessagePlaceholderHandler<PrenotazioneDTO> getHandler() {
            return new MailMessagePlaceholderRCNPRHandler();
        }
    },
    /**
     * Mail in caso di termine dell'adesione Garanzia Giovani chaimata internamente
     * da PSLP
     */
    GGEND("GGEND") {
        @Override
        public MailMessagePlaceholderHandler<ParametriMailGG> getHandler() {
            return new MailMessagePlaceholderGGENDHandler();
        }
    },
    /**
     * Mail in caso di termine dell'adesione Garanzia Giovani chiamata da sistemi
     * esterni a PSLP
     */
    GGENDEXT("GGENDEXT") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderGGENDEXTHandler();
        }
    },
    /**
     * Mail in caso di accettazione o non accettazione del documento fornito in
     * allegato alla prenotazione appuntamento per ambito GARANZIA GIOVANI
     */
    DOCES("DOCES") {
        @Override
        public MailMessagePlaceholderHandler<DocumentoDTO> getHandler() {
            return new MailMessagePlaceholderDOCESHandler();
        }
    },
    /**
     * Mail in caso di accettazione o non accettazione del documento fornito in
     * allegato alla prenotazione appuntamento per ambito RDC
     */
    RCDOC("RCDOC") {
        @Override
        public MailMessagePlaceholderHandler<DocumentoDTO> getHandler() {
            return new MailMessagePlaceholderRCDOCHandler();
        }
    },
    /** Mail per Reddito di Cittadinanza: completamento domanda online */
    RCRIC("RCRIC") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderRCRICHandler();
        }
    },
    /** Mail per rifiuto completo adesione */
    GGR1("GGR1") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderGGR1Handler();
        }
    },
    /** Mail per rifiuto di singolo stato adesione */
    GGR2("GGR2") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderGGR2Handler();
        }
    },
    /** Mail per rifiuto completo DID */
    DIDR1("DIDR1") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderDIDR1Handler();
        }
    },
    /** Mail per rifiuto singolo stato did */
    DIDR2("DIDR2") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderDIDR2Handler();
        }
    },
    /** Mail per accettazione/non accettazione patto di servizio */
    PSES("PSES") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderPSESHandler();
        }
    },
    /**
     * Mail per Richiesta Collocamento Mirato Piemonte: Inviata per la validazione
     */
    L68IN("L68IN") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderL68INHandler();
        }
    },
    /**
     * Mail per Richiesta Collocamento Mirato Piemonte: Accolta
     */
    L68AC("L68AC") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderL68ACHandler();
        }
    },
    /**
     * Mail per Richiesta Collocamento Mirato Piemonte: Respinta
     */
    L68RI("L68RI") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderL68RIHandler();
        }
    },
    /**
     * Mail per Richiesta Collocamento Mirato Piemonte: Annullata
     */
    L68AN("L68AN") {
        @Override
        public MailMessagePlaceholderHandler<ParametriInvioMail> getHandler() {
            return new MailMessagePlaceholderL68ANHandler();
        }
    },;

    private final String code;

    private MessaggioType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public abstract <T> MailMessagePlaceholderHandler<T> getHandler();

}
