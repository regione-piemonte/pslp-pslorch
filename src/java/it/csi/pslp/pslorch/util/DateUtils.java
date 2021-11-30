/********************************************************
 * Copyright Regione Piemonte - 2021					*
 * SPDX-License-Identifier: EUPL-1.2-or-later			*
 ********************************************************/
 
package it.csi.pslp.pslorch.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.util.StringUtils;

public class DateUtils {
	public static Date getNoonOfDay(Date day) {
		  Calendar cal = GregorianCalendar.getInstance();
	      if (day == null) day = new Date();
	      cal.setTime(day);
	      cal.set(Calendar.HOUR_OF_DAY, 12);
	      cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
	      cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
	      cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
	      return cal.getTime();
	  }
	
	
	
	public static Date convertDate(String dataRiferimento) throws ParseException {
		if (StringUtils.hasLength(dataRiferimento))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setLenient(false);
			return sdf.parse(dataRiferimento);
		}
		else return null;
	}
	
	public static String formatDateTimeForMinistero(Date data)  {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return sdf.format(data);
	}
	public static Date convertDateTimeFromMinistero(String dataRiferimento) throws ParseException {
		if (dataRiferimento == null) {
			System.out.println("dataRiferimento nul?");
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return sdf.parse(dataRiferimento);
	}

	public static Date convertDateTimeForMinistero(String dataRiferimento) throws ParseException {
		if (dataRiferimento == null) {
			System.out.println("dataRiferimento nul?");
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return sdf.parse(dataRiferimento);
	}

	public static String formatDateTimeFromMinistero(Date dataRiferimento) throws ParseException {
		if (dataRiferimento == null) {
			System.out.println("dataRiferimento nul?");
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return sdf.format(dataRiferimento);
	}
	
	public static Date addExpiresToNow(int expiresIn)
	{
		Calendar cal = Calendar.getInstance(); // creates calendar
	    cal.setTime(new Date()); // sets calendar time/date
	    cal.add(Calendar.SECOND, expiresIn); // adds one hour
	    return cal.getTime();
	}

	
	public static XMLGregorianCalendar toXmlGregorianCalendar(Date d) throws DatatypeConfigurationException {
		if(d!=null) {
			GregorianCalendar gcalendar = new GregorianCalendar();
			gcalendar.setTime(d);
			XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
			return  xmlDate;
		}
		return null;
	}
	
}
