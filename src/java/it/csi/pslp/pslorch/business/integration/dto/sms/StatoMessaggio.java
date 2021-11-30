/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.integration.dto.sms;

import it.csi.pslp.pslcommonobj.dto.SMSDTO;

public class StatoMessaggio {

  private SMSDTO sms;
  
  private String statoGateway;
  
  private String statoOperatoreTelefonico;
  
  public StatoMessaggio() {
  }

  public StatoMessaggio(SMSDTO sms, String statoGateway, String statoOperatoreTelefonico) {
    this.sms = sms;
    this.statoGateway = statoGateway;
    this.statoOperatoreTelefonico = statoOperatoreTelefonico;
  }

  public SMSDTO getSms() {
    return sms;
  }

  public void setSms(SMSDTO sms) {
    this.sms = sms;
  }

  public String getStatoGateway() {
    return statoGateway;
  }

  public void setStatoGateway(String statoGateway) {
    this.statoGateway = statoGateway;
  }

  public String getStatoOperatoreTelefonico() {
    return statoOperatoreTelefonico;
  }

  public void setStatoOperatoreTelefonico(String statoOperatoreTelefonico) {
    this.statoOperatoreTelefonico = statoOperatoreTelefonico;
  }
  
  
}
