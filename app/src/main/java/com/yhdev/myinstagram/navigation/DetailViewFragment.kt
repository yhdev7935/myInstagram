package com.yhdev.myinstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yhdev.myinstagram.R
import com.yhdev.myinstagram.navigation.model.AlarmDTO
import com.yhdev.myinstagram.navigation.model.ContentDTO

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).adapter =
            DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager =
            LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    var _contentDTOs: ArrayList<ContentDTO> = arrayListOf()
                    var _contentUidList: ArrayList<String> = arrayListOf()
                    if(querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        _contentDTOs.add(item!!)
                        _contentUidList.add(snapshot.id)
                    }

                    contentDTOs.addAll(_contentDTOs)
                    contentUidList.addAll(_contentUidList)
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            // UserId
            viewHolder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text =
                contentDTOs!![position].userId

            // Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewHolder.findViewById(R.id.detailviewitem_imageview_content))

            // Explain of Content
            viewHolder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text =
                contentDTOs!![position].explain

            // Likes
            viewHolder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text =
                "Likes " + contentDTOs!![position].favoriteCount

            // Profile Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewHolder.findViewById(R.id.detailviewitem_profile_image))

            // Button Click Event
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                .setOnClickListener {
                    clickFavorite(position)
                }

            if (contentDTOs!![position].favorites.contains(uid)) {
                // This is like status
                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite)
            } else {
                // This is unlike status
                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite_border)
            }

            // This code is when the profile image is clicked
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_profile_image)
                .setOnClickListener {
                    var fragment = UserFragment()
                    var bundle = Bundle()
                    bundle.putString("destinationUid", contentDTOs[position].uid)
                    bundle.putString("userId", contentDTOs[position].userId)
                    fragment.arguments = bundle
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.main_content, fragment)?.commit()
                }
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_comment_imageview)
                .setOnClickListener { v ->
                    var intent = Intent(v.context, CommentActivity::class.java)
                    intent.putExtra("contentUid", contentUidList[position])
                    intent.putExtra("destinationUid", contentDTOs[position].uid)
                    startActivity(intent)
                }
        }

        fun clickFavorite(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    // When the Button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else {
                    // When the Button isn't clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid : String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}
