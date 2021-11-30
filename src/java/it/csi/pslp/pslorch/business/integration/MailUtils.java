/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslcommonobj.dbdef.EventoDBDef;
import it.csi.pslp.pslcommonobj.dbdef.MessaggioUtenteDBDef;
import it.csi.pslp.pslcommonobj.dto.EventoDTO;
import it.csi.pslp.pslcommonobj.dto.MessaggioDTO;
import it.csi.pslp.pslcommonobj.dto.MessaggioUtenteDTO;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.MessaggiUtils;
import it.csi.pslp.pslorch.util.MessaggioType;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.pslp.pslorch.util.mail.MailMessagePlaceholderHandler;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAOException;
import it.csi.util.performance.StopWatch;

@Component("mailUtils")
public class MailUtils {

    private static final String ENVIRONMENT_PROD = "prod";

    private static final Long TIPO_EVENTO_MAIL = 3L;

    private volatile String host;
    private volatile String port;
    private volatile String mode;

    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    @Autowired
    private DAO dao;

    @Autowired
    private MessaggiUtils messaggiUtils;

    public <T> void sendMail(T parametriInvioMail, MessaggioType codiceMessaggio, boolean inviaSubito) throws Exception {
        sendMail(parametriInvioMail, codiceMessaggio, "-", inviaSubito);
    }

    public <T> void sendMail(T parametriInvioMail, MessaggioType codiceMessaggio, String codiceFiscaleUtenteCollegato, boolean inviaSubito) throws Exception {
        getParameters();
        String from = ParametroUtils.getInstance().getParametro(ParametroUtils.MITTENTE);
        String fromAlias = ParametroUtils.getInstance().getParametro(ParametroUtils.MITTENTE_ALIAS);
        String firma = ParametroUtils.getInstance().getParametro(ParametroUtils.FIRMA);
        MessaggioDTO messaggio = messaggiUtils.loadMessaggio(codiceMessaggio);
        MailMessagePlaceholderHandler<T> handler = codiceMessaggio.getHandler();
        if (messaggio == null) {
            log.warn("[MailUtils::sendMail] testo mail di tipo " + codiceMessaggio + " nullo. Mail non inviata con parametri " + parametriInvioMail);
            return;
        }

        if (handler.isPossibileInviareMail(parametriInvioMail)) {
            String subject = handler.replacePlaceholdersIntestazione(messaggio.getIntestazione(), parametriInvioMail);
            String[] to = handler.handleTo(parametriInvioMail);
            String message = handler.replacePlaceholders(messaggio.getCodMessaggio(), messaggio.getTesto(), parametriInvioMail, firma);
            String codiceFiscale = handler.getCodiceFiscaleUtente(parametriInvioMail);
            if (codiceFiscale == null)
                codiceFiscale = codiceFiscaleUtenteCollegato;

            // Traccia l'invio della mail
            MessaggioUtenteDTO messaggioUtente = tracciaEventoInvioMail(parametriInvioMail, codiceFiscale, from, messaggio, handler, subject, to, message);

            String invioAsincrono = ParametroUtils.getInstance().getParametro(ParametroUtils.INVIO_MAIL_ASINCRONO, "S");
            if ("N".equals(invioAsincrono) || inviaSubito) {
                sendMail(from, fromAlias, to, null, subject, message, messaggioUtente);
            }
        } else {
            log.warn("[MailUtils::sendMail] Mail non inviata con parametri " + parametriInvioMail);
        }
    }

    private <T> MessaggioUtenteDTO tracciaEventoInvioMail(T parametriInvioMail, String codiceFiscaleUtenteCollegato, String from, MessaggioDTO messaggio,
            MailMessagePlaceholderHandler<T> handler, String subject, String[] to, String message) throws DAOException {
        EventoDTO evento = new EventoDTO();
        evento.setCodUserAggiorn(codiceFiscaleUtenteCollegato);
        evento.setCodUserInserim(codiceFiscaleUtenteCollegato);
        evento.setIdTipoEvento(TIPO_EVENTO_MAIL);
        evento.setDEvento(new Date());
        evento.setIdUtente(handler.getIdUtente(parametriInvioMail));
        evento = dao.insert(EventoDBDef.class, evento);

        MessaggioUtenteDTO messaggioUtente = new MessaggioUtenteDTO();
        messaggioUtente.setEvento(evento);
        messaggioUtente.setOggetto(subject);
        messaggioUtente.setTesto(message);
        if (null != handler.getIdSilRifAmbito(parametriInvioMail)) {
            messaggioUtente.setIdSilRifAmbito(handler.getIdSilRifAmbito(parametriInvioMail));
        }
        String emailDestinatario = StringUtils.join(to, ",");
        messaggioUtente.setEmailDestinatario(emailDestinatario);
        messaggioUtente.setEmailMittente(from);
        messaggioUtente.setIdMessaggio(messaggio.getIdMessaggio());
        dao.insert(MessaggioUtenteDBDef.class, messaggioUtente);
        return messaggioUtente;
    }

