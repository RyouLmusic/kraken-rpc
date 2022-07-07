package org.kraken.core.common.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/19 21:53
 */
public class CollectionUtils {

    private static final Comparator<String> SIMPLE_NAME_COMPARATOR = (s1, s2) -> {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        int i1 = s1.lastIndexOf('.');
        if (i1 >= 0) {
            s1 = s1.substring(i1 + 1);
        }
        int i2 = s2.lastIndexOf('.');
        if (i2 >= 0) {
            s2 = s2.substring(i2 + 1);
        }
        return s1.compareToIgnoreCase(s2);
    };

    private CollectionUtils() {
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }


    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static void mapRemoveAll(Map<?, ?> map) {
        map.forEach(map::remove);
    }

}
