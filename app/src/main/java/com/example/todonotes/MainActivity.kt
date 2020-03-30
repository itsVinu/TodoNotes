package com.example.todonotes

import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    val list = arrayListOf<TodoModel>()

    //To build the database

    val db by lazy {
        AppDatabase.getDatabase(this)
    }
    val todoadapter = TodoAdapter(list)

    private val onItemClicked = object :TodoItemClickListener{
        override fun onItemClick(list: TodoModel) {
            Toast.makeText(this@MainActivity, "$list", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        todoadapter.onItemClickListener = onItemClicked

        floatingBtnAdd.setOnClickListener {
            val i = Intent(this@MainActivity, TaskActivity::class.java)
            startActivity(i)
        }

        db.todoDao().getTask().observe(this,Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                todoadapter.notifyDataSetChanged()
            } else {
                list.clear()
                todoadapter.notifyDataSetChanged()
            }
        })

        todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = todoadapter
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(todoRv)
    }

    val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP
                or ItemTouchHelper.DOWN
                or ItemTouchHelper.END
                or ItemTouchHelper.START,
        ItemTouchHelper.RIGHT
                or ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            Collections.swap(list, fromPosition, toPosition)
            todoRv.adapter?.notifyItemMoved(fromPosition, toPosition)
            todoRv.adapter?.notifyDataSetChanged()
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            list.removeAt(viewHolder.adapterPosition)
//            adapter.notifyDataSetChanged()

            val position = viewHolder.adapterPosition

            if (direction == ItemTouchHelper.LEFT) {
                GlobalScope.launch(Dispatchers.IO) {
                    db.todoDao().deleteTask(todoadapter.getItemId(position))
                }
            } else if (direction == ItemTouchHelper.RIGHT) {
                GlobalScope.launch(Dispatchers.IO) {
                    db.todoDao().finishTask(todoadapter.getItemId(position))
                }
            }
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView

                val paint = Paint()
                val icon: Bitmap

                if (dX > 0) {

                    icon = BitmapFactory.decodeResource(resources, R.mipmap.check_mark)

                    paint.color = Color.parseColor("#388E3C")

                    canvas.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                    )

                    canvas.drawBitmap(
                        icon,
                        itemView.left.toFloat(),
                        itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                        paint
                    )


                } else {
                    icon = BitmapFactory.decodeResource(resources, R.mipmap.delete)

                    paint.color = Color.parseColor("#D32F2F")

                    canvas.drawRect(
                        itemView.right.toFloat() + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                    )

                    canvas.drawBitmap(
                        icon,
                        itemView.right.toFloat() - icon.width,
                        itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                        paint
                    )
                }
                viewHolder.itemView.translationX = dX


            } else {
                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }

        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    displayTodo(newText)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun displayTodo(newText: String = "") {
        db.todoDao().getTask().observe(this,Observer {
            if (it.isNotEmpty()) {
                list.clear()
                list.addAll(
                    it.filter { todo ->
                        todo.title.contains(newText, true)
                    }
                )
                todoadapter.notifyDataSetChanged()
            }
        })

    }
}
