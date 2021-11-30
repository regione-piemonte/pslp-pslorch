/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
package it.csi.pslp.pslorch.util;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.csi.pslp.pslorch.business.common.DAODataSourceSpring;
import it.csi.pslp.pslorch.business.common.PslDAO;
import it.csi.silos.jedi.core.DAO;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@ComponentScan(basePackages={"it.csi.pslp.pslorch.business", "it.csi.pslp.pslorch.business.portale.impl", "it.csi.pslp.pslorch.business.timer", "it.csi.pslp.pslorch.business.common", "it.csi.pslp.pslorch.util"})
public class SpringConfig {

  @Bean
  public DataSource dataSource() throws IllegalArgumentException, NamingException {
    JndiObjectFactoryBean f = new JndiObjectFactoryBean();
    f.setProxyInterface(DataSource.class);
    f.setJndiName("java:/pslorchDS");
    f.afterPropertiesSet();
    return (DataSource)f.getObject();
  }

  @Bean
  public DAO dao() throws IllegalArgumentException, NamingException {
    DAO dao = new DAO();
    PslDAO.configure(dao);
    DAODataSourceSpring daoDS = new DAODataSourceSpring();
    daoDS.setDataSource((DataSource) dataSource());
    dao.setDataSource(daoDS);
    return dao;
  }
  
  @Bean
  public DataSourceTransactionManager transactionManager() throws IllegalArgumentException, NamingException {
    DataSourceTransactionManager tx = new DataSourceTransactionManager((DataSource)dataSource());
    return tx;
  }
}
