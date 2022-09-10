package io.github.zeal18.zio.mongodb.driver.indexes

import com.mongodb.client.model.IndexModel

final case class Index(key: IndexKey, options: IndexOptions) {
  private[driver] def toJava: IndexModel = new IndexModel(key, options.toJava)
}

object Index {
  def apply(key: IndexKey): Index = Index(key, IndexOptions())
}
