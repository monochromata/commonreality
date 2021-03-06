/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.identifier;

import java.io.Serializable;

/**
 * unique identifier for an object. These are used to identify objects
 * 
 * @author developer
 */
public interface IIdentifier extends Serializable
{
  
  /**
   * 
   */
  static public final IIdentifier ALL = new IIdentifier(){

    /**
     * 
     */
    private static final long serialVersionUID = 3846318889403027814L;

    public String getName()
    {
      return "all";
    }

    public IIdentifier getOwner()
    {
      return null;
    }

    public Type getType()
    {
      return Type.OTHER;
    }
    
    @Override
    public String toString()
    {
      return "[ALL]";
    }
    
  };

  static public enum Type {
    REALITY, SENSOR, AGENT, OBJECT, AFFERENT, EFFERENT, EFFERENT_COMMAND, NOTIFICATION, OTHER
  };

  /**
   * human readable name
   * 
   * @return
   */
  public String getName();

  /**
   * owner of this identifier
   * 
   * @return identifer of the owner, may be null in the case of participants
   */
  public IIdentifier getOwner();

  public Type getType();
}
