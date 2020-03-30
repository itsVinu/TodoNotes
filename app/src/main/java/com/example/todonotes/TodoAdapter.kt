package com.example.todonotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_todo.view.*
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(val todo: List<TodoModel>): RecyclerView.Adapter<TodoAdapter.ItemViewHolder>() {

//    var onItemClick: ((user: TodoModel) -> Unit)? = null
        var onItemClickListener:TodoItemClickListener? = null
    var trigger = MutableLiveData<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        )
    }

    override fun getItemCount() = todo.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(todo[position])
    }

    // so that while we swipe left or right app did not get crash and successfully operate delete and update task

    override fun getItemId(position: Int): Long {
        return todo[position].id
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(todoModel: TodoModel) {
            itemView.apply {
               // val colors = resources.getIntArray(R.array.random_array)

                txtShowTitle.text = todoModel.title
                txtShowTask.text = todoModel.description
                txtShowCategory.text = todoModel.category
                updateTime(todoModel.time)
                updateDate(todoModel.date)
                setOnClickListener{
//                    onItemClick?.invoke(todoModel)
                onItemClickListener?.onItemClick(todoModel)
                }
            }
        }
        private fun updateTime(time: Long) {
            val myFormat = "h:mm a"
            val sdf = SimpleDateFormat(myFormat)
            itemView.txtShowTime.text = sdf.format(Date(time))
        }


        private fun updateDate(time: Long) {
            val myFormat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myFormat)
            itemView.txtShowDate.text = sdf.format(Date(time))
        }
    }

}


interface TodoItemClickListener {
    fun onItemClick(list:TodoModel)
}
