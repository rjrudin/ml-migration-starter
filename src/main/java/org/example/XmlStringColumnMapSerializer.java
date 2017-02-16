package org.example;

import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;


public class XmlStringColumnMapSerializer implements ColumnMapSerializer {

    @Override
    public String serializeColumnMap(Map<String, Object> columnMap, String rootLocalName) {
        String content = "";
        String rootName = rootLocalName.length() == 0 ? "record" : rootLocalName.replaceAll("[^A-Za-z0-9\\_\\-]", "");
        content = "<" + rootName + ">\n";

        for (Map.Entry<String, Object> entry : transformColumnMap(columnMap).entrySet()) {
            String elName = entry.getKey().replaceAll("[^A-Za-z0-9\\_\\-]", "");
            String value = entry.getValue() == null ? "" : StringEscapeUtils.escapeXml11(entry.getValue().toString());
            content += "<" + elName + ">" + value + "</" + elName + ">\n";
        }

        content += "</" + rootName + ">";
        return content;
    }

    //The strategy is to extend this class and overwrite this method.
    protected Map<String, Object> transformColumnMap(Map<String, Object> columnMap) {
        return columnMap;
    }

}
