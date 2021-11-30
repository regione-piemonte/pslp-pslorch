/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import it.csi.silos.jedi.core.DAODataSource;
import it.csi.silos.jedi.core.DAOException;

@Component
public class DAODataSourceSpring implements DAODataSource {
  
  @Autowired
  private DataSource dataSource;
  
  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Connection getConnection() throws DAOException {
    return DataSourceUtils.getConnection(dataSource);
  }

  @Override
  public void closeConnection(Connection conn) throws SQLException {
    DataSourceUtils.releaseConnection(conn, dataSource);
  }

}
