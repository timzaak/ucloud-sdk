package very.util.filestore

import scala.util.Try

trait FileStorage {
  def getBytes(key: String): Try[Array[Byte]]
  def saveBytes(key: String, bytes: Array[Byte]): Try[Unit]

  def isExists(key:String):Boolean
}
