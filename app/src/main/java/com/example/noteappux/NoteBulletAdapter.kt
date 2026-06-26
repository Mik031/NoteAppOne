package com.example.noteappux

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class NoteBulletAdapter(
    private val bulletList: ArrayList<NoteBullet>,
    private val onBulletCompletedClick: (NoteBullet) -> Unit,
    private val onBulletTypeClick: (NoteBullet) -> Unit,
    private val onBulletTextClick: (NoteBullet) -> Unit,
    private val onDeleteBulletClick: (NoteBullet) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onBulletOrderChanged: (ArrayList<NoteBullet>) -> Unit
) : RecyclerView.Adapter<NoteBulletAdapter.NoteBulletViewHolder>() {

    class NoteBulletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bulletItemLayout: LinearLayout = itemView.findViewById(R.id.bulletItemLayout)
        val btnDragBullet: TextView = itemView.findViewById(R.id.btnDragBullet)
        val btnBulletCompleted: TextView = itemView.findViewById(R.id.btnBulletCompleted)
        val tvBulletSymbol: TextView = itemView.findViewById(R.id.tvBulletSymbol)
        val tvBulletText: TextView = itemView.findViewById(R.id.tvBulletText)
        val btnDeleteBullet: TextView = itemView.findViewById(R.id.btnDeleteBullet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteBulletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_bullet, parent, false)

        return NoteBulletViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteBulletViewHolder, position: Int) {
        val bullet = bulletList[position]

        if (bullet.isTask == 1) {
            holder.btnBulletCompleted.visibility = View.VISIBLE

            holder.btnBulletCompleted.text = if (bullet.isCompleted == 1) {
                "☑"
            } else {
                "☐"
            }
        } else {
            holder.btnBulletCompleted.visibility = View.GONE
        }

        holder.tvBulletSymbol.visibility = View.VISIBLE

        if (bullet.bulletType == DatabaseHelper.BULLET_TYPE_NONE) {
            holder.tvBulletSymbol.text = ""
        } else {
            holder.tvBulletSymbol.text = getBulletSymbol(position, bullet)
        }

        holder.tvBulletText.text = bullet.bulletText

        if (bullet.isTask == 1 && bullet.isCompleted == 1) {
            holder.tvBulletText.paintFlags =
                holder.tvBulletText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            holder.tvBulletText.alpha = 0.55f
            holder.tvBulletSymbol.alpha = 0.55f
        } else {
            holder.tvBulletText.paintFlags =
                holder.tvBulletText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.tvBulletText.alpha = 1.0f
            holder.tvBulletSymbol.alpha = 1.0f
        }

        holder.btnDragBullet.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onStartDrag(holder)
            }

            false
        }

        holder.btnBulletCompleted.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                val selectedBullet = bulletList[currentPosition]

                if (selectedBullet.isTask == 1) {
                    onBulletCompletedClick(selectedBullet)
                }
            }
        }

        holder.tvBulletSymbol.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                onBulletTypeClick(bulletList[currentPosition])
            }
        }
        

        holder.tvBulletText.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                onBulletTextClick(bulletList[currentPosition])
            }
        }

        holder.tvBulletText.setOnLongClickListener {
            val currentPosition = holder.bindingAdapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                onBulletTypeClick(bulletList[currentPosition])
            }

            true
        }

        holder.btnDeleteBullet.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteBulletClick(bulletList[currentPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return bulletList.size
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || toPosition < 0) {
            return
        }

        if (fromPosition >= bulletList.size || toPosition >= bulletList.size) {
            return
        }

        Collections.swap(bulletList, fromPosition, toPosition)

        notifyItemMoved(fromPosition, toPosition)
        notifyItemRangeChanged(0, bulletList.size)
    }

    fun finishMove() {
        onBulletOrderChanged(bulletList)
    }

    private fun getBulletSymbol(position: Int, bullet: NoteBullet): String {
        return when (bullet.bulletType) {
            DatabaseHelper.BULLET_TYPE_NONE -> ""
            DatabaseHelper.BULLET_TYPE_DASH -> "-"
            DatabaseHelper.BULLET_TYPE_NUMBER -> "${getNumberPosition(position)}."
            else -> "•"
        }
    }

    private fun getNumberPosition(position: Int): Int {
        var numberCount = 0

        for (i in 0..position) {
            if (bulletList[i].bulletType == DatabaseHelper.BULLET_TYPE_NUMBER) {
                numberCount++
            }
        }

        return numberCount
    }
}