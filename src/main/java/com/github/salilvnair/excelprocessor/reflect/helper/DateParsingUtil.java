package com.github.salilvnair.excelprocessor.reflect.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DateParsingUtil {
	 public enum DateFormats {
	        DASH_YY_M_D("yy-M-d"),
	        DASH_D_M_YY("d-M-yy"),
	        DASH_YY_MM_DD("yy-MM-dd"),
	        DASH_DD_MM_YY("dd-MM-yy"),
	        DASH_YY_MMM_DD("yy-MMM-dd"), 
	        DASH_DD_MMM_YY("dd-MMM-yy"),
	        DASH_YYYY_MM_DD("yyyy-MM-dd"), 
	        DASH_DD_MM_YYYY("dd-MM-yyyy"),
	        DASH_YYYY_MMM_DD("yyyy-MMM-dd"), 
	        DASH_DD_MMM_YYYY("dd-MMM-yyyy"),
	        DASH_YY_D_M("yy-d-M"),
	        DASH_M_D_YY("d-M-yy"),
	        DASH_YY_DD_MM("yy-dd-MM"),
	        DASH_MM_DD_YY("MM-dd-yy"),
	        DASH_YY_DD_MMM("yy-dd-MM"), 
	        DASH_MMM_DD_YY("MMM-dd-yy"),
	        DASH_YYYY_DD_MM("yyyy-dd-MM"), 
	        DASH_MM_DD_YYYY("MM-dd-yyyy"),
	        DASH_YYYY_DD_MMM("yyyy-dd-MMM"), 
	        DASH_MMM_DD_YYYY("MMM-dd-yyyy"),
	        
	        DOT_YY_M_D("yy.M.d"),
	        DOT_D_M_YY("d.M.yy"),
	        DOT_YY_MM_DD("yy.MM.dd"),
	        DOT_DD_MM_YY("dd.MM.yy"),
	        DOT_YY_MMM_DD("yy.MMM.dd"), 
	        DOT_DD_MMM_YY("dd.MMM.yy"),
	        DOT_YYYY_MM_DD("yyyy.MM.dd"), 
	        DOT_DD_MM_YYYY("dd.MM.yyyy"),
	        DOT_YYYY_MMM_DD("yyyy.MMM.dd"), 
	        DOT_DD_MMM_YYYY("dd.MMM.yyyy"),
	        DOT_YY_D_M("yy.d.M"),
	        DOT_M_D_YY("d.M.yy"),
	        DOT_YY_DD_MM("yy.dd.MM"),
	        DOT_MM_DD_YY("MM.dd.yy"),
	        DOT_YY_DD_MMM("yy.dd.MM"), 
	        DOT_MMM_DD_YY("MMM.dd.yy"),
	        DOT_YYYY_DD_MM("yyyy.dd.MM"), 
	        DOT_MM_DD_YYYY("MM.dd.yyyy"),
	        DOT_YYYY_DD_MMM("yyyy.dd.MMM"), 
	        DOT_MMM_DD_YYYY("MMM.dd.yyyy"),
	        
	        
	        SLASH_YY_M_D("yy/M/d"),
	        SLASH_D_M_YY("d/M/yy"),
	        SLASH_YY_MM_DD("yy/MM/dd"),
	        SLASH_DD_MM_YY("dd/MM/yy"),
	        SLASH_YY_MMM_DD("yy/MMM/dd"), 
	        SLASH_DD_MMM_YY("dd/MMM/yy"),
	        SLASH_YYYY_MM_DD("yyyy/MM/dd"), 
	        SLASH_DD_MM_YYYY("dd/MM/yyyy"),
	        SLASH_YYYY_MMM_DD("yyyy/MMM/dd"), 
	        SLASH_DD_MMM_YYYY("dd/MMM/yyyy"),
	        SLASH_YY_D_M("yy/d/M"),
	        SLASH_M_D_YY("d/M/yy"),
	        SLASH_YY_DD_MM("yy/dd/MM"),
	        SLASH_MM_DD_YY("MM/dd/yy"),
	        SLASH_YY_DD_MMM("yy/dd/MM"), 
	        SLASH_MMM_DD_YY("MMM/dd/yy"),
	        SLASH_YYYY_DD_MM("yyyy/dd/MM"), 
	        SLASH_SLASH_MM_DD_YYYY("MM/dd/yyyy"),
	        SLASH_YYYY_DD_MMM("yyyy/dd/MMM"), 
	        SLASH_MMM_DD_YYYY("MMM/dd/yyyy");
	        
	        private String dateFormat;

	        DateFormats(String dateFormat) {this.dateFormat = dateFormat;}

	        public String value() {
	            return dateFormat;
	        }
	       
	    }
	 
	 	public enum DateTimeFormats {   
	 		
	        DASH_YY_MM_DD_HH_MMA("yy-MMM-dd, hh:mma"), 
	        DASH_DD_MMM_YY_HH_MMA("dd-MMM-yy, hh:mma"),	   
	        DASH_YY_MMM_DD_HH_MMA("yy-MMM-dd, hh:mma"), 
	        DASH_YYYY_MM_DD_HH_MMA("yyyy-MM-dd, hh:mma"), 
	        DASH_DD_MM_YYYY_HH_MMA("dd-MM-yyyy, hh:mma"),
	        DASH_YYYY_MMM_DD_HH_MMA("yyyy-MMM-dd, hh:mma"), 
	        DASH_DD_MMM_YYYY_HH_MMA("dd-MMM-yyyy, hh:mma"),
	        DASH_YY_MMM_DD_HH_MM_SSA("yy-MMM-dd, hh:mm:ssa"), 
	        DASH_DD_MMM_YY_HH_MM_SSA("dd-MMM-yy, hh:mm:ssa"),
	        DASH_YYYY_MM_DD_HH_MM_SSA("yyyy-MM-dd, hh:mm:ssa"), 
	        DASH_DD_MM_YYYY_HHMM_SSA("dd-MM-yyyy, hh:mm:ssa"),
	        DASH_YYYY_MMM_DD_HH_MM_SSA("yyyy-MMM-dd, hh:mm:ssa"), 
	        DASH_DD_MMM_YYYY_HH_MM_SSA("dd-MMM-yyyy, hh:mm:ssa"),
	        
	        SLASH_YY_MM_DD_HH_MMA("yy/MM/dd, hh:mma"), 
	        SLASH_DD_MM_YY_HH_MMA("dd/MM/yy, hh:mma"),
	        SLASH_YY_MMM_DD_HH_MMA("yy/MMM/dd, hh:mma"), 
	        SLASH_DD_MMM_YY_HH_MMA("dd/MMM/yy, hh:mma"),
	        SLASH_YYYY_MM_DD_HH_MMA("yyyy/MM/dd, hh:mma"), 
	        SLASH_DD_MM_YYYY_HH_MMA("dd/MM/yyyy, hh:mma"),
	        SLASH_YYYY_MMM_DD_HH_MMA("yyyy/MMM/dd, hh:mma"), 
	        SLASH_DD_MMM_YYYY_HH_MMA("dd/MMM/yyyy, hh:mma"),
	        SLASH_YY_MM_DD_HH_MM_SSA("yy/MM/dd, hh:mm:ssa"), 
	        SLASH_DD_MM_YY_HH_MM_SSA("dd/MM/yy, hh:mm:ssa"),
	        SLASH_YY_MMM_DD_HH_MM_SSA("yy/MMM/dd, hh:mm:ssa"), 
	        SLASH_DD_MMM_YY_HH_MM_SSA("dd/MMM/yy, hh:mm:ssa"),
	        SLASH_YYYY_MM_DD_HH_MM_SSA("yyyy/MM/dd, hh:mm:ssa"), 
	        SLASH_DD_MM_YYYY_HH_MM_SSA("dd/MM/yyyy, hh:mm:ssa"),
	        SLASH_YYYY_MMM_DD_HH_MM_SSA("yyyy/MMM/dd, hh:mm:ssa"), 
	        SLASH_DD_MMM_YYYY_HH_MM_SSA("dd/MMM/yyyy, hh:mm:ssa");
	        
	        private String dateTimeFormat;

	        DateTimeFormats(String dateTimeFormat) {this.dateTimeFormat = dateTimeFormat;}

	        public String getDateTimeFormat() {
	            return dateTimeFormat;
	        }
	    
	 	}


	    public static long getDateOnly(String date) {
	        SimpleDateFormat sample = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	        try {
	            return sample.parse(date).getTime();
	        } 
	        catch (Exception e) {

	        }
	        return 0;
	    }

	    public static String getDateOnly(long time) {
	        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(time);
	    }

	    public static String getDateAndTime(long time) {
	        SimpleDateFormat sample = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault());
	        return sample.format(new Date(time));
	    }

	    public static String getDateAndTime(String time) {
	        SimpleDateFormat sample = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault());
	        return sample.format(time);
	    }

	    public static String getTimeOnly(long time) {
	        SimpleDateFormat sample = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	        return sample.format(time);
	    }

	    public static String getTodayWithTime() {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
	        return dateFormat.format(new Date());
	    }

	    public static String getToday() {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
	        return dateFormat.format(new Date());
	    }

	    public static String getYesterday() {
	        try {
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTime(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(getToday()));
	            calendar.add(Calendar.DATE, -1);
	            Date tomorrow = calendar.getTime();
	            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tomorrow);
	        } 
	        catch (Exception e) {
	            
	        }
	        return null;
	    }

	    public static String getTomorrow() {
	        try {
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTime(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(getToday()));
	            calendar.add(Calendar.DATE, 1);
	            Date tomorrow = calendar.getTime();
	            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tomorrow);
	        } 
	        catch (Exception e) {
	        	
	        }
	        return null;
	    }

	    
	    public static long getDaysBetweenTwoDate(String old, String newDate, DateFormats dateFormats) {
	        SimpleDateFormat myFormat = new SimpleDateFormat(dateFormats.value(), Locale.getDefault());
	        try {
	            Date date1 = myFormat.parse(old);
	            Date date2 = myFormat.parse(newDate);
	            long diff = date1.getTime() - date2.getTime();
	            long seconds = diff / 1000;
	            long minutes = seconds / 60;
	            long hours = minutes / 60;
	            long days = hours / 24;
	            return days;
	        } 
	        catch (Exception e) {
	        	
	        }
	        return 0;
	    }

	    public static long getHoursBetweenTwoDate(String old, String newDate, DateFormats dateFormats) {
	        SimpleDateFormat myFormat = new SimpleDateFormat(dateFormats.value(), Locale.getDefault());
	        try {
	            Date date1 = myFormat.parse(old);
	            Date date2 = myFormat.parse(newDate);
	            long diff = date1.getTime() - date2.getTime();
	            long seconds = diff / 1000;
	            long minutes = seconds / 60;
	            long hours = minutes / 60;
	            return hours;
	        } 
	        catch (Exception e) {
	        	
	        }
	        return 0;
	    }

	    public static long getMinutesBetweenTwoDates(String old, String newDate, DateFormats dateFormats) {
	        SimpleDateFormat myFormat = new SimpleDateFormat(dateFormats.value(), Locale.getDefault());
	        try {
	            Date date1 = myFormat.parse(old);
	            Date date2 = myFormat.parse(newDate);
	            long diff = date1.getTime() - date2.getTime();
	            long seconds = diff / 1000;
	            long minutes = seconds / 60;
	            return minutes;
	        } 
	        catch (Exception e) {
	        	
	        }
	        return 0;
	    }

	    public static long getMinutesBetweenTwoDates(long old, long newDate) {
	        long diff = old - newDate;
	        long seconds = diff / 1000;
	        long minutes = seconds / 60;
	        return minutes;
	    }

	    public static boolean isInFuture(long timestamp) {
	        Date date = new Date(timestamp);
	        return date.getTime() - new Date().getTime() >= 0;
	    }
	    
	    public static int compareDate(Date date1, Date date2) {
	    	if(date1.getTime()==date2.getTime()) {
	    		return 0;
	    	}
	    	else if(date1.getTime()>date2.getTime()) {
	    		return 1;
	    	}
	    	else {
	    		return -1;
	    	}
	    	
	    }

	    public static Date parseAnyDate(String dateString) {
	    	Date date = null;
	        for (DateFormats formats : DateFormats.values()) {
	            try {
	                SimpleDateFormat format = new SimpleDateFormat(formats.value(), Locale.getDefault());
	                date = format.parse(dateString);
	            } 
	            catch (Exception e) {
	            	
	            }
	        }
	        return date;
	    }
	    
	    public static boolean isDate(String date) {
	        for (DateFormats formats : DateFormats.values()) {
	            try {
	                SimpleDateFormat format = new SimpleDateFormat(formats.value(), Locale.getDefault());
	                format.parse(date).getTime();
	            } 
	            catch (Exception ex) {
	            	return false;
	            }
	        }
	        return true;
	    }
	    
	    public static boolean isDate(String dateString, String dateFormat) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());
                Date formattedDate = format.parse(dateString);
                String parsedDate = format.format(formattedDate);
                if(!dateString.equals(parsedDate)) {
                	return false;
                }
            } 
            catch (Exception ex) {
            	return false;
            }
	        return true;
	    }
	    
	    public static String getDateFormat(String dateString, String[] dateFormats) {
	    	for(String dateFormat:dateFormats) {
		    	try {
	                SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());
	                Date formattedDate = format.parse(dateString);
	                String parsedDate = format.format(formattedDate);
	                if(dateString.equals(parsedDate)) {
	                	return dateFormat;
	                }
	            } 
	            catch (Exception ex) {
	            	return null;
	            }
	    	}
	        return null;
	    }
	    
	    public static boolean isDate(String date, List<String> dateFormats) {
	    	boolean isFormattedDate = false;
           for(String dateFormat:dateFormats) {
        	   isFormattedDate = isDate(date, dateFormat);
           }
           return isFormattedDate;
	    }
	    
	    public static boolean isDate(String date, String[] dateFormats) {
	    	boolean isFormattedDate = false;
           for(String dateFormat:dateFormats) {
        	   isFormattedDate = isDate(date, dateFormat);
        	   if(isFormattedDate) {
        		   return isFormattedDate;
        	   }
           }
           return isFormattedDate;
	    }

	    public static Date parseDate(String dateString, String dateFormat) {
	        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.getDefault());
	        try {
	            return format.parse(dateString);
	        } 
	        catch (Exception e) {
	        	
	        }
	        return null;
	    }
	    
	    public static Date parseDate(String dateString, String[] dateFormats) {
	    	String dateFormat = getDateFormat(dateString, dateFormats);
	    	return parseDate(dateString, dateFormat);
	    }
	    
	    public static Date parseDate(String dateString, DateFormats dateFormats) {
	        SimpleDateFormat format = new SimpleDateFormat(dateFormats.value(), Locale.getDefault());
	        try {
	            return format.parse(dateString);
	        } 
	        catch (Exception e) {
	        	
	        }
	        return null;
	    }

	    public static String getDate(String date, DateFormats orginalFormat, DateFormats newFormat) {
	        SimpleDateFormat sample = new SimpleDateFormat(orginalFormat.value(), Locale.getDefault());
	        try {
	            long time = sample.parse(date).getTime();
	            sample = new SimpleDateFormat(newFormat.value(), Locale.getDefault());
	            return sample.format(time);
	        } 
	        catch (Exception e) {
	        	
	        }
	        return date;
	    }

	    public static String getDesiredFormat(DateFormats formats) {
	        SimpleDateFormat format = new SimpleDateFormat(formats.value(), Locale.getDefault());
	        return format.format(new Date());
	    }

	    public static String getDesiredFormat(DateFormats formats, long date) {
	        if (date == 0) return "";
	        SimpleDateFormat format = new SimpleDateFormat(formats.value(), Locale.getDefault());
	        return format.format(date);
	    }
}
