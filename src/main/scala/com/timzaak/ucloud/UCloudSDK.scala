package com.timzaak.ucloud

import com.tizaak.ucloud.codec.HmacSHA1
import scalaj.http.Http
import ws.very.util.json.JsonHelperWithDoubleMode
import ws.very.util.security.SHA

case class UFileRequest(

                         key: String,
                         http_method: String = "POST",
                         content_md5: String = "",
                         content_type: String = "",
                         date: String = "",
                         put_policy: String = ""
                       )

trait UCloudSDK extends JsonHelperWithDoubleMode {

  val baseUrl = "https://api.ucloud.cn/"

  def publicKey: String

  def privateKey: String

  def projectId: String

  protected def signature(param: Map[String, String]) = {
    val str = param.toSeq.sortBy(_._1).foldLeft("")((r, z) => r + z._1 + z._2) + privateKey
    SHA(str, SHA.SHA_1)
  }

  def get(param: Map[String, String]) = {
    val withProjectParam = param + ("ProjectId" -> projectId) + ("PublicKey" -> publicKey)
    Http(baseUrl).params(withProjectParam + ("Signature" -> signature(withProjectParam))).asString
  }

  def post(param: Map[String, String]) = {
    val withProjectParam = param + ("ProjectId" -> projectId) + ("PublicKey" -> publicKey)
    val jsonStr = toJson(withProjectParam + ("Signature" -> signature(withProjectParam)))
    Http(baseUrl).postData(jsonStr).asString
  }

  class UFile(bucket: String) {
    private def authorization(req:UFileRequest) = {
      import req._
      val canonicalizedResource = s"""\$bucket\$key"""
      val strTosig = s"$http_method\n$content_md5\n$content_type\n$date\n$canonicalizedResource"
      val signature = new HmacSHA1().sign(privateKey, strTosig)
      s"Ucloud $publicKey:$signature"
    }
  }

}
