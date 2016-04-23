package org.commonreality.util;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class LockUtilities
{

  static private boolean             _useInfiniteLocks = !Boolean
                                                           .getBoolean("org.commonreality.lockUtilities.useLimitedLocks");

  @Deprecated
  static private Map<Lock, LockInfo> _recentLocks      = Collections
                                                           .synchronizedMap(new WeakHashMap<Lock, LockInfo>());
  
  private final Map<Lock, LockInfo> recentLocks      = Collections
          .synchronizedMap(new WeakHashMap<Lock, LockInfo>());

  /**
   * This method might be renamed {@code getLockInfo()} after the static method has been removed.
   * @return
   */
  public String lockInfo()
  {
    StringBuilder sb = new StringBuilder("LockUtilities, tracked locks: \n");
    synchronized (recentLocks)
    {
      recentLocks.entrySet().forEach(
          (e) -> {
            sb.append("  lock(").append(e.getKey().getClass().getSimpleName());
            sb.append(")[").append(e.getKey().hashCode())
                .append("] last acquired by ").append(e.getValue())
                .append("\n");
          });
    }

    return sb.toString();
  }
  
  /**
   * more diagnostics
   * 
   * @return
   * @deprecated Use {@link #lockInfo()} instead.
   */
  @Deprecated
  static public String getLockInfo()
  {
    StringBuilder sb = new StringBuilder("LockUtilities, tracked locks: \n");
    synchronized (_recentLocks)
    {
      _recentLocks.entrySet().forEach(
          (e) -> {
            sb.append("  lock(").append(e.getKey().getClass().getSimpleName());
            sb.append(")[").append(e.getKey().hashCode())
                .append("] last acquired by ").append(e.getValue())
                .append("\n");
          });
    }

    return sb.toString();
  }

  /**
   * This method might be renamed {@code runLocked} after the static method has been removed.
   * @param lock
   * @param runnable
   * @throws InterruptedException
   */
  public void doLocked(Lock lock, Runnable runnable)
	      throws InterruptedException
	  {
	    boolean locked = false;
	    try
	    {
	      locked = tryToLock(lock, runnable);
	      if (locked)
	        runnable.run();
	      else
	        throw new InterruptedException(String.format(
	            "%s failed to acquire lock %s, recently acquired by %s",
	            Thread.currentThread(), lock, recentLocks.get(lock)));
	    }
	    finally
	    {
	      if (locked) tryToUnlock(lock, runnable);
	    }
	  }
  
  /**
   * 
   * @param lock
   * @param runnable
   * @throws InterruptedException
   * @deprecated Use {@link doLocked(Lock,Runnable)} instead.
   */
  @Deprecated
  static public void runLocked(Lock lock, Runnable runnable)
      throws InterruptedException
  {
    boolean locked = false;
    try
    {
      locked = attemptLock(lock, runnable);
      if (locked)
        runnable.run();
      else
        throw new InterruptedException(String.format(
            "%s failed to acquire lock %s, recently acquired by %s",
            Thread.currentThread(), lock, _recentLocks.get(lock)));
    }
    finally
    {
      if (locked) attemptUnlock(lock, runnable);
    }
  }
  
  public <T> T doLocked(Lock lock, Callable<T> callable)
	      throws InterruptedException, Exception
	  {
	    boolean locked = false;
	    try
	    {
	      locked = tryToLock(lock, callable);
	      if (locked)
	        return callable.call();
	      else
	        throw new InterruptedException(String.format(
	            "%s failed to acquire lock %s, recently acquired by %s",
	            Thread.currentThread(), lock, recentLocks.get(lock)));
	    }
	    finally
	    {
	      if (locked) tryToUnlock(lock, callable);
	    }
	  }

  /**
   * 
   * @param lock
   * @param callable
   * @return
   * @throws InterruptedException
   * @throws Exception
   * @deprecated Use {@link #doLocked(Lock, Callable)} instead.
   */
  @Deprecated
  static public <T> T runLocked(Lock lock, Callable<T> callable)
      throws InterruptedException, Exception
  {
    boolean locked = false;
    try
    {
      locked = attemptLock(lock, callable);
      if (locked)
        return callable.call();
      else
        throw new InterruptedException(String.format(
            "%s failed to acquire lock %s, recently acquired by %s",
            Thread.currentThread(), lock, _recentLocks.get(lock)));
    }
    finally
    {
      if (locked) attemptUnlock(lock, callable);
    }
  }

  protected void tryToUnlock(Lock lock, Object with)
  {
    recentLocks.get(lock).close();
    lock.unlock();
  }
  
  /**
   * 
   * @param lock
   * @param with
   * @deprecated Use {@link #tryToUnlock(Lock, Object)} instead.
   */
  @Deprecated
  static protected void attemptUnlock(Lock lock, Object with)
  {
    _recentLocks.get(lock).close();
    lock.unlock();
  }

  protected boolean tryToLock(Lock lock, Object with)
	      throws InterruptedException
	  {
	    boolean rtn = false;
	    if (_useInfiniteLocks)
	    {
	      lock.lock();
	      rtn = true;
	    }
	    else
	      rtn = lock.tryLock(1, TimeUnit.MINUTES);
	    if (rtn) recentLocks.put(lock, new LockInfo());
	    return rtn;
	  }
  
  /**
   * 
   * @param lock
   * @param with
   * @return
   * @throws InterruptedException
   * @deprecated Use {@link #tryToLock(Lock, Object)} instead.
   */
  @Deprecated
  static protected boolean attemptLock(Lock lock, Object with)
      throws InterruptedException
  {
    boolean rtn = false;
    if (_useInfiniteLocks)
    {
      lock.lock();
      rtn = true;
    }
    else
      rtn = lock.tryLock(1, TimeUnit.MINUTES);
    if (rtn) _recentLocks.put(lock, new LockInfo());
    return rtn;
  }

  static private class LockInfo
  {
    Thread _thread;

    long   _entryTime;

    long   _exitTime;

    public LockInfo()
    {
      _thread = Thread.currentThread();
      _entryTime = System.nanoTime();
    }

    public void close()
    {
      _exitTime = System.nanoTime();
    }

    @Override
    public String toString()
    {
      return String.format("%s enter: %d  exit :%d", _thread, _entryTime,
          _exitTime);
    }
  }
}
