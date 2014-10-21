package sightlytck.scripts.exprlang.filters;

import java.util.LinkedHashMap;
import java.util.Map;

public class FiltersPojo {

    private Map<String, String> collection = new LinkedHashMap<String, String>();

    public FiltersPojo() {
        collection.put("a", "1");
        collection.put("b", "2");
        collection.put("c", "3");
    }

    public Map<String, String> collection() {
        return collection;
    }


}
