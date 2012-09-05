package eu.europeana.corelib.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StringArrayUtils {
    
	public static final String[] EMPTY_ARRAY = new String[0];
	
    public static boolean isNotBlank(String[] array) {
        return ((array != null) && 
                (array.length > 0) && 
                (StringUtils.isNotBlank(array[0])));
    }
    
    public static boolean isBlank(String[] array) {
        return !isNotBlank(array);
    }
    
    public static String[] toArray(List<String> list) {
    	if (list != null) {
    		return list.toArray(new String[list.size()]);
    	}
    	return new String[]{};
    }
    
    public static void addToList(List<String> list, String[] toAdd) {
    	if ( (list != null) && (isNotBlank(toAdd)) ) {
    		for (String string : toAdd) {
				list.add(string);
			}
    	}
    }

    public static String formatList(String[] items) {
		if (isNotBlank(items)) {
			if (items.length == 1) {
				return StringUtils.trim(items[0]);
			}
			StringBuilder sb = new StringBuilder();
			for (String item : items) {
				if (sb.length() > 0) {
					sb.append("| ");
				}
				sb.append(StringUtils.trim(item));
			}
			int p = sb.lastIndexOf("|");
			if (p > 0) {
				sb.replace(p, p + 1, " &");
			}
			return sb.toString().replace("|", ",");
		}
		return "";
	}
    
    public static String[] addToArray(String[] items, String str){
    	if(items==null){
    		items = new String[1];
    	}
    	List<String> itemList = new ArrayList<String>(Arrays.asList(items));
    	itemList.add(str);
    	return itemList.toArray(new String[itemList.size()]);
    	
    }

}