    public void sendMail(String from, String fromAlias, String[] to, String[] ccn, String subject, String message, MessaggioUtenteDTO messaggioUtente)
            throws Exception {
        EventoDTO evento = messaggioUtente.getEvento();
        StopWatch watcher = new StopWatch(Constants.COMPONENT_NAME);
        watcher.start();
        try {

            getParameters();
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.debug", "false");

            Session mailSession = Session.getInstance(props, null);
            Message msg = new MimeMessage(mailSession);

            // Message FROM
            InternetAddress addressFrom = new InternetAddress(from, fromAlias);
            msg.setFrom(addressFrom);

            // Message TO
            if (!ENVIRONMENT_PROD.equals(mode)) {
                log.warn("[MailUtils::sendMail] <<Modalita' di TEST>>");

                if (to != null && to.length == 1 && to[0] != null && to[0].endsWith("csi.it")) {
                    log.debug("[MailUtils::sendMail] indirizzo utente di tipo csi, la mail verra' inviata a " + to[0]);
                } else {
                    String tos = ParametroUtils.getInstance().getParametro(ParametroUtils.DESTINATARIO_TEST);
                    log.warn("[MailUtils::sendMail] indirizzo utente reale " + Arrays.toString(to) + " verra sostituito con i destinatari di default " + tos);
                    if (tos == null)
                        throw new RuntimeException("Parametro DESTINATARIO_TEST non presente");
                    to = tos.split(",");
                }

            }
            int lengthTo = (to != null) ? to.length : 0;
            Address[] addressesTo = new Address[lengthTo];
            for (int i = 0; i < lengthTo; i++) {
                addressesTo[i] = new InternetAddress(to[i].trim());
            }

            // Message CCN
            int lengthCcn = (ccn != null) ? ccn.length : 0;
            Address[] addressesCcn = new Address[lengthCcn];
            for (int i = 0; i < lengthCcn; i++) {
                addressesCcn[i] = new InternetAddress(ccn[i]);
            }

            msg.setRecipients(Message.RecipientType.TO, addressesTo);
            msg.setRecipients(Message.RecipientType.BCC, addressesCcn);

            // Subject
            msg.setSubject(subject);

            // Message
            MimeBodyPart mbp = new MimeBodyPart();
//            mbp.setText(message + "\n");
            mbp.setContent(message, "text/html; charset=utf-8");

            // Body
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp);

            msg.setContent(mp);
//            msg.setContent(mp, "text/html; charset=utf-8");
            // mailSession.getTransport().send(msg);
            log.info("[MailUtils::sendMail] Sending mail to " + Arrays.toString(to) + " with message " + message);

            if (to != null && to.length > 0) {
                Transport.send(msg);
            } else {
                log.warn("[MailUtils::sendMail] Destinatario mail assente, impossibile inviare mail");
            }

            if (messaggioUtente != null) {
                evento.setCodEsito("OK");
                dao.update(EventoDBDef.class, evento);
                messaggioUtente.setDInvio(new Date());
                dao.update(MessaggioUtenteDBDef.class, messaggioUtente);
            }
        } catch (Exception ex) {
            log.error("[MailUtils::sendMail]", ex);
            try {
                evento.setCodEsito("KO");
                evento.setNote(ex.getClass().getName());
                dao.update(EventoDBDef.class, evento);
            } catch (Throwable ex2) {
                log.error("[MailUtils::sendMail]", ex2);
            }
        } finally {
            watcher.dumpElapsed("MailUtils", "sendMail()", "invocazione servizio [MAIL::sendMail]", "");
            watcher.stop();
        }
    }

    public String[] getParameters() throws Exception {
        if (host == null || port == null || mode == null) {
            synchronized (this) {
                if (host == null || port == null || mode == null) {
                    Properties properties = new Properties();
                    InputStream stream = this.getClass().getResourceAsStream("/wsEndpointUrls.properties");
                    properties.load(stream);
                    host = properties.getProperty("mail.host");
                    port = properties.getProperty("mail.port");
                    mode = properties.getProperty("mail.mode");
                }
            }
        }
        return new String[] { host, port, mode };
    }

}
