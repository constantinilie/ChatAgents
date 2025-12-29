package usv.protocol;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import usv.messages.MsgCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatProtocol {

  private ChatProtocol() {}

  public static final String ONTOLOGY = "chat";

  // Message types
  public static final String AUTH = "AUTH";
  public static final String AUTH_OK = "AUTH_OK";
  public static final String AUTH_FAIL = "AUTH_FAIL";
  public static final String GET_USERS = "GET_USERS";
  public static final String USER_LIST = "USER_LIST";
  public static final String SEND_CHAT = "SEND_CHAT";
  public static final String CHAT_DELIVER = "CHAT_DELIVER";
  public static final String UNREGISTER = "UNREGISTER";

  //LLM types
  public static final String LLM_REQUEST = "LLM_REQUEST";
  public static final String LLM_RESPONSE = "LLM_RESPONSE";
  public static final String LLM_FAIL = "LLM_FAIL";

  //LLM conversation id
  public static final String CID_LLM = "llm";
  
  // Conversation IDs
  public static final String CID_AUTH = "auth";
  public static final String CID_GET_USERS = "get-users";
  public static final String CID_SEND_CHAT = "send-chat";
  public static final String CID_CHAT_DELIVER = "chat-deliver";
  public static final String CID_UNREGISTER = "unregister";
  public static final String CID_USER_LIST = "user-list";

  // Build AUTH request
  public static ACLMessage auth(AID server, String op, String user, String pass) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("op", safe(op));
    payload.put("user", safe(user));
    payload.put("pass", pass == null ? "" : pass);

    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(server);
    m.setOntology(ONTOLOGY);
    m.setConversationId(CID_AUTH);
    m.setContent(MsgCodec.pack(AUTH, payload));
    return m;
  }

  // Build GET_USERS request
  public static ACLMessage getUsers(AID server) {
    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(server);
    m.setOntology(ONTOLOGY);
    m.setConversationId(CID_GET_USERS);
    m.setContent(GET_USERS + "|ok=1");
    return m;
  }

  // Build SEND_CHAT request
  public static ACLMessage sendChat(AID server, String to, String text) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("to", safe(to));
    payload.put("text", text == null ? "" : text);

    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(server);
    m.setOntology(ONTOLOGY);
    m.setConversationId(CID_SEND_CHAT);
    m.setContent(MsgCodec.pack(SEND_CHAT, payload));
    return m;
  }

  // Build UNREGISTER request
  public static ACLMessage unregister(AID server, String user) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("user", safe(user));

    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(server);
    m.setOntology(ONTOLOGY);
    m.setConversationId(CID_UNREGISTER);
    m.setContent(MsgCodec.pack(UNREGISTER, payload));
    return m;
  }

  public static boolean isType(ACLMessage msg, String type) {
    if (msg == null) return false;
    MsgCodec.Parsed p = MsgCodec.unpack(msg.getContent());
    return type.equals(p.type());
  }

  public static MsgCodec.Parsed parse(ACLMessage msg) {
    return MsgCodec.unpack(msg == null ? null : msg.getContent());
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
