package com.ben.core.util;

import java.util.*;


/**
 * @author 94391
 */
public class ArraysUtils {

    /**
     * 判断交集
     * 如果都是空则为false
     * @param array1
     * @param array2
     * @return
     */
    public static boolean hasIntersectionByIndexOf(String[] array1, String[] array2) {
        if (Objects.isNull(array1) && Objects.isNull(array2)){
            return false;
        }
        String map = Arrays.toString(array2);
        for (String str : array1) {
            if (map.indexOf(str) > -1) {
                return true;
            }
        }
        return false;
    }


    /**
     * 去重
     * @param a
     * @param b
     * @return
     */
    public static String[] deWeight(String[] a, String[] b) {
        int subscript = 0;
        Map<String, String> map = new TreeMap<>();
        for (int i = 0; i < a.length; i++) {
            map.put(a[i], a[i]);
        }
        for (int i = 0; i < b.length; i++) {
            map.put(b[i], b[i]);
        }
        Collection<String> values = map.values();
        Iterator<String> iterator = values.iterator();
        String res[] = new String[values.size()];
        while (iterator.hasNext()) {
            res[subscript++] = iterator.next();
        }
        return res;
    }

    /**
     * 合并数组
     * @param <T> 泛型，用于合并不同类型数组
     * @param first 同下
     * @param rest 传入的数组，可以传递多个
     * */

    public static <T> T[] concatAll(T[] first, T[]... rest) {

        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }

        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }


}
