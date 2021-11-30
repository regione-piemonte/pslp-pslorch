/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.08.22 at 12:20:03 PM CEST 
//


package it.csi.pslp.pslorch.business.integration.dto.sms.info.risposta;

import java.util.ArrayList;
import java.util.List;
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
    "sms"
})
@XmlRootElement(name = "ESITO_CONSULTAZIONE_SMS")
public class ESITOCONSULTAZIONESMS {

    @XmlElement(name = "SMS", required = true)
    protected List<SMS> sms;

    /**
     * Gets the value of the sms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSMS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SMS }
     * 
     * 
     */
    public List<SMS> getSMS() {
        if (sms == null) {
            sms = new ArrayList<SMS>();
        }
        return this.sms;
    }

}
