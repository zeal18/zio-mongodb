package io.github.zeal18.zio.mongodb.driver.reactivestreams

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.reactivestreams.tck.TestEnvironment
import org.reactivestreams.tck.TestEnvironment.ManualPublisher
import zio.Chunk
import zio.Exit
import zio.Fiber
import zio.Runtime
import zio.Supervisor
import zio.Task
import zio.Trace
import zio.UIO
import zio.Unsafe
import zio.ZEnvironment
import zio.ZIO
import zio.durationInt
import zio.test.Assertion.*
import zio.test.*

object EmptySubscriberSpec extends ZIOSpecDefault {
  override def spec =
    suite("EmptySubscriberSpec")(
      test("works with a well behaved `Publisher`") {
        assertZIO(publish(seq, None))(succeeds(isUnit))
      },
      test("fails with an initially failed `Publisher`") {
        assertZIO(publish(Chunk.empty, Some(e)))(fails(equalTo(e)))
      },
      test("returns Unit when completed without elements") {
        withProbe(probe =>
          assertZIO((for {
            fiber  <- probe.unit.fork
            _      <- ZIO.attemptBlockingInterrupt(probe.expectRequest())
            _      <- ZIO.attempt(probe.sendCompletion())
            result <- fiber.join
          } yield result).exit)(
            succeeds(isUnit),
          ),
        )
      } @@ TestAspect.timeout(60.seconds),
      test("does not fail a fiber on failing `Publisher`") {

        val publisher = new Publisher[Int] {
          override def subscribe(s: Subscriber[? >: Int]): Unit =
            s.onSubscribe(
              new Subscription {
                override def request(n: Long): Unit = s.onError(new Throwable("boom!"))
                override def cancel(): Unit         = ()
              },
            )
        }

        val supervisor =
          new Supervisor[Boolean] {

            override def onStart[R, E, A](
              environment: ZEnvironment[R],
              effect: ZIO[R, E, A],
              parent: Option[Fiber.Runtime[Any, Any]],
              fiber: Fiber.Runtime[E, A],
            )(implicit unsafe: Unsafe): Unit = ()

            override def onEnd[R, E, A](value: Exit[E, A], fiber: Fiber.Runtime[E, A])(implicit
              unsafe: Unsafe,
            ): Unit =
              if (value.isFailure) failedAFiber = true

            @transient var failedAFiber = false

            def value(implicit trace: Trace): UIO[Boolean] =
              ZIO.succeed(failedAFiber)

          }

        for {
          runtime <- Runtime.addSupervisor(supervisor).toRuntime
          exit    <- runtime.run(publisher.unit.exit)
          failed  <- supervisor.value
        } yield assert(exit)(fails(anything)) && assert(failed)(isFalse)

      },
      test("cancels subscription when interrupted before subscription") {
        val tst =
          for {
            subscriberP    <- Promise.make[Nothing, Subscriber[?]]
            cancelledLatch <- Promise.make[Nothing, Unit]

            subscription = new Subscription {
              override def request(n: Long): Unit = ()
              override def cancel(): Unit         = cancelledLatch.unsafe.done(ZIO.unit)
            }
            probe = new Publisher[Int] {
              override def subscribe(subscriber: Subscriber[? >: Int]): Unit =
                subscriberP.unsafe.done(ZIO.succeed(subscriber))
            }

            fiber      <- probe.unit.fork
            subscriber <- subscriberP.await
            _          <- fiber.interrupt
            _          <- ZIO.succeed(subscriber.onSubscribe(subscription))
            _          <- cancelledLatch.await
          } yield ()

        assertZIO(tst.exit)(succeeds(anything))
      } @@ TestAspect.nonFlaky @@ TestAspect.timeout(60.seconds),
      test("cancels subscription when interrupted after subscription") {
        withProbe(probe =>
          assertZIO((for {
            fiber <- probe.unit.fork
            _     <- ZIO.attemptBlockingInterrupt(probe.expectRequest())
            _     <- fiber.interrupt
            _     <- ZIO.attemptBlockingInterrupt(probe.expectCancelling())
          } yield ()).exit)(
            succeeds(isUnit),
          ),
        )
      } @@ TestAspect.nonFlaky @@ TestAspect.timeout(60.seconds),
      test("cancels subscription when interrupted during consumption") {
        withProbe(probe =>
          assertZIO((for {
            fiber <- probe.unit.fork
            _     <- ZIO.attemptBlockingInterrupt(probe.expectRequest())
            _     <- ZIO.attempt((1 to 10).foreach(i => probe.sendNext(i)))
            _     <- fiber.interrupt
            _     <- ZIO.attemptBlockingInterrupt(probe.expectCancelling())
          } yield ()).exit)(
            succeeds(isUnit),
          ),
        )
      } @@ TestAspect.nonFlaky @@ TestAspect.timeout(60.seconds),
    )

  val e: Throwable    = new RuntimeException("boom")
  val seq: Chunk[Int] = Chunk.fromIterable(List.range(0, 100))

  def withProbe[R, E0, E >: Throwable, A](f: ManualPublisher[Int] => ZIO[R, E, A]): ZIO[R, E, A] = {
    val testEnv = new TestEnvironment(5000, 500)
    val probe   = new ManualPublisher[Int](testEnv)
    f(probe) <* ZIO.attempt(testEnv.verifyNoAsyncErrorsNoDelay())
  }

  def publish(seq: Chunk[Int], failure: Option[Throwable]): UIO[Exit[Throwable, Unit]] = {

    def loop(probe: ManualPublisher[Int], remaining: Chunk[Int]): Task[Unit] =
      for {
        n <- ZIO.attemptBlockingInterrupt(probe.expectRequest())
        split         = n.toInt
        (nextN, tail) = remaining.splitAt(split)
        _ <- ZIO.attempt(nextN.foreach(probe.sendNext))
        _ <-
          if (nextN.size < split)
            ZIO.attempt(failure.fold(probe.sendCompletion())(probe.sendError))
          else loop(probe, tail)
      } yield ()

    val faillable =
      withProbe(probe =>
        for {
          fiber <- probe.unit.fork
          _     <- loop(probe, seq)
          r     <- fiber.join
        } yield r,
      )

    faillable.exit
  }
}
