package very.util.filestore

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.{Random, Try}

class MixedFileStorage(prefix: String, localFileStorage: LocalFileStorage, uCloudFileStorage: UCloudFileStorage) {
  private val fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss")

  protected def generateKey(): String = {
    if (prefix.isEmpty) {
      s"${DateTime.now.toString(fmt)}-${Random.nextInt(99999)}"
    } else {
      s"$prefix/${DateTime.now.toString(fmt)}-${Random.nextInt(99999)}"
    }
  }

  def saveLocalFile(data: Array[Byte]) = {
    val key = generateKey
    localFileStorage.saveBytes(key, data).map { _ =>
      key
    }
  }
  def saveRemoteFile(data: Array[Byte]) = {
    val key = generateKey
    uCloudFileStorage.saveBytes(key, data).map { _ =>
      key
    }
  }
  def saveFile(data: Array[Byte]) = {
    val key = generateKey
    localFileStorage
      .saveBytes(key, data)
      .flatMap { _ =>
        uCloudFileStorage.saveBytes(key, data)
      }
      .map(_ => key)
  }
  def getLocalFile(key: String): Try[Array[Byte]] = localFileStorage.getBytes(key)

  def getRemoteFile(key: String): Try[Array[Byte]] = uCloudFileStorage.getBytes(key)

  def getRemoteUrl(key: String) = uCloudFileStorage.getRemoteUrl(key)

  def existLocal(key: String) = localFileStorage.isExists(key)

  def getLocalPath(key: String) = localFileStorage.getPath(key)

}
