package usv.protocol;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import usv.messages.MsgCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LlmProtocol {
  private LlmProtocol() {}

  public static ACLMessage request(AID llmAgent, String rid, String op, String text, String targetLang) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("rid", safe(rid));
    payload.put("op", safe(op));
    payload.put("text", text == null ? "" : text);
    if (targetLang != null && !targetLang.trim().isEmpty()) {
      payload.put("target_lang", targetLang.trim());
    }

    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(llmAgent);
    m.setOntology(ChatProtocol.ONTOLOGY);
    m.setConversationId(ChatProtocol.CID_LLM);
    m.setContent(MsgCodec.pack(ChatProtocol.LLM_REQUEST, payload));
    return m;
  }

  public static ACLMessage response(ACLMessage requestMsg, String rid, String result) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("rid", safe(rid));
    payload.put("result", result == null ? "" : result);

    ACLMessage reply = requestMsg.createReply();
    reply.setPerformative(ACLMessage.INFORM);
    reply.setOntology(ChatProtocol.ONTOLOGY);
    reply.setConversationId(ChatProtocol.CID_LLM);
    reply.setContent(MsgCodec.pack(ChatProtocol.LLM_RESPONSE, payload));
    return reply;
  }

  public static ACLMessage fail(ACLMessage requestMsg, String rid, String reason) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("rid", safe(rid));
    payload.put("reason", reason == null ? "unknown" : reason);

    ACLMessage reply = requestMsg.createReply();
    reply.setPerformative(ACLMessage.FAILURE);
    reply.setOntology(ChatProtocol.ONTOLOGY);
    reply.setConversationId(ChatProtocol.CID_LLM);
    reply.setContent(MsgCodec.pack(ChatProtocol.LLM_FAIL, payload));
    return reply;
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
