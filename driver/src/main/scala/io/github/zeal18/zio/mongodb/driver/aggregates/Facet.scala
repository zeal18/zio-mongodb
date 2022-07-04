package io.github.zeal18.zio.mongodb.driver.aggregates

final case class Facet(name: String, pipeline: Aggregation*)
