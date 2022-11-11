package io.github.zeal18.zio.mongodb.driver.reactivestreams

import org.reactivestreams.Subscriber
import zio.Task
import zio.UIO

private trait InterruptibleSubscriber[A, B] extends Subscriber[A] {
  def interrupt(): UIO[Unit]
  def await(): Task[B]
}
