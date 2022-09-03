package io.github.zeal18.zio.mongodb.driver.aggregates

/** Defines a Facet for use in `\$facet` pipeline stages.
  */
final case class Facet(name: String, pipeline: Aggregation*)
