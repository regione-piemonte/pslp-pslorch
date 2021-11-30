/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util.mail;

import java.io.Serializable;
import java.util.Date;

/**
 * Contiene i parametri per sostituire i placeholder relativi alle mail legate
 * alle adesioni GG. Non riesco a usare altri oggetti delle common perche' qui
 * servono anche informazioni provenienti sia da adesioni, sia dalla
 * prenotazione
 * 
 * @author CSI
 *
 */
public class ParametriMailGG implements Serializable {

  private static final long serialVersionUID = 1L;

  private String nomeCognomeCittadino;

  private String codiceFiscaleCittadino;

  private Date dataStatoAdesione;

  private Long idAdesione;

  private String codStatoAdesione;

  private String descrizioneStatoAdesione;

  private String mail;

  public String getNomeCognomeCittadino() {
    return nomeCognomeCittadino;
  }

  public void setNomeCognomeCittadino(String nomeCognomeCittadino) {
    this.nomeCognomeCittadino = nomeCognomeCittadino;
  }

  public String getCodiceFiscaleCittadino() {
    return codiceFiscaleCittadino;
  }

  public void setCodiceFiscaleCittadino(String codiceFiscaleCittadino) {
    this.codiceFiscaleCittadino = codiceFiscaleCittadino;
  }

  public Date getDataStatoAdesione() {
    return dataStatoAdesione;
  }

  public void setDataStatoAdesione(Date dataStatoAdesione) {
    this.dataStatoAdesione = dataStatoAdesione;
  }

  public Long getIdAdesione() {
    return idAdesione;
  }

  public void setIdAdesione(Long idAdesione) {
    this.idAdesione = idAdesione;
  }

  public String getCodStatoAdesione() {
    return codStatoAdesione;
  }

  public void setCodStatoAdesione(String codStatoAdesione) {
    this.codStatoAdesione = codStatoAdesione;
  }

  public String getDescrizioneStatoAdesione() {
    return descrizioneStatoAdesione;
  }

  public void setDescrizioneStatoAdesione(String descrizioneStatoAdesione) {
    this.descrizioneStatoAdesione = descrizioneStatoAdesione;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ParametriMailGG [");
    if (nomeCognomeCittadino != null) {
      builder.append("nomeCognomeCittadino=");
      builder.append(nomeCognomeCittadino);
      builder.append(", ");
    }
    if (codiceFiscaleCittadino != null) {
      builder.append("codiceFiscaleCittadino=");
      builder.append(codiceFiscaleCittadino);
      builder.append(", ");
    }
    if (dataStatoAdesione != null) {
      builder.append("dataStatoAdesione=");
      builder.append(dataStatoAdesione);
      builder.append(", ");
    }
    if (idAdesione != null) {
      builder.append("idAdesione=");
      builder.append(idAdesione);
      builder.append(", ");
    }
    if (codStatoAdesione != null) {
      builder.append("codStatoAdesione=");
      builder.append(codStatoAdesione);
      builder.append(", ");
    }
    if (descrizioneStatoAdesione != null) {
      builder.append("descrizioneStatoAdesione=");
      builder.append(descrizioneStatoAdesione);
      builder.append(", ");
    }
    if (mail != null) {
      builder.append("mail=");
      builder.append(mail);
    }
    builder.append("]");
    return builder.toString();
  }

  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
  }

}
