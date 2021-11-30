/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.common;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.pslp.pslorch.business.SpringApplicationContextHelper;
import it.csi.pslp.pslorch.util.Constants;
import it.csi.pslp.pslorch.util.ParametroUtils;
import it.csi.silos.jedi.core.DAO;
import it.csi.silos.jedi.core.DAODynamicQueryInfo;

@Component("psldao")
public class PslDAO {

  protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

  @Autowired
  private DAO dao;
  
  private PslDAO() {
  }

  public static PslDAO getInstance() {
    return (PslDAO)SpringApplicationContextHelper.getBean("psldao");
  }
  
  public static void configure(DAO dao) {
    log.debug("[PslDAO::configure]");
    dao.setLoggerPrefix(Constants.COMPONENT_NAME);
    dao.setStopWatchThreshold(1000);
    dao.setQueryPath("it/csi/pslp/pslorch/business/");
    DAODynamicQueryInfo dynamicQueryInfo = new DAODynamicQueryInfo();
    dynamicQueryInfo.setTableName("PSLP_D_QUERY_REPLACED");
    dynamicQueryInfo.setQueryNameColumn("COD_QUERY_REPLACED");
    dynamicQueryInfo.setQueryValueColumn("DS_QUERY_REPLACED");
    dynamicQueryInfo.setQueryValueColumnExtension("DS_QUERY_REPLACED_EXT");
    dynamicQueryInfo.setQueryActiveColumn("FLG_ATTIVO");
    dynamicQueryInfo.setQueryTimeoutColumn("NUM_SECONDI_TIMEOUT");
    dao.setDynamicQueryInfo(dynamicQueryInfo);
    dao.setDefaultQueryTimeout(new Integer(3600));
  }
  

  public DAO getDao() {
    return dao;
  }
}
