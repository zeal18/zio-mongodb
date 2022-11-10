package io.github.zeal18.zio.mongodb.driver

import scala.collection.IterableFactory

import org.reactivestreams.Publisher
import zio.Chunk
import zio.Task
import zio.ZIO

package object reactivestreams {
  // implicit class PublisherVoidOps(private val publisher: Publisher[Void]) extends AnyVal {
  //   def unit: Task[Unit] = ZIO.scoped(for {
  //     subscriber <- EmptySubscriber.make[Void]
  //     _ = publisher.subscribe(subscriber)

  //     result <- subscriber.await().onInterrupt(ZIO.succeed(subscriber.interrupt()))
  //   } yield result)
  // }

  implicit class PublisherOps[A](private val publisher: Publisher[A]) extends AnyVal {
    def headOption: Task[Option[A]] = ZIO.scoped(for {
      subscriber <- SingleElementSubscriber.make[A]
      _ = publisher.subscribe(subscriber)

      result <- subscriber.await().onInterrupt(ZIO.succeed(subscriber.interrupt()))
    } yield result)

    def head: Task[A] =
      headOption.someOrFail(new IllegalStateException("Expected one value but received nothing"))

    def unit: Task[Unit] = ZIO.scoped(for {
      subscriber <- EmptySubscriber.make[A]
      _ = publisher.subscribe(subscriber)

      result <- subscriber.await().onInterrupt(ZIO.succeed(subscriber.interrupt()))
    } yield result)

    def toIterable[I[B] <: Iterable[B], B](factory: IterableFactory[I]): Task[I[A]] =
      ZIO.scoped(for {
        subscriber <- IterableSubscriber.make[A, I, B](factory)
        _ = publisher.subscribe(subscriber)

        result <- subscriber.await().onInterrupt(ZIO.succeed(subscriber.interrupt()))
      } yield result)

    def toList: Task[List[A]]     = toIterable(List)
    def toSeq: Task[Seq[A]]       = toIterable(Seq)
    def toVector: Task[Vector[A]] = toIterable(Vector)
    def toSet: Task[Set[A]]       = toIterable(Set)
    def toChunk: Task[Chunk[A]]   = toIterable(Chunk)
  }
}
