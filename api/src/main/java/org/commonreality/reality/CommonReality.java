/*
 * Created on May 14, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.reality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.commonreality.agents.IAgent;
import org.commonreality.sensors.ISensor;

public class CommonReality
{

	private final IReality reality;

	private final List<ISensor> connectedSensors = new ArrayList<>();
	
	private final List<IAgent> connectedAgents = new ArrayList<>();
	
	public CommonReality(IReality reality) {
		this.reality = reality;
	}

  public IReality getReality()
  {
    return reality;
  }

  public void addSensor(ISensor sensor)
  {
    synchronized (connectedSensors)
    {
      connectedSensors.add(sensor);
    }
  }

  public void removeSensor(ISensor sensor)
  {
    synchronized (connectedSensors)
    {
      connectedSensors.remove(sensor);
    }
  }
	
  public void addAgent(IAgent agent)
  {
    synchronized (connectedAgents)
    {
      connectedAgents.add(agent);
    }
  }

  public void removeAgent(IAgent agent)
  {
    synchronized (connectedAgents)
    {
      connectedAgents.remove(agent);
    }
  }
	
  /**
   * @return An unmodifiable list of the sensors connected to this common reality.
   */
  public Collection<IAgent> getAgents()
  {
      return Collections.unmodifiableList(connectedAgents);
  }
  
	/**
	 * @return An unmodifiable list of the sensors connected to this common reality.
	 */
  public Collection<ISensor> getSensors()
  {
		return Collections.unmodifiableList(connectedSensors);
  }
}
