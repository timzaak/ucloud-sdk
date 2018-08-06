package com.timzaak.ucloud

import scalaj.http.Http
import ws.very.util.json.JsonHelperWithDoubleMode
import ws.very.util.security.SHA

trait UCloudSDK extends JsonHelperWithDoubleMode {

  val baseUrl = "https://api.ucloud.cn/"

  val publicKey: String

  val privateKey: String

  val projectId: String

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
}
