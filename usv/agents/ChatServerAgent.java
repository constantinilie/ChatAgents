package usv.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import usv.jade.DfUtil;
import usv.messages.MsgCodec;
import usv.server.AuthService;
import usv.server.ChatService;
import usv.server.OnlineRegistry;
import usv.server.ServerResponder;
import usv.storage.UserStore;

import java.io.File;

public class ChatServerAgent extends Agent {
  private static final long serialVersionUID = 1L;

  private UserStore userStore;
  private OnlineRegistry online;
  private ServerResponder out;
  private AuthService auth;
  private ChatService chat;

  @Override
  protected void setup() {
    userStore = new UserStore(new File("users.txt"));
    userStore.load();

    online = new OnlineRegistry();
    out = new ServerResponder(this);
    auth = new AuthService(userStore, online, out);
    chat = new ChatService(online, out);

    DfUtil.register(this, "chat-server", "Chat Server");

    addBehaviour(new CyclicBehaviour() {
      private static final long serialVersionUID = 1L;

      @Override
      public void action() {
        ACLMessage msg = receive();
        if (msg == null) { block(); return; }

        MsgCodec.Parsed parsed = MsgCodec.unpack(msg.getContent());
        String type = parsed.type();

        if ("PING".equals(type)) {
          ACLMessage reply = msg.createReply();
          reply.setPerformative(ACLMessage.INFORM);
          reply.setContent("PONG|ok=1");
          send(reply);
          return;
        }

        if ("AUTH".equals(type)) {
          auth.handleAuth(msg, parsed.kv());
          return;
        }

        if ("SEND_CHAT".equals(type)) {
          chat.handleSendChat(msg, parsed.kv());
          return;
        }

        if ("UNREGISTER".equals(type)) {
          chat.handleUnregister(msg, parsed.kv());
          return;
        }

        if ("GET_USERS".equals(type)) {
          chat.handleGetUsers(msg);
          return;
        }

        out.replyNotUnderstood(msg);
      }
    });

    System.out.println(getLocalName() + " started.");
  }

  @Override
  protected void takeDown() {
    DfUtil.deregister(this);
    System.out.println(getLocalName() + " stopped.");
  }
}
