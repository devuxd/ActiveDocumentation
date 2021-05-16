import com.google.gson.JsonObject;

class MessageProcessor {

    private static final String[] dataKeys = {
            WebSocketConstants.MESSAGE_KEY_COMMAND,
            WebSocketConstants.MESSAGE_KEY_DATA
    };
    private static final String[] xmlKeys = {"filePath", "xml"};
    private static final String[] textXMLKeys = {"xmlText", "messageID"};

    static JsonObject encodeData(Object[] source_Destination_Protocol_Data_Array) {
        return createJsonObject(source_Destination_Protocol_Data_Array, dataKeys);
    }

    static JsonObject encodeXMLData(Object[] filepath_xml_Array) {
        return createJsonObject(filepath_xml_Array, xmlKeys);
    }

    static JsonObject encodeNewXMLData(Object[] filepath_newXml) {
        return createJsonObject(filepath_newXml, xmlKeys);
    }

    static JsonObject encodeXMLandText(Object[] xml_text) {
        return createJsonObject(xml_text, textXMLKeys);
    }

    private static JsonObject createJsonObject(Object[] data_Array, String[] keys) {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < keys.length; i++) {
            if (data_Array[i] instanceof String) {
                jsonObject.addProperty(keys[i], (String) data_Array[i]);
            } else if (data_Array[i] instanceof JsonObject) {
                jsonObject.add(keys[i], (JsonObject) data_Array[i]);
            }
        }
        return jsonObject;
    }

}
