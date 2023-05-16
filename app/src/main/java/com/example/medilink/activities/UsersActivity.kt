package com.example.medilink.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.medilink.adapters.UsersAdapter
import com.example.medilink.databinding.ActivityUsersBinding
import com.example.medilink.listeners.UserListener
import com.example.medilink.models.User
import com.example.medilink.utilities.Constants
import com.example.medilink.utilities.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UsersActivity : BaseActivity(), UserListener {
    private var binding: ActivityUsersBinding? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        users
    }

    private fun setListeners() {
        binding!!.imageBack.setOnClickListener { v: View? -> onBackPressed() }
    }

    private val users: Unit
        private get() {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    loading(false)
                    val currentUserId = preferenceManager!!.getString(Constants.KEY_USER_ID)
                    if (task.isSuccessful && task.result != null) {
                        val users: MutableList<User> = ArrayList()
                        for (queryDocumentSnapshot in task.result!!) {
                            if (currentUserId == queryDocumentSnapshot.id) {
                                continue
                            }
                            val user = User()
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                            user.id = queryDocumentSnapshot.id
                            users.add(user)
                        }
                        if (users.size > 0) {
                            val usersAdapter = UsersAdapter(users, this)
                            binding!!.usersRecyclerView.adapter = usersAdapter
                            binding!!.usersRecyclerView.visibility = View.VISIBLE
                        } else {
                            showErrorMessage()
                        }
                    } else {
                        showErrorMessage()
                    }
                }
        }

    private fun showErrorMessage() {
        binding!!.txtErrorMessage.text = String.format("%s", "No user available")
        binding!!.txtErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isloading: Boolean) {
        if (isloading) {
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}