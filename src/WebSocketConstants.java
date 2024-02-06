/*
 * written by saharmehrpour
 * This class contains messages sent by websocket.
 * consistent with coreConstants.js webSocketSendMessage and webSocketReceiveMessage
 */

public class WebSocketConstants {

    final static public String MESSAGE_KEY_COMMAND = "command";
    final static public String MESSAGE_KEY_DATA = "data";


    final static public String RECEIVE_MODIFIED_RULE_MSG = "MODIFIED_RULE";
    final static public String RECEIVE_MODIFIED_TAG_MSG = "MODIFIED_TAG";
    final static public String RECEIVE_SNIPPET_XML_MSG = "XML_RESULT";
    final static public String RECEIVE_CODE_TO_XML_MSG = "EXPR_STMT";
    final static public String RECEIVE_NEW_RULE_MSG = "NEW_RULE";
    final static public String RECEIVE_NEW_TAG_MSG = "NEW_TAG";


    final static public String SEND_XML_FILES_MSG = "XML";
    final static public String SEND_RULE_TABLE_MSG = "RULE_TABLE";
    final static public String SEND_TAG_TABLE_MSG = "TAG_TABLE";
    final static public String SEND_PROJECT_HIERARCHY_MSG = "PROJECT_HIERARCHY";
    final static public String SEND_PROJECT_PATH_MSG = "PROJECT_PATH";
    final static public String SEND_VERIFY_RULES_MSG = "VERIFY_RULES";
    final static public String SEND_UPDATE_XML_FILE_MSG = "UPDATE_XML";
    final static public String SEND_CHECK_RULES_FOR_FILE_MSG = "CHECK_RULES_FOR_FILE";
    final static public String SEND_UPDATE_TAG_MSG = "UPDATE_TAG";
    final static public String SEND_FAILED_UPDATE_TAG_MSG = "FAILED_UPDATE_TAG";
    final static public String SEND_UPDATE_RULE_MSG = "UPDATE_RULE";
    final static public String SEND_FAILED_UPDATE_RULE_MSG = "FAILED_UPDATE_RULE";
    final static public String SEND_XML_FROM_CODE_MSG = "EXPR_STMT_XML";
    final static public String SEND_NEW_RULE_MSG = "NEW_RULE";
    final static public String SEND_FAILED_NEW_RULE_MSG = "FAILED_NEW_RULE";
    final static public String SEND_NEW_TAG_MSG = "NEW_TAG";
    final static public String SEND_FAILED_NEW_TAG_MSG = "FAILED_NEW_TAG";
    final static public String SEND_FILE_CHANGE_IN_IDE_MSG = "FILE_CHANGE";

    final static public String SEND_ENTER_CHAT_MSG = "ENTER";
    final static public String SEND_LEFT_CHAT_MSG = "LEFT";
}
