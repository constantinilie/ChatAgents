package usv.server;

import jade.core.AID;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineRegistry {

  private final Map<String, AID> online = new ConcurrentHashMap<String, AID>();

  public void putOnline(String user, AID aid) {
    online.put(user, aid);
  }

  public AID getAID(String user) {
    return online.get(user);
  }

  public boolean isOnline(String user) {
    return online.containsKey(user);
  }

  public void remove(String user) {
    online.remove(user);
  }

  public Collection<AID> allAids() {
    return online.values();
  }

  public String findUserByAID(AID aid) {
    for (Map.Entry<String, AID> e : online.entrySet()) {
      if (e.getValue().equals(aid)) return e.getKey();
    }
    return null;
  }

  public String getOnlineUsersCsv() {
    return String.join(",", new TreeSet<String>(online.keySet()));
  }

  public Set<String> getOnlineUsersSorted() {
    return new TreeSet<String>(online.keySet());
  }
}
