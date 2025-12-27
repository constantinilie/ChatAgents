package usv.chat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationStore {

  private final Map<String, List<String>> conv = new ConcurrentHashMap<String, List<String>>();
  private volatile String activePeer = null;

  public void setActivePeer(String peer) {
    activePeer = peer;
  }

  public String getActivePeer() {
    return activePeer;
  }

  public void addLine(String peer, String line) {
    if (peer == null) return;
    List<String> lines = conv.get(peer);
    if (lines == null) {
      lines = Collections.synchronizedList(new ArrayList<String>());
      conv.put(peer, lines);
    }
    lines.add(line);
  }

  public String getConversationText(String peer) {
    if (peer == null) return "";
    List<String> lines = conv.get(peer);
    if (lines == null) return "";
    return joinLines(lines);
  }

  private String joinLines(List<String> lines) {
    StringBuilder sb = new StringBuilder();
    for (String s : lines) sb.append(s).append("\n");
    return sb.toString();
  }
}
