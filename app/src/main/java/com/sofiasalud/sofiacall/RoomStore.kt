package com.sofiasalud.sofiacall

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.getValue
import org.json.JSONObject
import androidx.compose.runtime.mutableStateMapOf
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.google.firebase.ktx.options
import com.sofiasalud.callscreen.VCRoom
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val FIREBASE_PROJECT_NAME = Firebase.options.projectId
val FIREBASE_ENDPOINT = "https://us-central1-$FIREBASE_PROJECT_NAME.cloudfunctions.net"
val CREATE_ROOM_URL = "$FIREBASE_ENDPOINT/createVideoRoom"

class RoomStore(private val context: Context) : ViewModel() {
    private val ref = Firebase.database.getReference("test/rooms")
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val roomAdapter = moshi.adapter(VCRoom::class.java)

    var rooms: MutableMap<String, VCRoom> = mutableStateMapOf()

    init {
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snap: DataSnapshot, _prevId: String?) {
                snap.getValue<VCRoom>()?.let { room ->
                    rooms[room.id] = room
                }
            }

            override fun onChildChanged(snap: DataSnapshot, _prevId: String?) {
                snap.getValue<VCRoom>()?.let { room ->
                    rooms[room.id] = room
                }
            }

            override fun onChildRemoved(snap: DataSnapshot) {
                snap.getValue<VCRoom>()?.let { room ->
                    rooms.remove(room.id)
                }
            }

            override fun onChildMoved(snap: DataSnapshot, prevId: String?) {
//                Firebase.app.name
                snap.getValue<VCRoom>()?.let { room ->
                    rooms.remove(prevId)
                    rooms[room.id] = room
                }
            }

            override fun onCancelled(snap: DatabaseError) {
                Log.e("FirebaseStore", "onCancelled " + snap.message)
            }
        })
    }

    fun createRoom(roomName: String, createdBy: String, callback: (room: VCRoom) -> Unit) {
        Log.d("RoomStore", "createRoom")
        val requestBody: Map<*, *> = mapOf(
            "name" to roomName,
            "createdBy" to createdBy
        )

        Fuel.post(CREATE_ROOM_URL)
            .jsonBody(JSONObject(requestBody).toString())
            .responseObject(moshiDeserializerOf(roomAdapter)) { (room, error) ->
                if (error != null) {
                    Log.e("RoomStore", "Create room error: ${error.message}")
                } else if (room == null) {
                    Log.e("RoomStore", "Create room error (no room object in response)")
                } else {
                    rooms[room.id] = room
                    callback(room)
                }
            }
    }

    fun deleteRoom(id: String) {
        ref.child(id).removeValue()
    }

    fun joinRoom(roomId: String, name: String) {
        rooms[roomId]?.let { room ->
            if (room.needsPartner == null) {
                ref.child(room.id).updateChildren(mapOf("needsPartner" to name))
                room.needsPartner = name
            } else {
                ref.child(room.id).updateChildren(mapOf("needsPartner" to null))
                room.needsPartner = null
            }
        }
    }

    fun leaveRoom(roomId: String, name: String) {
        rooms[roomId]?.let { room ->
            if (room.needsPartner == name) {
                room.needsPartner = null
                ref.child(room.id).updateChildren(mapOf("needsPartner" to null))
            }
        }
    }
}