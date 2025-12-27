package usv.client;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import usv.chat.ConversationStore;
import usv.gui.ChatController;
import usv.protocol.ChatProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientMessageHandler {

  private final Agent agent;
  private final String user;

  private AID serverAID;
  private boolean authed;

  private final ConversationStore convs;
  private ChatController ui;

  public ClientMessageHandler(Agent agent, String user, ConversationStore convs) {
    this.agent = agent;
    this.user = user;
    this.convs = convs;
  }

  public void setServerAID(AID serverAID) {
    this.serverAID = serverAID;
  }

  public void setUi(ChatController ui) {
    this.ui = ui;
  }

  public boolean isAuthed() {
    return authed;
  }

  public void setAuthed(boolean authed) {
    this.authed = authed;
  }

  public void handle(ACLMessage msg) {
    ChatProtocol.parse(msg); // ensure content not null
    usv.messages.MsgCodec.Parsed parsed = ChatProtocol.parse(msg);
    String type = parsed.type();

    if (ChatProtocol.AUTH_OK.equals(type)) {
      authed = true;
      if (serverAID != null) {
        agent.send(ChatProtocol.getUsers(serverAID));
      }
      return;
    }

    if (ChatProtocol.AUTH_FAIL.equals(type)) {
      // Let agent handle UI popup + delete if desired
      return;
    }

    if (!authed || ui == null) return;

    if (ChatProtocol.USER_LIST.equals(type)) {
      String nicksStr = parsed.kv().get("nicks");
      List<String> raw = Arrays.asList(nicksStr.split(","));
      List<String> filtered = new ArrayList<String>();

      for (String n : raw) {
        if (n != null) {
          String t = n.trim();
          if (!t.isEmpty() && !t.equals(user)) filtered.add(t);
        }
      }

      ui.setUsers(filtered);
      return;
    }

    if (ChatProtocol.CHAT_DELIVER.equals(type)) {
      String from = parsed.kv().get("from");
      String text = parsed.kv().get("text");
      String line = from + ": " + text;

      convs.addLine(from, line);

      if (from != null && from.equals(convs.getActivePeer())) {
        ui.appendLine(line);
      }
      return;
    }
  }
}
