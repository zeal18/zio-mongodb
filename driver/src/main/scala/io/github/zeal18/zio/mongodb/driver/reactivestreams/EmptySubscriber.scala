package io.github.zeal18.zio.mongodb.driver.reactivestreams

import org.reactivestreams.Subscription
import zio.Scope
import zio.Task
import zio.Trace
import zio.UIO
import zio.URIO
import zio.ZIO

import java.util.concurrent.atomic.AtomicBoolean

private object EmptySubscriber {
  def make[A](implicit trace: Trace): URIO[Scope, InterruptibleSubscriber[A, Unit]] = for {
    subscriptionP <- ZIO.acquireRelease(
      Promise.make[Throwable, Subscription],
    )(
      _.poll.flatMap(_.fold(ZIO.unit)(_.foldZIO(_ => ZIO.unit, sub => ZIO.succeed(sub.cancel())))),
    )
    promise <- Promise.make[Throwable, Unit]
  } yield new InterruptibleSubscriber[A, Unit] {

    val isSubscribedOrInterrupted = new AtomicBoolean

    override def interrupt(implicit trace: Trace): UIO[Unit] = {
      isSubscribedOrInterrupted.set(true)
      promise.interrupt.unit
    }

    override def await(implicit trace: Trace): Task[Unit] = promise.await

    override def onSubscribe(s: Subscription): Unit =
      if (s == null)
        failNPE("s was null in onSubscribe")
      else {
        val shouldCancel = isSubscribedOrInterrupted.getAndSet(true)
        if (shouldCancel) s.cancel()
        else {
          subscriptionP.unsafe.done(ZIO.succeed(s))
          s.request(Int.MaxValue)
        }
      }

    override def onNext(t: A): Unit = ()

    override def onError(e: Throwable): Unit =
      if (e == null)
        failNPE("t was null in onError")
      else
        fail(e)

    override def onComplete(): Unit =
      promise.unsafe.done(ZIO.unit)

    private def failNPE(msg: String) = {
      val e = new NullPointerException(msg)
      fail(e)
      throw e
    }

    private def fail(e: Throwable): Unit =
      promise.unsafe.done(ZIO.fail(e))
  }
}
