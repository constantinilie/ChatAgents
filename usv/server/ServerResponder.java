package usv.server;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import usv.messages.MsgCodec;
import usv.protocol.ChatProtocol;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerResponder {

  private final Agent agent;

  public ServerResponder(Agent agent) {
    this.agent = agent;
  }

  public void replyError(ACLMessage msg, String reason) {
    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.FAILURE);
    reply.setContent("ERROR|reason=" + safe(reason));
    agent.send(reply);
  }

  public void replyNotUnderstood(ACLMessage msg) {
    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
    reply.setContent("ERROR|reason=unknown_type");
    agent.send(reply);
  }

  public void replyAuthOk(ACLMessage msg, String user) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("user", safe(user));

    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.INFORM);
    reply.setContent(MsgCodec.pack(ChatProtocol.AUTH_OK, payload));
    agent.send(reply);
  }

  public void replyAuthFail(ACLMessage msg, String reason) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("reason", safe(reason));

    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.FAILURE);
    reply.setContent(MsgCodec.pack(ChatProtocol.AUTH_FAIL, payload));
    agent.send(reply);
  }

  public void replyUserList(ACLMessage msg, String csv) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("nicks", csv == null ? "" : csv);

    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.INFORM);
    reply.setOntology(ChatProtocol.ONTOLOGY);
    reply.setConversationId(ChatProtocol.CID_USER_LIST);
    reply.setContent(MsgCodec.pack(ChatProtocol.USER_LIST, payload));
    agent.send(reply);
  }

  public void broadcastUserList(OnlineRegistry reg) {
    String csv = reg.getOnlineUsersCsv();

    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("nicks", csv);

    for (AID a : reg.allAids()) {
      ACLMessage m = new ACLMessage(ACLMessage.INFORM);
      m.addReceiver(a);
      m.setOntology(ChatProtocol.ONTOLOGY);
      m.setConversationId(ChatProtocol.CID_USER_LIST);
      m.setContent(MsgCodec.pack(ChatProtocol.USER_LIST, payload));
      agent.send(m);
    }
  }

  public void sendChatDeliver(AID dest, String fromUser, String text) {
    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("from", safe(fromUser));
    payload.put("text", text == null ? "" : text);

    ACLMessage deliver = new ACLMessage(ACLMessage.INFORM);
    deliver.addReceiver(dest);
    deliver.setOntology(ChatProtocol.ONTOLOGY);
    deliver.setConversationId(ChatProtocol.CID_CHAT_DELIVER);
    deliver.setContent(MsgCodec.pack(ChatProtocol.CHAT_DELIVER, payload));
    agent.send(deliver);
  }

  public void replySentAck(ACLMessage msg, String to) {
    ACLMessage ack = msg.createReply();
    ack.setPerformative(ACLMessage.INFORM);
    ack.setContent("SENT|to=" + safe(to));
    agent.send(ack);
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
