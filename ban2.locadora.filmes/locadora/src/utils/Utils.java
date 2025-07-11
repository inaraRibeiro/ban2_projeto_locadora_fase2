package utils;

import org.bson.Document;

public class Utils {

    public static long getDocId(Document doc, String key) {
        try {
            return doc.getInteger(key).longValue();
        } catch (Exception e) {
            return doc.getLong(key);
        }
    }

    public static int getIntegerDoc(Document doc, String key, int defaultValue) {
        try {
            if(doc.get(key) instanceof Double) {
                var doubleNumber = doc.getDouble(key);
                return doubleNumber == null || doubleNumber < 0 ? defaultValue : doubleNumber.intValue();
            }
            Integer value = doc.getLong(key).intValue();
            return value < 0 ? defaultValue : value;
        } catch (Exception e) {
            Integer value =  doc.getInteger(key);
            return value == null || value < 0 ? defaultValue : value;
        }
    }

}
