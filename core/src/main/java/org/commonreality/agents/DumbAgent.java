package org.commonreality.agents;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.reality.CommonReality;

public class DumbAgent extends AbstractAgent
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER              = LogFactory
                                                             .getLog(DumbAgent.class);

  private boolean                    _participatesInTime = true;

  public DumbAgent(CommonReality cr)
  {
	  super(cr);
  }

  @Override
  public String getName()
  {
    return "dumb";
  }

  @Override
  public void start() throws Exception
  {
    super.start();
    if (_participatesInTime) getPeriodicExecutor().execute(this::timeLoop);
  }

  protected void timeLoop()
  {
    if (stateMatches(State.STARTED) && _participatesInTime)
      getClock().getAuthority().get().requestAndWaitForChange(null)
          .thenAccept((t) -> {
            timeLoop();
          });
  }

}
