package io.github.zeal18.zio.mongodb.driver.reactivestreams

import org.reactivestreams.Subscriber
import zio.Task
import zio.Trace
import zio.UIO

private trait InterruptibleSubscriber[A, B] extends Subscriber[A] {
  def interrupt(implicit trace: Trace): UIO[Unit]
  def await(implicit trace: Trace): Task[B]
}
