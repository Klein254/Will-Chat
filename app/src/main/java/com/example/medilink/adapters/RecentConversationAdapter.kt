package com.example.medilink.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medilink.adapters.RecentConversationAdapter.ConversationViewHolder
import com.example.medilink.databinding.ItemContainerRecentConversationBinding
import com.example.medilink.listeners.ConversionListener
import com.example.medilink.models.ChatMessage
import com.example.medilink.models.User

class RecentConversationAdapter(
    private val chatMessages: List<ChatMessage>,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<ConversationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder(
            ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class ConversationViewHolder(var binding: ItemContainerRecentConversationBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversationImage))
            binding.txtName.text = chatMessage.conversationName
            binding.txtRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener { v: View? ->
                val user = User()
                user.id = chatMessage.conversationId
                user.name = chatMessage.conversationName
                user.image = chatMessage.conversationImage
                conversionListener.onConversionClicker(user)
            }
        }
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}