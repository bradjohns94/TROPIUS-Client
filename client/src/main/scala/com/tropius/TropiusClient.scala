package com.tropius

import akka.actor.Actor
import spray.routing._ 
import spray.http._ 
import spray.json._
import MediaTypes._

import scala.sys.process._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class TropiusClientActor extends Actor with TropiusClient {

    // the HttpService trait defines only one abstract member, which // connects the services environment to the enclosing actor or test
    def actorRefFactory = context

    // this actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait TropiusClient extends HttpService {
    
    val myRoute =
        // Simple test route
        path("") {
            get {
                //respondWithMediaType(`text/plain`) {
                complete {
                    <h1>Welcome To The TROPIUS Client!</h1>
                }
                //}
            }
        } ~
        // Play the top song result off spotify
        path("TROPIUS" / "spotify" / "play" / "song" / Rest) { title =>
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.playSong(title).prettyPrint
                    }
                }
            }
        } ~
        // Play the top album result off spotify
        path("TROPIUS" / "spotify" / "play" / "album" / Rest) { title =>
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.playAlbum(title).prettyPrint
                    }
                }
            }
        } ~
        // Play the top artist result off spotify
        path("TROPIUS" / "spotify" / "play" / "artist" / Rest) { title =>
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.playArtist(title).prettyPrint
                    }
                }
            }
        } ~
        // Pause the currently playing spotify song
        path("TROPIUS" / "spotify" / "pause") {
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.command("Pause").prettyPrint
                    }
                }
            }
        } ~
        // Pause the currently playing spotify song
        path("TROPIUS" / "spotify" / "play") {
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.command("PlayPause").prettyPrint
                    }
                }
            }
        } ~
        // Pause the currently playing spotify song
        path("TROPIUS" / "spotify" / "next") {
            get {
                respondWithMediaType(`application/json`) {
                    complete {
                        CommandCenter.command("Next").prettyPrint
                    }
                }
            }
        }
}
