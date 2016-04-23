package org.commonreality.reality.control;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.participant.IParticipant.State;
import org.commonreality.reality.CommonReality;
import org.commonreality.sensors.ISensor;

public class RealitySetup implements Runnable
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RealitySetup.class);

  private final CommonReality             cr;

  private Collection<ISensor>        _sensors;

  private Collection<IAgent>         _agents;

  public RealitySetup(CommonReality cr, Collection<ISensor> sensors,
      Collection<IAgent> agents)
  {
    this.cr = cr;
    _sensors = new ArrayList<ISensor>(sensors);
    _agents = new ArrayList<IAgent>(agents);
  }

  public void run()
  {
    /*
     * initialize CR
     */
    if (cr != null) try
    {
      cr.getReality().initialize();
      cr.getReality().waitForState(State.INITIALIZED);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Could not initialize common reality ", e);
    }

    /*
     * and connect everyone
     */
    for (ISensor sensor : _sensors)
      try
      {
    	  if(cr != sensor.getCommonReality())
    		  throw new IllegalStateException("Sensor was not constructed for the CommonReality being set-up");
        sensor.connect();
        /*
         * we could wait for initialized, but by doing this we allow the
         * participants to initialize in parallel. we must check for both states
         * because it is possible that it would reach initialized before wait
         * for state(connected) is even called.
         */
        sensor.waitForState(State.CONNECTED, State.INITIALIZED);
        // sensor.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not connect sensor " + sensor, e);
      }

    for (IAgent agent : _agents)
      try
      {
    	  if(cr != agent.getCommonReality())
    		  throw new IllegalStateException("Agent was not constructed for the CommonReality being set-up");
        agent.connect();
        agent.waitForState(State.CONNECTED, State.INITIALIZED);
        // agent.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not connect agent " + agent, e);
      }

    /*
     * now, one last time, make sure everyone is initialized before returning
     */
    for (ISensor sensor : _sensors)
      try
      {
        sensor.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not initialize sensor " + sensor, e);
      }

    for (IAgent agent : _agents)
      try
      {
        agent.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not initialize agent " + agent, e);
      }

      if (LOGGER.isDebugEnabled())
      LOGGER.debug("Reality, sensors and agents are ready to go!");
  }

}
