/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
package it.csi.pslp.pslorch.business.common;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import it.csi.pslp.pslorch.util.Constants;
import it.csi.silos.jedi.core.DAOException;
import it.csi.silos.silcommon.util.SilTimeUtils;

/**
 * Imposta a runtime le configurazioni in base all'ambiente di esecuzione.
 * 
 */
public class PslorchRuntimeConfig {

    /** The db local property. */
    public static String DB_LOCAL_PROPERTY = "silp.locale.db";

    /** The esecuzione locale db dev. */
    public static String ESECUZIONE_LOCALE_DB_DEV = "svil10";

    /** The esecuzione locale db test. */
    public static String ESECUZIONE_LOCALE_DB_TEST = "test10";

    /** The esecuzione locale db prod. */
    public static String ESECUZIONE_LOCALE_DB_PROD = "prod10";

    /** The Constant NO_COMMIT_MODE. */
    private static final String NO_COMMIT_MODE = "nocommit";

    /** The Constant JUNIT_TEST. */
    private static final String JUNIT_TEST = "junittest";

    /** The Constant log. */
    protected static final Logger log = Logger.getLogger(Constants.COMPONENT_NAME);

    /**
     * Sets the test J unit.
     *
     * @throws SQLException the SQL exception
     * @throws DAOException the DAO exception
     */
    public static void setTestJUnit() throws SQLException, DAOException {
        /**** ATTENZIONE PRIMA DI TOCCARE QUESTO, COINVOLGE LA COMMIT TUTTI I TEST ****/
        // NON COMMENTARE MAI
        // NON COMMENTARE MAI
        // Usare una istruzione tipo SilpsvRuntimeConfig.setForceCommit() nel vostro
        // test specifico se volete committare
        setNoCommitMode();
        /****
         * ATTENZIONE PRIMA DI TOCCARE QUESTO, COINVOLGE LA COMMIT DI TUTTI I TEST
         ****/

        System.setProperty(JUNIT_TEST, "true");

        // Questo gestisce il puntamento di tutti i test di non regressione. Modificarlo
        // se necessario ma i test puntando a TEST dovrebbero funzionare tutti
  //      setLocalExecutionSviluppo();
        setLocalExecutionTest();
        
        if(isLocalExecutionAndrea()) { 
        	setLocalExecutionProd();
        }
    }

    /**
     * Checks if is test J unit.
     *
     * @return true, if is test J unit
     */
    public static boolean isTestJUnit() {
        return System.getProperty(JUNIT_TEST) != null;
    }

    /**
     * Sets the local execution sviluppo.
     */
    public static void setLocalExecutionSviluppo() {
        System.setProperty(DB_LOCAL_PROPERTY, ESECUZIONE_LOCALE_DB_DEV);
    }

    /**
     * Sets the local execution test.
     */
    public static void setLocalExecutionTest() {
        System.setProperty(DB_LOCAL_PROPERTY, ESECUZIONE_LOCALE_DB_TEST);
    }

    /**
     * Sets the local execution coll.
     */
    public static void setLocalExecutionProd() {
        System.setProperty(DB_LOCAL_PROPERTY, ESECUZIONE_LOCALE_DB_PROD);
    }

    /**
     * Checks if is local execution andrea.
     *
     * @return true, if is local execution andrea
     */
    public static boolean isLocalExecutionAndrea() {
        return System.getProperty("user.name").equals("1871");
    }

    /**
     * Checks if is local execution svil.
     *
     * @return true, if is local execution svil
     */
    public static boolean isLocalExecutionSvil() {
        return ESECUZIONE_LOCALE_DB_DEV.equals(System.getProperty(DB_LOCAL_PROPERTY));
    }

    /**
     * Checks if is local execution test.
     *
     * @return true, if is local execution test
     */
    public static boolean isLocalExecutionTest() {
        return ESECUZIONE_LOCALE_DB_TEST.equals(System.getProperty(DB_LOCAL_PROPERTY));
    }

    /**
     * Checks if is local execution prod.
     *
     * @return true, if is local execution prod
     */
    public static boolean isLocalExecutionProd() {
        return ESECUZIONE_LOCALE_DB_PROD.equals(System.getProperty(DB_LOCAL_PROPERTY));
    }

    /**
     * Checks if is local execution.
     *
     * @return true, if is local execution
     */
    public static boolean isLocalExecution() {
        return System.getProperty(DB_LOCAL_PROPERTY) != null;
    }

    /**
     * Checks if is local execution.
     *
     * @param dataDiEsecuzione the data di esecuzione
     * @return true, if is local execution
     */
    public static boolean isLocalExecution(String dataDiEsecuzione) {
        return isLocalExecution() && SilTimeUtils.isData1UgualeData2(SilTimeUtils.convertStringaInData(dataDiEsecuzione), SilTimeUtils.today());
    }

    /**
     * Sets the local execution.
     */
    public static void setLocalExecution() {
        System.setProperty(DB_LOCAL_PROPERTY, "X");
    }

    /**
     * Sets the no commit mode.
     */
    public static void setNoCommitMode() {
        System.setProperty(NO_COMMIT_MODE, "X");
    }

    /**
     * Sets the force commit.
     */
    public static void setForceCommit() {
        System.clearProperty(PslorchRuntimeConfig.NO_COMMIT_MODE);
    }

}
