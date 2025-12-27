package usv.storage;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {

  private final File file;
  private final Object lock = new Object();
  private final Map<String, String> users = new ConcurrentHashMap<String, String>();

  public UserStore(File file) {
    this.file = file;
  }

  public Map<String, String> usersView() {
    return users;
  }

  public void load() {
    if (file == null || !file.exists()) return;

    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        int idx = line.indexOf(':');
        if (idx <= 0) continue;
        String u = line.substring(0, idx).trim();
        String p = line.substring(idx + 1).trim();
        if (!u.isEmpty()) users.put(u, p);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try { if (br != null) br.close(); } catch (Exception ignored) {}
    }
  }

  public boolean exists(String user) {
    return users.containsKey(user);
  }

  public String getPassword(String user) {
    return users.get(user);
  }

  public void put(String user, String pass) {
    users.put(user, pass);
  }

  public boolean appendToFile(String user, String pass) {
    synchronized (lock) {
      FileWriter fw = null;
      try {
        fw = new FileWriter(file, true);
        fw.write(user + ":" + pass + "\n");
        fw.flush();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      } finally {
        try { if (fw != null) fw.close(); } catch (Exception ignored) {}
      }
    }
  }
}
