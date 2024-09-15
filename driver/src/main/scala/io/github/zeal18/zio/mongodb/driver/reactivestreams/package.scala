package io.github.zeal18.zio.mongodb.driver

import org.reactivestreams.Publisher
import zio.Chunk
import zio.Task
import zio.Trace
import zio.ZIO

import scala.collection.IterableFactory

package object reactivestreams {
  implicit class PublisherOps[A](private val publisher: Publisher[A]) extends AnyVal {
    def headOption(implicit trace: Trace): Task[Option[A]] =
      ZIO.scoped(SingleElementSubscriber.make[A].flatMap(subscribeAndAwait))

    def head(implicit trace: Trace): Task[A] =
      headOption.someOrFail(new IllegalStateException("Expected one value but received nothing"))

    def unit(implicit trace: Trace): Task[Unit] = ZIO.scoped(EmptySubscriber.make[A].flatMap(subscribeAndAwait))

    def toIterable[I[B] <: Iterable[B], B](factory: IterableFactory[I])(implicit trace: Trace): Task[I[A]] =
      ZIO.scoped(IterableSubscriber.make[A, I, B](factory).flatMap(subscribeAndAwait))

    def toList(implicit trace: Trace): Task[List[A]]     = toIterable(List)
    def toSeq(implicit trace: Trace): Task[Seq[A]]       = toIterable(Seq)
    def toVector(implicit trace: Trace): Task[Vector[A]] = toIterable(Vector)
    def toSet(implicit trace: Trace): Task[Set[A]]       = toIterable(Set)
    def toChunk(implicit trace: Trace): Task[Chunk[A]]   = toIterable(Chunk)

    private def subscribeAndAwait[B](subscriber: InterruptibleSubscriber[A, B])(implicit trace: Trace) =
      (for {
        _      <- ZIO.attempt(publisher.subscribe(subscriber))
        result <- subscriber.await
      } yield result).onInterrupt(subscriber.interrupt)
  }
}
