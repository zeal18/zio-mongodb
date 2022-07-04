package io.github.zeal18.zio.mongodb.driver.query

import zio.stream.ZStream

trait Query[A] {
  def execute: ZStream[Any, Throwable, A]
}
