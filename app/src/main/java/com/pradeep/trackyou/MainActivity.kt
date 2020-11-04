package com.pradeep.trackyou

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult


open class MainActivity : AppCompatActivity() {

    private lateinit var messageText: TextView

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val send = findViewById<Button>(R.id.send)
        val message = findViewById<EditText>(R.id.message)
        messageText = findViewById(R.id.message_text)
        val theChannel = "demo-animal-2"

        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = "sub-c-7af42346-1ea3-11eb-b558-be5397d4d556"
        pnConfiguration.publishKey = "pub-c-07797fac-70e7-4254-b301-c3228725944c"
        pnConfiguration.uuid =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val pubNub = PubNub(pnConfiguration)

        pubNub.setUUIDMetadata()
            .name("John Doe")
            .email("johndoe@pubnub.com")
            .async { _, status ->
                if (!status.isError) {
                    Log.i(TAG, "Success: ${status.uuid}")
                } else {
                    Log.i(TAG, "Error: ${status.errorData}")
                }
                runOnUiThread {
                    send.setOnClickListener {
                        val data = JsonObject()
                        data.addProperty("text", message.text.toString())
                        Log.i(TAG, "Message: ${message.text}")
                        pubNub.publish()
                            .message(data)
                            .channel(theChannel)
                            .async { result, status ->
                                if (!status.isError) {
                                    val timeToken = result?.timetoken
                                    Log.i(TAG, "Success: $timeToken")
                                } else {
                                    Log.i(TAG, "Error: ${status.statusCode}")
                                }
                            }
                    }
                }
            }

        pubNub.subscribe().channels(listOf(theChannel)).withPresence().execute()

        pubNub.addListener(object : SubscribeCallback() {
            override fun status(pubNub: PubNub, pnStatus: PNStatus) {
                displayMessage(pnStatus.origin)
            }

            override fun message(pubNub: PubNub, event: PNMessageResult) {
                val messagePayload = event.message

                val msg: JsonObject = messagePayload.asJsonObject
                val entryVal = msg["text"].asString

                displayMessage("[MESSAGE: received]: $entryVal")

                Log.i(TAG, messagePayload.toString())
            }

            override fun presence(pubNub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                Log.i(TAG, pnPresenceEventResult.uuid.toString())
            }

            override fun signal(pubNub: PubNub, pnSignalResult: PNSignalResult) {
                TODO("Not yet implemented")
            }

            override fun uuid(pubNub: PubNub, pnUUIDMetadataResult: PNUUIDMetadataResult) {
                TODO("Not yet implemented")
            }

            override fun channel(pubNub: PubNub, pnChannelMetadataResult: PNChannelMetadataResult) {
                TODO("Not yet implemented")
            }

            override fun membership(pubNub: PubNub, pnMembershipResult: PNMembershipResult) {
                TODO("Not yet implemented")
            }

            override fun messageAction(
                pubNub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
                TODO("Not yet implemented")
            }

            override fun file(pubNub: PubNub, pnFileEventResult: PNFileEventResult) {
                TODO("Not yet implemented")
            }
        })
    }

    protected fun displayMessage(aMessage: String?) {
        val newLine = "\n"
        val textBuilder: StringBuilder = StringBuilder()
            .append(aMessage)
            .append(newLine).append(newLine)
            .append(messageText.text.toString())
        runOnUiThread { messageText.text = textBuilder.toString() }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}