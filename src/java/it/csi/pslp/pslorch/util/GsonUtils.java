package it.csi.pslp.pslorch.util;

import com.google.gson.GsonBuilder;
/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/ 
 
public class GsonUtils {
  
  /**
   * Converte una stringa Gson in una istanza di un oggetto della classe specificata in input
   * @param gsonString
   * @param objectClass
   * @return
   */
  public static <T> T toGsonObject(String gsonString,Class<T> objectClass) {
    GsonBuilder gb = getDefaultGsonBuilder();
    return gb.create().fromJson(gsonString,objectClass);
  }
  
  /**
   * Converte un oggetto in una sua rappresentazione in stringa Gson
   * @param o
   * @return
   */
  public static String toGsonString(Object o) {
    GsonBuilder gb = getDefaultGsonBuilder();
    return gb.create().toJson(o);
  }


  /**
   * Regole di base per le conversione da e per stringa gson
   * @return
   */
  protected static GsonBuilder getDefaultGsonBuilder() {
    GsonBuilder gb = new GsonBuilder();
    gb.setPrettyPrinting();
    //gb.setDateFormat("dd/MM/yyyy HH:mm:ss SSS"); //selezionare il formato voluto
    gb.setDateFormat("dd/MM/yyyy HH:mm:ss"); //selezionare il formato voluto
    //gb.serializeNulls(); //per default  non stampa i campi nulli, usare questa istruzione 
    return gb;
  }
  
  /*
   * Converte un oggetto sourceObj in rappresentazione gson e la ripristina sull'oggetto destObject.
   * In pratica esegue un remapping dei campi uguali su oggetti diversi
   */
  public static <T> T remapObject(Object sourceObj,Class<T> destObjClass){
    return toGsonObject(toGsonString(sourceObj),destObjClass);
  }

}
