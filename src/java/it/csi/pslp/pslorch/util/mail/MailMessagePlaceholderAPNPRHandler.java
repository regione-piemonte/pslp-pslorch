/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

/**
 * Handler per comporre il messaggio della mail da inviare in caso di appuntamento "Non presentato" impostato da SILP
 * 
 * vedi testo messaggio nella tabella PSLP_D_MESSAGGIO con cod_messaggio = 'APNPR'
 * 
 * 
 * E' uguale come placeholder a quello dell'annullo appuntamento
 * 
 * @author 1871
 *
 *
 *
Gentile {0},
visto che non ti sei presentato all''incontro previsto in data {1,date,dd/MM/yyyy}, relativamente alla tua richiesta di adesione alla Garanzia Giovani in Piemonte 
con numero progressivo {2}, per completare la tua adesione e'' necessario che prenoti un nuovo appuntamento presso il tuo Centro per l''Impiego attraverso il servizio {3}. 
Se non sei piu'' interessato, ti consigliamo di annullare l''adesione sul portale Nazionale ANPAL.

{4}

Questo messaggio e'' stato inviato in modalita'' automatica, la preghiamo di non rispondere a questo indirizzo.

Cordiali Saluti

{5}
 *
 *
 */
public class MailMessagePlaceholderAPNPRHandler extends MailMessagePlaceholderAPANNHandler {

	
}
