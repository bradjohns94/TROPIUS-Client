package com.tropius

import akka.actor.ActorSystem
import spray.json._
import spray.http._
import spray.client.pipelining._

import scala.concurrent._
import duration._
import scala.sys.process._
import scalaj.http.Http

object CommandCenter {

    implicit val system = ActorSystem()
    import system.dispatcher

    def playSong(songName: String): JsValue = {
        // Send a search request to the spotify API and encode it into json
        val req = "https://api.spotify.com/v1/search?q=%s&type=track".format(songName)
        val pipeline: HttpRequest =>   Future[HttpResponse] = sendReceive
        val response: Future[HttpResponse] = pipeline(Get(req))
        val result = Await.result(response, 10 seconds)
        var resJson = result.entity.data.asString.parseJson
        var ret = JsObject()
        try {
            // Get the ID of the first song to match our query
            resJson = resJson.asJsObject.getFields("tracks")(0)
            resJson = resJson.asJsObject.getFields("items")(0)
            val items = resJson.toString.parseJson.asInstanceOf[JsArray]
            resJson = items.elements(0)
            val uri = resJson.asJsObject.getFields("uri")(0).toString.replace("\"", "")
            // Put the ID as our return value
            ret = JsObject(
                            "val" -> JsString(uri),
                            "err" -> JsBoolean(false)
                          )
            // Play the actual song
            val cmd = Seq("qdbus", "org.mpris.MediaPlayer2.spotify", "/",
                          "org.freedesktop.MediaPlayer2.OpenUri", uri)
            cmd.lineStream
        } catch {
            case e: Throwable  => ret = JsObject(
                                    "val" -> JsString("Failed to get track"),
                                    "err" -> JsBoolean(true)
                                  )
        }
        ret
    }

    def playAlbum(albumName: String): JsValue = {
        // Send a search request to the spotify API and encode it into json
        val req = "https://api.spotify.com/v1/search?q=%s&type=album".format(albumName)
        val pipeline: HttpRequest =>   Future[HttpResponse] = sendReceive
        val response: Future[HttpResponse] = pipeline(Get(req))
        val result = Await.result(response, 10 seconds)
        var resJson = result.entity.data.asString.parseJson
        var ret = JsObject()
        try {
            // Get the ID of the first album to match our query
            resJson = resJson.asJsObject.getFields("albums")(0)
            resJson = resJson.asJsObject.getFields("items")(0)
            val items = resJson.toString.parseJson.asInstanceOf[JsArray]
            resJson = items.elements(0)
            val uri = resJson.asJsObject.getFields("uri")(0).toString.replace("\"", "")
            // Put the ID as our return value
            ret = JsObject(
                            "val" -> JsString(uri),
                            "err" -> JsBoolean(false)
                          )
            // Play the actual album
            val cmd = Seq("qdbus", "org.mpris.MediaPlayer2.spotify", "/",
                          "org.freedesktop.MediaPlayer2.OpenUri", uri)
            cmd.lineStream
        } catch {
            case e: Throwable  => ret = JsObject(
                                    "val" -> JsString("Failed to get album"),
                                    "err" -> JsBoolean(true)
                                  )
        }
        ret
    }

    def playArtist(artistName: String): JsValue = {
        // Send a search request to the spotify API and encode it into json
        val req = "https://api.spotify.com/v1/search?q=%s&type=artist".format(artistName)
        val pipeline: HttpRequest =>   Future[HttpResponse] = sendReceive
        val response: Future[HttpResponse] = pipeline(Get(req))
        val result = Await.result(response, 10 seconds)
        var resJson = result.entity.data.asString.parseJson
        var ret = JsObject()
        try {
            // Get the ID of the first artist to match our query
            resJson = resJson.asJsObject.getFields("artists")(0)
            resJson = resJson.asJsObject.getFields("items")(0)
            val items = resJson.toString.parseJson.asInstanceOf[JsArray]
            resJson = items.elements(0)
            val uri = resJson.asJsObject.getFields("uri")(0).toString.replace("\"", "")
            // Put the ID as our return value
            ret = JsObject(
                            "val" -> JsString(uri),
                            "err" -> JsBoolean(false)
                          )
            // Play the actual artist
            val cmd = Seq("qdbus", "org.mpris.MediaPlayer2.spotify", "/",
                          "org.freedesktop.MediaPlayer2.OpenUri", uri)
            cmd.lineStream
        } catch {
            case e: Throwable  => ret = JsObject(
                                    "val" -> JsString("Failed to get artist"),
                                    "err" -> JsBoolean(true)
                                  )
        }
        ret
    }

    def command(command: String): JsValue = {
        // Pause the currently playing spotify song
        val options = Seq("Next", "PlayPause", "Pause")
        var ret = JsObject()
        if (options.contains(command)) {
            val cmd = Seq("dbus-send", "--print-reply",
                          "--dest=org.mpris.MediaPlayer2.spotify",
                          "/org/mpris/MediaPlayer2",
                          "org.mpris.MediaPlayer2.Player.%s".format(command))
            cmd.lineStream
            ret = JsObject(
                            "msg" -> JsString(command),
                            "err" -> JsBoolean(false)
                          )
        } else {
            ret = JsObject(
                            "msg" -> JsString("Invalid command"),
                            "err" -> JsBoolean(true)
                          )
        }
        ret
    }
}
