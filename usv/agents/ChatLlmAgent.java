package usv.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import usv.jade.DfUtil;
import usv.llm.LlmHttpClient;
import usv.messages.MsgCodec;
import usv.protocol.ChatProtocol;
import usv.protocol.LlmProtocol;

public class ChatLlmAgent extends Agent {
  private static final long serialVersionUID = 1L;

  private final LlmHttpClient llmHttp = new LlmHttpClient("http://127.0.0.1:8000");

  @Override
  protected void setup() {
    DfUtil.register(this, "llm-service", "LLM Service");

    addBehaviour(new CyclicBehaviour() {
      private static final long serialVersionUID = 1L;

      public void action() {
        ACLMessage msg = receive();
        if (msg == null) { block(); return; }

        MsgCodec.Parsed p;
        try {
          p = MsgCodec.unpack(msg.getContent());
        } catch (Exception e) {
          return;
        }

        if (!ChatProtocol.LLM_REQUEST.equals(p.type())) return;

        String rid = safe(p.kv().get("rid"));
        String op = safe(p.kv().get("op"));
        String text = p.kv().get("text");
        String targetLang = safe(p.kv().get("target_lang"));

        try {
          String result;
          if ("translate".equals(op)) {
            result = llmHttp.transform("translate", text, null, targetLang);
          } else {
            result = llmHttp.transform(op, text, null, null);
          }

          send(LlmProtocol.response(msg, rid, result));

        } catch (Exception ex) {
          send(LlmProtocol.fail(msg, rid, String.valueOf(ex.getMessage())));
        }
      }
    });
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }

  @Override
  protected void takeDown() {
    try { DfUtil.deregister(this); } catch (Exception ignored) {}
    super.takeDown();
  }
}
