package com.ben.core.connector.mongodb;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.flink.core.io.InputSplit;

/**
 * @author learn
 * @date 2023/2/10 9:53
 */
@AllArgsConstructor
@Data
public class MongodbInputSplit  implements InputSplit {

    private static final long serialVersionUID = 7803273215947823806L;

    private int skip;

    private int limit;

    @Override
    public int getSplitNumber() {
        return 0;
    }
}
