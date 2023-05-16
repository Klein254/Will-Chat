package com.example.medilink.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medilink.adapters.UsersAdapter.UserViewHolder
import com.example.medilink.databinding.ItemContainerUserBinding
import com.example.medilink.listeners.UserListener
import com.example.medilink.models.User

class UsersAdapter(private val users: List<User>, private val userListener: UserListener) :
    RecyclerView.Adapter<UserViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(var binding: ItemContainerUserBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setUserData(user: User) {
            binding.txtName.text = user.name
            binding.txtEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener { v: View? -> userListener.onUserClicked(user) }
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}