package usv.server;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Map;

public class ChatService {

  private final OnlineRegistry online;
  private final ServerResponder out;

  public ChatService(OnlineRegistry online, ServerResponder out) {
    this.online = online;
    this.out = out;
  }

  public void handleSendChat(ACLMessage msg, Map<String, String> kv) {
    String to = safe(kv.get("to"));
    String text = kv.get("text");

    String fromUser = online.findUserByAID(msg.getSender());
    if (fromUser == null) {
      out.replyError(msg, "not_authenticated");
      return;
    }

    if (to.isEmpty()) {
      out.replyError(msg, "missing_to");
      return;
    }

    if (fromUser.equals(to)) {
      out.replyError(msg, "self_message_not_allowed");
      return;
    }

    AID dest = online.getAID(to);
    if (dest == null) {
      out.replyError(msg, "unknown_recipient");
      return;
    }

    out.sendChatDeliver(dest, fromUser, text);
    out.replySentAck(msg, to);
  }

  public void handleUnregister(ACLMessage msg, Map<String, String> kv) {
    String user = safe(kv.get("user"));
    if (user.isEmpty()) {
      user = online.findUserByAID(msg.getSender());
    }

    if (user == null || user.trim().isEmpty()) {
      out.replyError(msg, "missing_user");
      return;
    }

    AID current = online.getAID(user);
    if (current == null) {
      out.replyError(msg, "not_online");
      return;
    }

    if (!current.equals(msg.getSender())) {
      out.replyError(msg, "not_owner");
      return;
    }

    online.remove(user);

    ACLMessage reply = msg.createReply();
    reply.setPerformative(ACLMessage.INFORM);
    reply.setContent("UNREGISTERED|user=" + user);
    msg.getSender(); // touch sender to avoid warnings
    out.broadcastUserList(online);
  }

  public void handleGetUsers(ACLMessage msg) {
    out.replyUserList(msg, online.getOnlineUsersCsv());
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
