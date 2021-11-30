/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

public interface MailMessagePlaceholderHandler<T> {

	String replacePlaceholders(String codiceMessaggio, String bodyTemplate, T dataHolder, String signature) throws Exception;
	String[] handleTo(T dataHolder);
	Long getIdUtente(T dataHolder);
	Long getIdSilRifAmbito(T dataHolder);
	boolean isPossibileInviareMail(T dataHolder) throws Exception;
	String getCodiceFiscaleUtente(T dataHolder);
	
	//Fare override se necessario sostituire anche alcuni dati nell'intestazione della mail
	String replacePlaceholdersIntestazione(String intestazione,T dataHolder);
}
