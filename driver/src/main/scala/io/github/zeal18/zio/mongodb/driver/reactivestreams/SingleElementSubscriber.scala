package io.github.zeal18.zio.mongodb.driver.reactivestreams

import java.util.concurrent.atomic.AtomicBoolean

import org.reactivestreams.Subscription
import zio.Scope
import zio.Task
import zio.UIO
import zio.URIO
import zio.ZIO

private object SingleElementSubscriber {
  def make[A]: URIO[Scope, InterruptibleSubscriber[A, Option[A]]] = for {
    subscriptionP <- ZIO.acquireRelease(
      Promise.make[Throwable, Subscription],
    )(
      _.poll.flatMap(_.fold(ZIO.unit)(_.foldZIO(_ => ZIO.unit, sub => ZIO.succeed(sub.cancel())))),
    )
    promise <- Promise.make[Throwable, Option[A]]
  } yield new InterruptibleSubscriber[A, Option[A]] {

    val isSubscribedOrInterrupted = new AtomicBoolean

    override def interrupt(): UIO[Unit] = {
      isSubscribedOrInterrupted.set(true)
      promise.interrupt.unit
    }

    override def await(): Task[Option[A]] = promise.await

    override def onSubscribe(s: Subscription): Unit =
      if (s == null) // scalafix:ok
        failNPE("s was null in onSubscribe")
      else {
        val shouldCancel = isSubscribedOrInterrupted.getAndSet(true)
        if (shouldCancel) s.cancel()
        else {
          subscriptionP.unsafe.done(ZIO.succeed(s))
          s.request(Int.MaxValue)
        }
      }

    override def onNext(t: A): Unit =
      if (t == null) // scalafix:ok
        failNPE("t was null in onNext")
      else
        promise.unsafe.done(ZIO.succeed(Some(t)))

    override def onError(e: Throwable): Unit =
      if (e == null) // scalafix:ok
        failNPE("t was null in onError")
      else
        fail(e)

    override def onComplete(): Unit =
      promise.unsafe.done(ZIO.succeed(None))

    private def failNPE(msg: String) = {
      val e = new NullPointerException(msg)
      fail(e)
      throw e // scalafix:ok
    }

    private def fail(e: Throwable): Unit =
      promise.unsafe.done(ZIO.fail(e))
  }
}
