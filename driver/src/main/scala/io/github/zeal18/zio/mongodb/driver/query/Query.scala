package io.github.zeal18.zio.mongodb.driver.query

import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Chunk
import zio.Task
import zio.interop.reactivestreams.*
import zio.stream.ZStream

trait Query[A] {
  @deprecated("use runToZStream", "0.8.0")
  def execute: ZStream[Any, Throwable, A] = runToZStream()

  /** Runs query returning low level reactive Publisher
    *
    * It could be useful for dealing with special cases, otherwise it's recommended to use one of high level run* methods
    *
    * @see
    *   [[runToZSTream]]
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def run: Publisher[A]

  /** Runs query to a ZStream
    *
    * Use this method for potentially big or unlimited queries
    *
    * @param qSize internal buffer size
    *
    * @see
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def runToZStream(qSize: Int = 16): ZStream[Any, Throwable, A] = run.toZIOStream(qSize)

  /** Runs query to a [[List]]
    *
    * NOTE: this method collects all data in memory, use it only for queries with limited capacity
    *
    * @see
    *   [[runToZStream]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def runToList: Task[List[A]] = run.toList

  /** Runs query to a [[Seq]]
    *
    * NOTE: this method collects all data in memory, use it only for queries with limited capacity
    *
    * @see
    *   [[runToZStream]]
    *   [[runToList]]
    *   [[runToVector]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def runToSeq: Task[Seq[A]] = run.toSeq

  /** Runs query to a [[Vector]]
    *
    * NOTE: this method collects all data in memory, use it only for queries with limited capacity
    *
    * @see
    *   [[runToZStream]]
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def runToVector: Task[Vector[A]] = run.toVector

  /** Runs query to a [[Set]]
    *
    * NOTE: this method collects all data in memory, use it only for queries with limited capacity
    *
    * @see
    *   [[runToZStream]]
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToChunk]]
    */
  def runToSet: Task[Set[A]] = run.toSet

  /** Runs query to a [[zio.Chunk]]
    *
    * NOTE: this method collects all data in memory, use it only for queries with limited capacity
    *
    * @see
    *   [[runToZStream]]
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToSet]]
    */
  def runToChunk: Task[Chunk[A]] = run.toChunk

  /** Runs query returning the first element
    *
    * @see
    *   [[runToZStream]]
    *   [[runToList]]
    *   [[runToSeq]]
    *   [[runToVector]]
    *   [[runToSet]]
    *   [[runToChunk]]
    */
  def runHead: Task[Option[A]]

  @deprecated("use runHead", "0.8.0")
  def first(): Task[Option[A]] = runHead
}
