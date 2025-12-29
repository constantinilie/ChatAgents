package usv.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import usv.chat.ConversationStore;
import usv.client.ClientMessageHandler;
import usv.gui.ChatCallbacks;
import usv.gui.ChatController;
import usv.gui.ChatWindow;
import usv.jade.DfUtil;
import usv.protocol.ChatProtocol;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.Optional;


public class ChatClientAgent extends Agent {
  private static final long serialVersionUID = 1L;

  private AID serverAID;
  
  private String user;
  private String pass;
  private String op;

  private ConversationStore convs;
  private ChatController ui;
  private ClientMessageHandler handler;
  
  private usv.llm.JadeLlmClient llmClient;



  @Override
  protected void setup() {
    Object[] args = getArguments();
    user = (args != null && args.length > 0) ? String.valueOf(args[0]) : getLocalName();
    pass = (args != null && args.length > 1) ? String.valueOf(args[1]) : "";
    op   = (args != null && args.length > 2) ? String.valueOf(args[2]) : "login";
    
    convs = new ConversationStore(user);
    handler = new ClientMessageHandler(this, user, convs);
    llmClient = new usv.llm.JadeLlmClient(this);

    // Find server then auth
    addBehaviour(new TickerBehaviour(this, 500) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onTick() {
    	  
        if (serverAID != null) return;

        Optional<AID> found = DfUtil.findOne(myAgent, "chat-server");
        if (found.isPresent()) {
          serverAID = found.get();
          handler.setServerAID(serverAID);

          send(ChatProtocol.auth(serverAID, op, user, pass));
          stop();
        } 
      }
    });

    // Inbox
    addBehaviour(new CyclicBehaviour() {
      private static final long serialVersionUID = 1L;

      @Override
      public void action() {
        ACLMessage msg = receive();
        if (msg == null) { block(); return; }
        
        // Handle LLM replies first (before any other parsing/handling)
        if (llmClient != null && llmClient.handleIncoming(msg)) {
          return;
        }

        usv.messages.MsgCodec.Parsed p = ChatProtocol.parse(msg);
        String type = p.type();

        if (ChatProtocol.AUTH_OK.equals(type)) {
            openUiIfNeeded();
            handler.setUi(ui);
            handler.handle(msg);
            return;
         }

        if (ChatProtocol.AUTH_FAIL.equals(type)) {
          final String reason = p.kv().get("reason");
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { JOptionPane.showMessageDialog(null, "Auth failed: " + reason); }
          });
          doDelete();
          return;
        }

        handler.handle(msg);
      }
    });
  }

  private void openUiIfNeeded() {
    if (ui != null) return;

    ChatWindow win = new ChatWindow(user);
    ui = new ChatController(win, convs, new ChatCallbacks() {
      public void onSend(String to, String text) {
        if (serverAID == null || !handler.isAuthed()) {
          ui.appendLine("[system] Not connected.");
          return;
        }

        send(ChatProtocol.sendChat(serverAID, to, text));

        String line = "me: " + text;
        convs.addLine(to, line);
        if (to.equals(convs.getActivePeer())) ui.appendLine(line);
      }
      public void onPeerSelected(String peer) { }
      public void onWindowClosed() { doDelete(); }
    }, 
      llmClient);


    ui.show();
  }

  
  @Override
  protected void takeDown() {
    try {
      if (serverAID != null && handler != null && handler.isAuthed()) {
        send(ChatProtocol.unregister(serverAID, user));
      }
    } catch (Exception ignored) {}
    super.takeDown();
  }
}
