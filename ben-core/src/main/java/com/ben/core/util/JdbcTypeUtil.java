package com.ben.core.util;

import org.apache.flink.api.common.typeinfo.*;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author learn
 * @date 2023/5/9 10:58
 */
public class JdbcTypeUtil {
    private static final Map<TypeInformation<?>, Integer> TYPE_MAPPING;

    private JdbcTypeUtil() {
    }

    public static int typeInformationToSqlType(TypeInformation<?> type) {
        if (TYPE_MAPPING.containsKey(type)) {
            return (Integer)TYPE_MAPPING.get(type);
        } else if (!(type instanceof ObjectArrayTypeInfo) && !(type instanceof PrimitiveArrayTypeInfo)) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        } else {
            return 2003;
        }
    }

    static {
        HashMap<TypeInformation<?>, Integer> m = new HashMap();
        m.put(BasicTypeInfo.STRING_TYPE_INFO, 12);
        m.put(BasicTypeInfo.BOOLEAN_TYPE_INFO, 16);
        m.put(BasicTypeInfo.BYTE_TYPE_INFO, -6);
        m.put(BasicTypeInfo.SHORT_TYPE_INFO, 5);
        m.put(BasicTypeInfo.INT_TYPE_INFO, 4);
        m.put(BasicTypeInfo.LONG_TYPE_INFO, -5);
        m.put(BasicTypeInfo.FLOAT_TYPE_INFO, 7);
        m.put(BasicTypeInfo.DOUBLE_TYPE_INFO, 8);
        m.put(BasicTypeInfo.DATE_TYPE_INFO, 91);
        m.put(SqlTimeTypeInfo.DATE, 91);
        m.put(SqlTimeTypeInfo.TIME, 92);
        m.put(SqlTimeTypeInfo.TIMESTAMP, 93);
        m.put(LocalTimeTypeInfo.LOCAL_DATE, 91);
        m.put(LocalTimeTypeInfo.LOCAL_TIME, 92);
        m.put(LocalTimeTypeInfo.LOCAL_DATE_TIME, 93);
        m.put(BasicTypeInfo.BIG_DEC_TYPE_INFO, 3);
        m.put(PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO, -2);
        TYPE_MAPPING = Collections.unmodifiableMap(m);
    }
}
