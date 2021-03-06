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


package it.csi.pslp.pslorch.business.integration.dto.sms.info.richiesta;

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
    "username",
    "password",
    "codiceprogetto",
    "replydetail",
    "codintscarti",
    "dataaccodamentostart",
    "dataaccodamentoend"
})
@XmlRootElement(name = "CONSULTAZIONE_SMS")
public class CONSULTAZIONESMS {

    @XmlElement(name = "USERNAME", required = true)
    protected String username;
    @XmlElement(name = "PASSWORD", required = true)
    protected String password;
    @XmlElement(name = "CODICE_PROGETTO", required = true)
    protected String codiceprogetto;
    @XmlElement(name = "REPLY_DETAIL", required = true)
    protected String replydetail;
    @XmlElement(name = "COD_INT_SCARTI", required = true)
    protected String codintscarti;
    @XmlElement(name = "DATA_ACCODAMENTO_START", required = true)
    protected String dataaccodamentostart;
    @XmlElement(name = "DATA_ACCODAMENTO_END", required = true)
    protected String dataaccodamentoend;

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUSERNAME() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUSERNAME(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPASSWORD() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPASSWORD(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the codiceprogetto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCODICEPROGETTO() {
        return codiceprogetto;
    }

    /**
     * Sets the value of the codiceprogetto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCODICEPROGETTO(String value) {
        this.codiceprogetto = value;
    }

    /**
     * Gets the value of the replydetail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getREPLYDETAIL() {
        return replydetail;
    }

    /**
     * Sets the value of the replydetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setREPLYDETAIL(String value) {
        this.replydetail = value;
    }

    /**
     * Gets the value of the codintscarti property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCODINTSCARTI() {
        return codintscarti;
    }

    /**
     * Sets the value of the codintscarti property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCODINTSCARTI(String value) {
        this.codintscarti = value;
    }

    /**
     * Gets the value of the dataaccodamentostart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATAACCODAMENTOSTART() {
        return dataaccodamentostart;
    }

    /**
     * Sets the value of the dataaccodamentostart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATAACCODAMENTOSTART(String value) {
        this.dataaccodamentostart = value;
    }

    /**
     * Gets the value of the dataaccodamentoend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDATAACCODAMENTOEND() {
        return dataaccodamentoend;
    }

    /**
     * Sets the value of the dataaccodamentoend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDATAACCODAMENTOEND(String value) {
        this.dataaccodamentoend = value;
    }

}
