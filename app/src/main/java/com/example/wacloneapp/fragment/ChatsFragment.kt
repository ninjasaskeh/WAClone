package com.example.wacloneapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wacloneapp.activity.ConversationActivity
import com.example.wacloneapp.R
import com.example.wacloneapp.listener.ChatClickListener
import com.example.wacloneapp.adapter.ChatsAdapter
import com.example.wacloneapp.listener.FailureCallback
import com.example.wacloneapp.util.Chat
import com.example.wacloneapp.util.DATA_CHATS
import com.example.wacloneapp.util.DATA_USERS
import com.example.wacloneapp.util.DATA_USER_CHATS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment(),
    ChatClickListener {

    private var chatAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (userId.isNullOrEmpty()){
            failureCallback?.onUserError()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatAdapter.setOnClickItemListener(this)
        rv_chats.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        firebaseDb.collection(DATA_USERS).document(userId!!)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    refreshChats()
                }
            }
    }

    private fun refreshChats() {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {
                    val partners = it[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatAdapter.updateChats(chats)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onChatClicked(
        chatId: String?,
        otherUserId: String?,
        chatsImageUrl: String?,
        chatsName: String?
    ) {
        startActivity(ConversationActivity.newIntent(context, chatId, chatsImageUrl, otherUserId, chatsName))

    }

    fun newChat(partnerId: String) {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { userDocument->
                val userChatPartner = hashMapOf<String, String>()
                if (userDocument[DATA_USER_CHATS]!= null&&
                    userDocument[DATA_USER_CHATS]is HashMap<*,*>
                ){
                    val userDocumenMap = userDocument[DATA_USER_CHATS] as HashMap<String, String>
                    if (userDocumenMap.containsKey(partnerId)) {
                        return@addOnSuccessListener
                    } else {
                        userChatPartner.putAll(userDocumenMap)
                    }
                }
                firebaseDb.collection(DATA_USERS).document(partnerId).get()
                    .addOnSuccessListener { partnertDocument->
                        val partnerChatPartners = hashMapOf<String, String>()
                        if (partnertDocument[DATA_USERS] != null && partnertDocument[DATA_USER_CHATS] is HashMap<*, *>){
                            val parnerDocumentMap = partnertDocument[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(parnerDocumentMap)
                        }

                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDb.collection(DATA_CHATS).document()
                        val userRef = firebaseDb.collection(DATA_USERS).document(userId)
                        val partnerRef = firebaseDb.collection(DATA_USERS).document(partnerId)

                        userChatPartner[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDb.batch()
                        batch.set(chatRef, chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatPartner)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun setFailureCallbackListner(listener: FailureCallback) {
        failureCallback = listener

    }


}