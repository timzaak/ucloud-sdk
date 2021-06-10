package very.util.filestore

import java.io.{ FileNotFoundException, IOException }

import better.files._

import scala.util.{ Failure, Success, Try }

case class FileExisted(path: String) extends IOException

//FIXME: 1.不够健壮 2.错误相关没有处理 3. 覆盖问题没有解决
class LocalFileStorage(baseDir: String) extends FileStorage {
  private val fixBaseDir = if (baseDir.endsWith("/")) baseDir else s"$baseDir/"
  if (!File(fixBaseDir).exists) {
    throw new FileNotFoundException(s"LocalFile baseDir:$fixBaseDir do not exists")
  }
  override def getBytes(key: String): Try[Array[Byte]] = {
    Try { File(getPath(key)).byteArray }
  }
  override def saveBytes(
      key: String,
      bytes: Array[Byte]
  ): Try[Unit] = {
    val file = File(getPath(key))
    if (file.exists) {
      Failure(FileExisted(getPath(key)))
    } else {
      file.createIfNotExists()
      Success(file.writeByteArray(bytes))
    }

  }
  override def isExists(key: String): Boolean = File(getPath(key)).nonEmpty

  def getPath(key:String):String = s"$fixBaseDir$key"

}
