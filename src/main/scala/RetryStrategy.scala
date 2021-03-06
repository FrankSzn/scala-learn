import java.util.Random

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.blocking

/**
  * Interface defining a retry strategy
  *
  * @author xu.zhang
  */
sealed trait RetryStrategy {

  /**
    * Returns `true` if the retry should be performed
    */
  def shouldRetry(): Boolean

  /**
    * Returns the new retry strategy state
    */
  def update(): RetryStrategy
}

/**
  * Simplest retry strategy that performs retry
  */
object NoRetry extends RetryStrategy {
  override def shouldRetry(): Boolean = false

  override def update(): RetryStrategy = ???
}

class MaxNumberOfRetriesStrategy(val maxAttempts: Int) extends RetryStrategy {
  override def shouldRetry(): Boolean = maxAttempts > 0

  override def update(): RetryStrategy =
    new MaxNumberOfRetriesStrategy(maxAttempts = maxAttempts - 1)
}

class FixedWaitRetryStrategy(val millis: Long, override val maxAttempts: Int)
  extends MaxNumberOfRetriesStrategy(maxAttempts) with Sleep {

  override def update(): RetryStrategy = {
    sleep(millis)
    new FixedWaitRetryStrategy(millis, maxAttempts - 1)
  }
}

class RandomWaitRetryStrategy(val minimumWaitTime: Long,
                              val maximumWaitTime: Long,
                              override val maxAttempts: Int)
  extends MaxNumberOfRetriesStrategy(maxAttempts) with Sleep {

  private[this] final val random: Random = new Random()

  override def update(): RetryStrategy = {
    val millis: Long =
      math.abs(random.nextLong) % (maximumWaitTime - minimumWaitTime)
    sleep(millis)
    new RandomWaitRetryStrategy(
      minimumWaitTime,
      maximumWaitTime,
      maxAttempts - 1
    )
  }
}

class FibonacciBackOffStrategy(
                                waitTime: Long, step: Long, override val maxAttempts: Int)
  extends MaxNumberOfRetriesStrategy(maxAttempts) with Sleep {
  def fibonacci(n: Long) = {
    n match {
      case x@(0L | 1L) => x
      case _ =>
        var prevPrev: Long = 0L
        var prev: Long = 1L
        var result: Long = 0L

        for (i <- 2L to n) {
          result = prev + prevPrev
          prevPrev = prev
          prev = result
        }
        result
    }
  }

  override def update(): RetryStrategy = {
    val millis: Long = fibonacci(step) * waitTime
    sleep(millis)
    new FibonacciBackOffStrategy(waitTime, step + 1, maxAttempts - 1)
  }
}

sealed trait Sleep {
  def sleep(millis: Long) =
    try {
      blocking(Thread.sleep(millis))
    } catch {
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        throw e
    }
}

object RetryStrategy {
  val noRetry = NoRetry

  def noBackOff(maxAttempts: Int) = new MaxNumberOfRetriesStrategy(maxAttempts)

  def fixedBackOff(retryDuration: FiniteDuration, maxAttempts: Int) =
    new FixedWaitRetryStrategy(retryDuration.toMillis, maxAttempts)

  def randomBackOff(minimumWaitDuration: FiniteDuration,
                    maximumWaitDuration: FiniteDuration,
                    maxAttempts: Int) =
    new RandomWaitRetryStrategy(minimumWaitDuration.toMillis,
      maximumWaitDuration.toMillis,
      maxAttempts)

  def fibonacciBackOff(initialWaitDuration: FiniteDuration, maxAttempts: Int) =
    new FibonacciBackOffStrategy(initialWaitDuration.toMillis, 1, maxAttempts)
}
