package usv.llm;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import usv.jade.DfUtil;
import usv.messages.MsgCodec;
import jade.core.behaviours.OneShotBehaviour;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class JadeLlmClient implements LlmActions {

  private final Agent agent;
  private volatile AID llmAID;

  // rid -> waiting queue
  private final Map<String, BlockingQueue<String>> wait =
		    new ConcurrentHashMap<String, BlockingQueue<String>>();

  public JadeLlmClient(Agent agent) {
    this.agent = agent;
  }

  // Called from ChatClientAgent inbox before handler.handle(msg)
  public boolean handleIncoming(ACLMessage msg) {
    MsgCodec.Parsed p;
    try {
      p = MsgCodec.unpack(msg.getContent());
    } catch (Exception e) {
      return false;
    }

    String type = p.type();
    if (!"LLM_RESPONSE".equals(type) && !"LLM_FAIL".equals(type)) return false;

    String rid = p.kv().get("rid");
    if (rid == null) return true;

    BlockingQueue<String> q = wait.get(rid);
    if (q == null) return true;

    if ("LLM_RESPONSE".equals(type)) {
      String result = p.kv().get("result");
      try {
    	 q.put(result == null ? "" : result);
      } catch (InterruptedException ignored) {}
    } else {
      String reason = p.kv().get("reason");
      try {
    	  q.put("[error] " + (reason == null ? "unknown" : reason));
      } catch (InterruptedException ignored) {}
    }

    wait.remove(rid);
    return true;
  }

  public String run(String op, String inputText) throws Exception {
    return callLlmSync(op, inputText, null);
  }

  public String translate(String inputText, String targetLang) throws Exception {
    return callLlmSync("translate", inputText, targetLang);
  }

  private String callLlmSync(String op, String inputText, String targetLang) throws Exception {
    ensureLlmAID();

    String rid = UUID.randomUUID().toString();
    BlockingQueue<String> q = new ArrayBlockingQueue<String>(1);
    wait.put(rid, q);

    Map<String, String> payload = new LinkedHashMap<String, String>();
    payload.put("rid", rid);
    payload.put("op", op);
    payload.put("text", inputText);

    if (targetLang != null && !targetLang.trim().isEmpty()) {
      payload.put("target_lang", targetLang.trim());
    }

    ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
    m.addReceiver(llmAID);
    m.setOntology("chat");
    m.setConversationId("llm");
    m.setContent(MsgCodec.pack("LLM_REQUEST", payload));
    agent.send(m);

    // Wait for reply; ChatController already calls this in background thread
    String res = q.poll(90, TimeUnit.SECONDS);
    wait.remove(rid);

    if (res == null) throw new RuntimeException("LLM timeout");
    if (res.startsWith("[error]")) throw new RuntimeException(res);
    return res;
  }

  private void ensureLlmAID() throws Exception {
	  if (llmAID != null) return;
	
	  // Retry a bit, because DF registration might take a moment
	  for (int i = 0; i < 15; i++) {
	    AID found = findInDfOnAgentThread("llm-service");
	    if (found != null) {
	      llmAID = found;
	      return;
	    }
	    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
	  }
	
	  throw new RuntimeException("LLM agent not found in DF (llm-service)");
  }
  
  private AID findInDfOnAgentThread(final String serviceType) throws Exception {
	  final java.util.concurrent.BlockingQueue<AID> q =
	      new java.util.concurrent.ArrayBlockingQueue<AID>(1);

	  agent.addBehaviour(new OneShotBehaviour(agent) {
	    private static final long serialVersionUID = 1L;

	    public void action() {
	      Optional<AID> found = DfUtil.findOne(agent, serviceType);
	      try {
	        q.put(found.isPresent() ? found.get() : null);
	      } catch (InterruptedException ignored) {}
	    }
	  });

	  return q.poll(2, TimeUnit.SECONDS);
	}


}
