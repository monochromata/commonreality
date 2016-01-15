package org.commonreality.netty;

import org.commonreality.net.AbstractNetProviderTest;
import org.junit.Test;

public class SimpleNettyProviderTest extends AbstractNetProviderTest
{

	  @Test
	  public void testNettyNIO() throws Exception
	  {
	    testNIOSerializer(NettyNetworkingProvider.class.getName());
	  }
	
}
