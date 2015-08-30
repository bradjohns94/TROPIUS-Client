package com.tropius

import java.net.URLEncoder

import akka.actor.ActorSystem
import spray.json._
import spray.http._
import spray.client.pipelining._

import scala.collection.mutable
import scala.concurrent._
import scala.sys.process._
import scalaj.http.Http
import duration._
import com.rockymadden.stringmetric.similarity.LevenshteinMetric

object CommandCenter {

    implicit val system = ActorSystem()
    import system.dispatcher

    def playtrack(trackName: String): JsValue = {
        // Send a search request to the spotify API and encode it into json
        val req = "https://api.spotify.com/v1/search?q=%s&type=track".format(trackName)
        val pipeline: HttpRequest =>   Future[HttpResponse] = sendReceive
        val response: Future[HttpResponse] = pipeline(Get(req))
        val result = Await.result(response, 10 seconds)
        var resJson = result.entity.data.asString.parseJson
        var ret = JsObject()
        try {
            // Get the ID of the first track to match our query
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
            // Play the actual track
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

    def compoundSearch(args: JsValue): JsValue = {
        val categories = List("track", "album", "artist")
        var ret = JsObject(
                            "ret" -> JsString("Failed to complete request"),
                            "err" -> JsBoolean(true)
                          )
        val fields = args.asJsObject.fields
        val parent = _getMostSpecific(fields).get
        if (!parent.equals("")) {
            val parentVal = URLEncoder.encode(fields.get(parent).get.prettyPrint.replace("\"", ""))
            // Get the spotify api JSON
            var req = "https://api.spotify.com/v1/search?q=%s&type=%s".format(parentVal, parent)
            val pipeline: HttpRequest =>   Future[HttpResponse] = sendReceive
            val response: Future[HttpResponse] = pipeline(Get(req))
            val result = Await.result(response, 10 seconds)
            var resJson = result.entity.data.asString.parseJson
            resJson = resJson.asJsObject.getFields("%ss".format(parent))(0)
            resJson = resJson.asJsObject.getFields("items")(0)
            // Compose result data into a map of {uri -> rating}
            val items = resJson.toString.parseJson.asInstanceOf[JsArray]
            var compoundMap = mutable.Map[String, Int]()
            for { item <- items.elements
                  cat <- categories } {
                val uri = item.asJsObject.getFields("uri")(0).toString.replace("\"", "")
                if (!compoundMap.contains(uri)) {
                    compoundMap += ( uri -> 0 )
                }
                val queried = fields.getOrElse(cat, "").toString // Return an empty string if we weren't given the category
                var nameJson = item
                if (cat.equals("artist")) {
                    nameJson = nameJson.asJsObject.getFields("artists")(0)
                    val artists = nameJson.toString.parseJson.asInstanceOf[JsArray]
                    nameJson = artists.elements(0) // TODO account for all artists
                } else if (cat.equals("album")) {
                    nameJson = nameJson.asJsObject.getFields(cat)(0)
                }
                val name = nameJson.asJsObject.getFields("name")(0).toString.replace("\"", "")
                val metric = LevenshteinMetric.compare(queried, name).getOrElse(0)
                val old = compoundMap.getOrElse(uri, 0)
                compoundMap(uri) = old + metric
            }
            // pick the best matching uri in the map and play it
            val sortedMap = compoundMap.toList sortBy {_._2}
            val best = sortedMap(0)._1
            // Actually play the uri
            val cmd = Seq("qdbus", "org.mpris.MediaPlayer2.spotify", "/",
                          "org.freedesktop.MediaPlayer2.OpenUri", best)
            cmd.lineStream
            ret = JsObject(
                            "val" -> JsString(best),
                            "err" -> JsBoolean(false)
                          )
        }
        ret
    }

    def _getMostSpecific(fields: Map[String, JsValue]): Option[String] = {
        var ret: Option[String] = None
        val categories = List("artist", "album", "track")
        categories.foreach { cat =>
            if (fields.contains(cat)) {
                ret = Some(cat)
            }
        }
        ret
    }

    def command(command: String): JsValue = {
        // Pause the currently playing spotify track
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
