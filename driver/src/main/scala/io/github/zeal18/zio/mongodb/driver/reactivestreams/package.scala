package io.github.zeal18.zio.mongodb.driver

import scala.collection.IterableFactory

import org.reactivestreams.Publisher
import zio.Chunk
import zio.Task
import zio.ZIO

package object reactivestreams {
  implicit class PublisherOps[A](private val publisher: Publisher[A]) extends AnyVal {
    def headOption: Task[Option[A]] =
      ZIO.scoped(SingleElementSubscriber.make[A].flatMap(subscribeAndAwait))

    def head: Task[A] =
      headOption.someOrFail(new IllegalStateException("Expected one value but received nothing"))

    def unit: Task[Unit] = ZIO.scoped(EmptySubscriber.make[A].flatMap(subscribeAndAwait))

    def toIterable[I[B] <: Iterable[B], B](factory: IterableFactory[I]): Task[I[A]] =
      ZIO.scoped(IterableSubscriber.make[A, I, B](factory).flatMap(subscribeAndAwait))

    def toList: Task[List[A]]     = toIterable(List)
    def toSeq: Task[Seq[A]]       = toIterable(Seq)
    def toVector: Task[Vector[A]] = toIterable(Vector)
    def toSet: Task[Set[A]]       = toIterable(Set)
    def toChunk: Task[Chunk[A]]   = toIterable(Chunk)

    private def subscribeAndAwait[B](subscriber: InterruptibleSubscriber[A, B]) =
      (for {
        _      <- ZIO.attempt(publisher.subscribe(subscriber))
        result <- subscriber.await()
      } yield result).onInterrupt(subscriber.interrupt())
  }
}
