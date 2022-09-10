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

# ZIO 1.x and ZIO 2.x support

ZIO 2.x is the main ZIO version supported by the library starting from the `0.6.0` version. 
ZIO 1.x will be supported for some time in `0.5.x` releases.