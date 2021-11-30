/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.08.22 at 12:20:02 PM CEST 
//


package it.csi.pslp.pslorch.business.integration.dto.sms.consultazione.risposta;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "codmam",
    "telefono",
    "messaggio",
    "ttl",
    "codifica",
    "dataaccodamento",
    "datarichiestaspedizione",
    "datainviomam",
    "datatrasmissione",
    "dataultimocambiamentostato",
    "statomam",
    "priorita",
    "note",
    "errore"
})
@XmlRootElement(name = "SMS")
public class SMS {

    @XmlElement(name = "COD_MAM", required = true)
    protected String codmam;
    @XmlElement(name = "TELEFONO", required = true)
    protected String telefono;
    @XmlElement(name = "MESSAGGIO", required = true)
    protected String messaggio;
    @XmlElement(name = "TTL", required = true)
    protected String ttl;
    @XmlElement(name = "CODIFICA", required = true)
    protected String codifica;
    @XmlElement(name = "DATA_ACCODAMENTO", required = true)
    protected String dataaccodamento;
    @XmlElement(name = "DATA_RICHIESTA_SPEDIZIONE", required = true)
    protected String datarichiestaspedizione;
    @XmlElement(name = "DATA_INVIO_MAM", required = true)
    protected String datainviomam;
    @XmlElement(name = "DATA_TRASMISSIONE", required = true)
    protected String datatrasmissione;
    @XmlElement(name = "DATA_ULTIMO_CAMBIAMENTO_STATO", required = true)
    protected String dataultimocambiamentostato;
    @XmlElement(name = "STATO_MAM", required = true)
    protected String statomam;
    @XmlElement(name = "PRIORITA", required = true)
    protected String priorita;
    @XmlElement(name = "NOTE", required = true)
    protected String note;
    @XmlElement(name = "ERRORE", required = true)
    protected String errore;

    /**
     * Gets the value of the codmam property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCODMAM() {
        return codmam;
    }

    /**
     * Sets the value of the codmam property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCODMAM(String value) {
        this.codmam = value;
    }

    /**
     * Gets the value of the telefono property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTELEFONO() {
        return telefono;
    }

    /**
     * Sets the value of the telefono property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTELEFONO(String value) {
        this.telefono = value;
    }

    /**
     * Gets the value of the messaggio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMESSAGGIO() {
        return messaggio;
    }

    /**
     * Sets the value of the messaggio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMESSAGGIO(String value) {
        this.messaggio = value;
    }

    /**
     * Gets the value of the ttl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTTL() {
        return ttl;
    }

    /**
     * Sets the value of the ttl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTTL(String value) {
        this.ttl = value;
    }

    /**
     * Gets the value of the codifica property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCODIFICA() {
        return codifica;
    }

    /**
     * Sets the value of the codifica property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCODIFICA(String value) {
        this.codifica = value;
    }

    /**
     * Gets the value of the dataaccodamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATAACCODAMENTO() {
        return dataaccodamento;
    }

    /**
     * Sets the value of the dataaccodamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATAACCODAMENTO(String value) {
        this.dataaccodamento = value;
    }

    /**
     * Gets the value of the datarichiestaspedizione property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATARICHIESTASPEDIZIONE() {
        return datarichiestaspedizione;
    }

    /**
     * Sets the value of the datarichiestaspedizione property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATARICHIESTASPEDIZIONE(String value) {
        this.datarichiestaspedizione = value;
    }

    /**
     * Gets the value of the datainviomam property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATAINVIOMAM() {
        return datainviomam;
    }

    /**
     * Sets the value of the datainviomam property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATAINVIOMAM(String value) {
        this.datainviomam = value;
    }

    /**
     * Gets the value of the datatrasmissione property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATATRASMISSIONE() {
        return datatrasmissione;
    }

    /**
     * Sets the value of the datatrasmissione property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATATRASMISSIONE(String value) {
        this.datatrasmissione = value;
    }

    /**
     * Gets the value of the dataultimocambiamentostato property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATAULTIMOCAMBIAMENTOSTATO() {
        return dataultimocambiamentostato;
    }

    /**
     * Sets the value of the dataultimocambiamentostato property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATAULTIMOCAMBIAMENTOSTATO(String value) {
        this.dataultimocambiamentostato = value;
    }

    /**
     * Gets the value of the statomam property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSTATOMAM() {
        return statomam;
    }

    /**
     * Sets the value of the statomam property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSTATOMAM(String value) {
        this.statomam = value;
    }

    /**
     * Gets the value of the priorita property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPRIORITA() {
        return priorita;
    }

    /**
     * Sets the value of the priorita property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPRIORITA(String value) {
        this.priorita = value;
    }

    /**
     * Gets the value of the note property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNOTE() {
        return note;
    }

    /**
     * Sets the value of the note property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNOTE(String value) {
        this.note = value;
    }

    /**
     * Gets the value of the errore property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getERRORE() {
        return errore;
    }

    /**
     * Sets the value of the errore property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setERRORE(String value) {
        this.errore = value;
    }

}
