package usv.chat;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationStore {

  private final Map<String, List<String>> conv = new ConcurrentHashMap<String, List<String>>();
  private volatile String activePeer;

  private final String owner; // logged-in user
  private final File baseDir;
  private final Object fileLock = new Object();

  public ConversationStore(String owner) {
    this.owner = owner == null ? "user" : owner.trim();
    this.baseDir = new File("chat_logs", safeFilename(owner));
    if (!baseDir.exists()) baseDir.mkdirs();

    // Load existing logs on startup
    loadAllFromDisk();
  }

  public void setActivePeer(String peer) {
    this.activePeer = peer;
  }

  public String getActivePeer() {
    return activePeer;
  }

  public void addLine(String peer, String line) {
    if (peer == null || peer.trim().isEmpty()) return;

    peer = peer.trim();
    List<String> lines = conv.get(peer);
    if (lines == null) {
      lines = Collections.synchronizedList(new ArrayList<String>());
      conv.put(peer, lines);
    }
    lines.add(line);

    // Persist
    appendToDisk(peer, line);
  }

  public String getConversationText(String peer) {
    if (peer == null) return "";
    List<String> lines = conv.get(peer);
    if (lines == null) return "";
    StringBuilder sb = new StringBuilder();
    synchronized (lines) {
      for (String s : lines) sb.append(s).append("\n");
    }
    return sb.toString();
  }

  public List<String> getPeers() {
    return new ArrayList<String>(conv.keySet());
  }

  // --- persistence ---

  private void appendToDisk(String peer, String line) {
    File f = fileForPeer(peer);

    synchronized (fileLock) {
      FileWriter fw = null;
      try {
        fw = new FileWriter(f, true);
        fw.write(line.replace("\n", " ").replace("\r", " "));
        fw.write("\n");
        fw.flush();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try { if (fw != null) fw.close(); } catch (Exception ignored) {}
      }
    }
  }

  private void loadAllFromDisk() {
    File[] files = baseDir.listFiles();
    if (files == null) return;

    for (File f : files) {
      if (!f.isFile()) continue;
      if (!f.getName().endsWith(".txt")) continue;

      String peer = peerFromFilename(f.getName());
      if (peer == null) continue;

      List<String> lines = Collections.synchronizedList(new ArrayList<String>());
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
          if (line.trim().isEmpty()) continue;
          lines.add(line);
        }
        conv.put(peer, lines);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try { if (br != null) br.close(); } catch (Exception ignored) {}
      }
    }
  }

  private File fileForPeer(String peer) {
	  String p = safeFilename(peer);
	  return new File(baseDir, p + ".txt");
  }


  private String peerFromFilename(String name) {
	  if (!name.endsWith(".txt")) return null;
	  return name.substring(0, name.length() - 4);
  }	

  private String safeFilename(String s) {
    if (s == null) return "user";
    String x = s.trim().toLowerCase();
    x = x.replaceAll("[^a-z0-9._-]", "_");
    return x;
  }
  public String getOwner() {
	  return owner;
	}

}
