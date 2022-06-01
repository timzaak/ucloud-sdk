package very.util.filestore

import com.timzaak.ucloud.UFileRequest
import com.timzaak.ucloud.UFileSDK

import scala.util.Try

class UCloudFileStorage(sdk: UFileSDK, isPrivate: Boolean = true, urlPre: String) extends FileStorage {
  override def getBytes(key: String): Try[Array[Byte]] = {
    Try { sdk.privateDownloadFile(key).body }
  }

  override def saveBytes(
      key: String,
      bytes: Array[Byte]
  ): Try[Unit] = {
    val mime = key.split('.').lastOption.collect {
      case "pdf"          => "application/pdf"
      case "png"          => "image/png"
      case "jpeg" | "jpg" => "image/jpeg"
    }
    Try {
      sdk.putFile(UFileRequest(key, content_type = mime.getOrElse("")), bytes)
      ()
    }
  }
  override def isExists(key: String): Boolean = sdk.getInfo(key).nonEmpty

  def getRemoteUrl(key: String): String =
    if (isPrivate) sdk.privateDownloadFileUrl(key, url = urlPre) else sdk.publicDownloadFileUrl(key, url = urlPre)

  def getUploadFileSignature(key: String, contentMD5: String, contentType: String) = {
    sdk.authorization(key, contentMD5 = contentMD5, contentType = contentType)
  }

}
