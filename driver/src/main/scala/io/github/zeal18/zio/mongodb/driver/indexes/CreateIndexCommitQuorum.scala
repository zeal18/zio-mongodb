package io.github.zeal18.zio.mongodb.driver.indexes

import com.mongodb.CreateIndexCommitQuorum as JCreateIndexCommitQuorum

/** The minimum number of data-bearing voting replica set members (i.e. commit quorum),
  * including the primary, that must report a successful index build before the primary marks the indexes as ready.
  *  A "voting" member is any replica set member where `members[n].votes` is greater than `0`.
  *
  * @see [[https://www.mongodb.com/docs/manual/reference/command/createIndexes/#command-fields Create Index]]
  */
sealed trait CreateIndexCommitQuorum { self =>
  private[driver] def toJava: JCreateIndexCommitQuorum = self match {
    case CreateIndexCommitQuorum.WotingMembers             => JCreateIndexCommitQuorum.VOTING_MEMBERS
    case CreateIndexCommitQuorum.Majority                  => JCreateIndexCommitQuorum.MAJORITY
    case CreateIndexCommitQuorum.Disabled                  => JCreateIndexCommitQuorum.create(0)
    case CreateIndexCommitQuorum.ReplicaSetTag(name)       => JCreateIndexCommitQuorum.create(name)
    case CreateIndexCommitQuorum.ReplicaSetMembers(amount) =>
      JCreateIndexCommitQuorum.create(amount)
  }
}

object CreateIndexCommitQuorum {

  /** All data-bearing voting replica set members (Default)
    */
  case object WotingMembers extends CreateIndexCommitQuorum

  /** A simple majority of data-bearing voting replica set members
    */
  case object Majority extends CreateIndexCommitQuorum

  /** A specific number of data-bearing voting replica set members.
    */
  final case class ReplicaSetMembers(amount: Int) extends CreateIndexCommitQuorum

  /** Disables quorum-voting behavior.
    *
    * Members start the index build simultaneously but do not vote or wait for quorum before completing the index build.
    * If you start an index build with the disabled commit quorum, you cannot later modify the commit quorum using `setIndexCommitQuorum`.
    */
  case object Disabled extends CreateIndexCommitQuorum

  /** A replica set tag name.
    */
  final case class ReplicaSetTag(name: String) extends CreateIndexCommitQuorum

  def apply(replicaSetAmount: Int): CreateIndexCommitQuorum =
    ReplicaSetMembers(replicaSetAmount)
  def apply(replicaSetTag: String): CreateIndexCommitQuorum =
    ReplicaSetTag(replicaSetTag)

  val default: CreateIndexCommitQuorum = WotingMembers
}
