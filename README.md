# zio-mongodb
One more ZIO wrapper around the official MongoDB Java driver but better ;)

The main goals of the project:
* the first class ZIO support
* incapsulate Java driver runtime codecs resolution by a compile time solution
* Scala 3 support
* magnolia derived codecs support
* split query building and results processing
* type-safe query building DSL
* provide zio-test integrated test-kit