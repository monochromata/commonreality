package org.commonreality.mina;

import org.commonreality.net.AbstractNetProviderTest;
import org.junit.Test;

public class SimpleMinaProviderTest extends AbstractNetProviderTest
{

	  @Test
	  public void testMINANIO() throws Exception
	  {
	    testNIOSerializer(MINANetworkingProvider.class.getName());
	  }
	
}
