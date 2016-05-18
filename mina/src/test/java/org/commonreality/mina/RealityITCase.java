/*
 * Created on Apr 15, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.mina;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.SocketAddress;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.AbstractAgent;
import org.commonreality.agents.IAgent;
import org.commonreality.mina.protocol.NOOPProtocol;
import org.commonreality.mina.service.ClientService;
import org.commonreality.mina.service.ServerService;
import org.commonreality.mina.transport.LocalTransportProvider;
import org.commonreality.net.message.credentials.PlainTextCredentials;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.participant.IParticipant.State;
import org.commonreality.reality.CommonReality;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.DefaultReality;
import org.commonreality.sensors.AbstractSensor;
import org.commonreality.sensors.ISensor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * An integration test case using MINA.
 */
public class RealityITCase
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(RealityITCase.class);

  CommonReality cr;

  @Before
  public void setUp() throws Exception
  {
	  DefaultReality reality = DefaultReality.newInstanceThatNeedsToBePreparedWithACommonReality();
    cr = new CommonReality(reality);
    reality.prepare(cr);
    assertFalse(cr.getReality().stateMatches(State.INITIALIZED));
    assertFalse(cr.getReality().stateMatches(State.STARTED));
    setupConnection(cr.getReality());

    cr.getReality().initialize();
    assertTrue(cr.getReality().stateMatches(State.INITIALIZED));
  }

  protected void setupConnection(IReality reality) throws Exception
  {
    /*
     * set up default local connection
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IServerService service = new ServerService();
    ((DefaultReality) reality).addServerService(service, local, protocol,
        local
        .createAddress(1));
  }

  @After
  public void tearDown() throws Exception
  {
    cr.getReality().shutdown();
    assertFalse(cr.getReality().stateMatches(State.INITIALIZED));
  }

  @Test
  public void testStartUp() throws Exception
  {

    cr.getReality().start();
    assertTrue(cr.getReality().stateMatches(State.STARTED));

    cr.getReality().stop();
    assertTrue(cr.getReality().stateMatches(State.STOPPED));

    cr.getReality().reset(false);
    assertTrue(!cr.getReality().stateMatches(State.STARTED));

//    cr.getReality().shutdown();
    assertTrue(cr.getReality().stateMatches(State.INITIALIZED));
  }

  protected ISensor createSensor(CommonReality cr) throws Exception
  {
    /*
     * mock participant
     */
    AbstractSensor participant = new AbstractSensor(cr) {

      @Override
      public String getName()
      {
        return "mock";
      }
    };

    participant.setCredentials(new PlainTextCredentials("sensor", "pass"));

    SocketAddress address = ((DefaultReality) cr.getReality())
        .getAddressingInformation().getSocketAddress();

    /*
     * connect..
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IClientService service = new ClientService();
    participant.addClientService(service, local, protocol, address);

    assertFalse(participant.stateMatches(State.INITIALIZED));
    assertFalse(participant.stateMatches(State.STARTED));
    assertNull(participant.getIdentifier());

    return participant;
  }

  protected IAgent createAgent(CommonReality cr) throws Exception
  {
    /*
     * mock participant
     */
    AbstractAgent participant = new AbstractAgent(cr) {

      @Override
      public String getName()
      {
        return "agent";
      }
    };

    participant.setCredentials(new PlainTextCredentials("agent", "pass"));

    SocketAddress address = ((DefaultReality) cr.getReality())
        .getAddressingInformation().getSocketAddress();

    /*
     * connect..
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IClientService service = new ClientService();
    participant.addClientService(service, local, protocol, address);

    assertFalse(participant.stateMatches(State.INITIALIZED));
    assertFalse(participant.stateMatches(State.STARTED));
    assertNull(participant.getIdentifier());

    return participant;
  }

  @Test
  public void testMockSensor() throws Exception
  {
    AbstractSensor sensor = (AbstractSensor) createSensor(cr);
    AbstractAgent agent = (AbstractAgent) createAgent(cr);

    cr.getReality().configure(new TreeMap<String, String>());

    sensor.connect();
    sensor.waitForState(State.INITIALIZED);
    assertTrue(sensor.stateMatches(State.INITIALIZED));
    assertNotNull(sensor.getIdentifier());

    agent.connect();
    agent.waitForState(State.INITIALIZED);
    assertTrue(agent.stateMatches(State.INITIALIZED));
    assertNotNull(agent.getIdentifier());

    cr.getReality().start();
    assertTrue(cr.getReality().stateMatches(State.STARTED));

    

    sensor.waitForState(State.STARTED);
    assertTrue(sensor.stateMatches(State.STARTED));

    agent.waitForState(State.STARTED);
    assertTrue(agent.stateMatches(State.STARTED));

    // now with two kids playing, we can't test the clock..
    // IClock pClock = sensor.getClock();
    // IClock rClock = cr.getReality().getClock();
    //
    // assertEquals(1.0, pClock.waitForTime(1));
    // assertEquals(1.0, rClock.getTime());
    // assertEquals(1.5, pClock.waitForTime(1.5));
    // assertEquals(1.5, rClock.waitForTime(1.5));

    cr.getReality().stop();



    cr.getReality().reset(true);
    //
    // assertEquals(0.0, rClock.getTime());
    // assertEquals(0.0, pClock.waitForTime(0));

  }
}
