@file:Suppress("JavaCollectionsStaticMethod")

package com.example.medilink.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.example.medilink.adapters.ChatAdapter
import com.example.medilink.databinding.ActivityChatBinding
import com.example.medilink.models.ChatMessage
import com.example.medilink.models.User
import com.example.medilink.utilities.Constants
import com.example.medilink.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChatActivity : BaseActivity() {
    private var binding: ActivityChatBinding? = null
    private var receiveUser: User? = null
    private var chatMessageList: MutableList<ChatMessage>? = null
    private var chatAdapter: ChatAdapter? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    private var conversationId: String? = null
    private var isReceiverAvailable = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessageList = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessageList as ArrayList<ChatMessage>,
            getBitmapFromEncodedString(receiveUser!!.image),
            preferenceManager!!.getString(Constants.KEY_USER_ID)!!
        )
        binding!!.ChatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = HashMap<String, Any?>()
        message[Constants.KEY_SENDER_ID] = preferenceManager!!.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = receiveUser!!.id
        message[Constants.KEY_MESSAGE] = binding!!.edtMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database!!.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if (conversationId != null) {
            updateConversion(binding!!.edtMessage.text.toString())
        } else {
            val conversion = HashMap<String, Any?>()
            conversion[Constants.KEY_SENDER_ID] =
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            conversion[Constants.KEY_SENDER_NAME] =
                preferenceManager!!.getString(Constants.KEY_NAME)
            conversion[Constants.KEY_SENDER_IMAGE] =
                preferenceManager!!.getString(Constants.KEY_IMAGE)
            conversion[Constants.KEY_RECEIVER_ID] = receiveUser!!.id
            conversion[Constants.KEY_RECEIVER_NAME] = receiveUser!!.name
            conversion[Constants.KEY_RECEIVER_IMAGE] = receiveUser!!.image
            conversion[Constants.KEY_LAST_MESSAGE] =
                binding!!.edtMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Date()
            addConversion(conversion)
        }
        binding!!.edtMessage.text = null
    }

    private fun listenAvailabilityofReceiver() {
        database!!.collection(Constants.KEY_COLLECTION_USERS).document(
            receiveUser!!.id
        )
            .addSnapshotListener(this@ChatActivity) { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                        val availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                        )?.toInt()
                        isReceiverAvailable = availability == 1
                    }
                }
                if (isReceiverAvailable) {
                    binding!!.txtAvailability.visibility = View.VISIBLE
                } else {
                    binding!!.txtAvailability.visibility = View.GONE
                }
            }
    }

    private fun listenMessages() {
        database!!.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiveUser!!.id)
            .addSnapshotListener(eventListener)
        database!!.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiveUser!!.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                val count = chatMessageList!!.size
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants.KEY_SENDER_ID)
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_MESSAGE)
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP))
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        chatMessageList!!.add(chatMessage)
                    }
                }
                Collections.sort(chatMessageList) { obj1: ChatMessage, obj2: ChatMessage ->
                    obj1.dateObject.compareTo(
                        obj2.dateObject
                    )
                }
                if (count == 0) {
                    chatAdapter!!.notifyDataSetChanged()
                } else {
                    chatAdapter!!.notifyItemRangeInserted(
                        chatMessageList!!.size,
                        chatMessageList!!.size
                    )
                    binding!!.ChatRecyclerView.smoothScrollToPosition(chatMessageList!!.size - 1)
                }
                binding!!.ChatRecyclerView.visibility = View.VISIBLE
            }
            binding!!.progressBar.visibility = View.GONE
            if (conversationId == null) {
                checkForConversion()
            }
        }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiveUser = intent.getSerializableExtra(Constants.KEY_USER) as User?
        binding!!.txtName.text = receiveUser!!.name
    }

    private fun setListeners() {
        binding!!.imageBack.setOnClickListener { v: View? -> onBackPressed() }
        binding!!.layoutSend.setOnClickListener { v: View? -> sendMessage() }
    }

    private fun getReadableDateTime(date: Date?): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any?>) {
        database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { documentReference: DocumentReference ->
                conversationId = documentReference.id
            }
    }

    private fun updateConversion(message: String) {
        val documentReference =
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
                conversationId!!
            )
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversion() {
        if (chatMessageList!!.size != 0) {
            checkForConversionRemotely(
                preferenceManager!!.getString(Constants.KEY_USER_ID),
                receiveUser!!.id
            )
            checkForConversionRemotely(
                receiveUser!!.id,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String?, receiverId: String?) {
        database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener = OnCompleteListener { task: Task<QuerySnapshot?> ->
        if (task.isSuccessful && task.result != null && task.result!!
                .documents.size > 0
        ) {
            val documentSnapshot = task.result!!.documents[0]
            conversationId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityofReceiver()
    }
}