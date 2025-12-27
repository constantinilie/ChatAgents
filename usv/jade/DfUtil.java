package usv.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Optional;

public final class DfUtil {

  private DfUtil() {}

  // Find first agent registered with a service type
  public static Optional<AID> findOne(Agent agent, String serviceType) {
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType(serviceType);
    template.addServices(sd);

    try {
      DFAgentDescription[] res = DFService.search(agent, template);
      if (res != null && res.length > 0) {
        return Optional.of(res[0].getName());
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  // Register current agent in DF
  public static void register(Agent agent, String serviceType, String serviceName) {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(agent.getAID());

    ServiceDescription sd = new ServiceDescription();
    sd.setType(serviceType);
    sd.setName(serviceName);
    dfd.addServices(sd);

    try {
      DFService.register(agent, dfd);
    } catch (FIPAException e) {
      e.printStackTrace();
    }
  }

  // Deregister current agent from DF
  public static void deregister(Agent agent) {
    try {
      DFService.deregister(agent);
    } catch (Exception ignored) {}
  }
}
