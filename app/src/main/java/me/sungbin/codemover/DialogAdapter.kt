package me.sungbin.codemover

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sungbin.androidutils.extensions.get
import com.sungbin.androidutils.util.StorageUtil
import java.io.File

/**
 * Created by root on 9/7/17.
 */

class DialogAdapter(private val view: RecyclerView) :
    RecyclerView.Adapter<DialogAdapter.ContentListViewHolder>() {

    interface OnPathSelectedListener {
        fun onPathSelected(path: String)
    }

    private lateinit var onPathSelectedListener: OnPathSelectedListener

    private val defaultPath = "${StorageUtil.sdcard}/loco"
    private var contentList = ArrayList<File?>()
    private val context = view.context

    fun setOnFolderSelectedListener(onFolderSelectedListener: (String) -> Unit) {
        this.onPathSelectedListener = object : OnPathSelectedListener {
            override fun onPathSelected(path: String) {
                onFolderSelectedListener(path)
            }
        }
    }

    fun init() {
        contentListener(File(defaultPath))
    }

    fun goToBackPath() {
        val path = File(contentList[1]?.parent ?: defaultPath).parent!!
        contentListener(File(path))
    }

    private fun contentListener(path: File) {
        contentList.clear()
        contentList.add(null)
        contentList.addAll(path.listFiles() ?: return)
        notifyDataSetChanged()
        view.scrollToPosition(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContentListViewHolder(View.inflate(context, R.layout.layout_dialog_item, null))

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(viewHolder: ContentListViewHolder, position: Int) {
        val content = contentList[position]
        if (content == null) {
            viewHolder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_arrow_back_24))
            viewHolder.name.text = context.getString(R.string.dialog_goto_back_path)
        } else {
            if (content.isDirectory) {
                viewHolder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_folder_24))
            } else {
                viewHolder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_insert_drive_file_24))
            }
            viewHolder.name.text = content.name
        }
    }

    inner class ContentListViewHolder(view: View) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {

        val name = view[R.id.tv_name, TextView::class.java]
        val icon = view[R.id.iv_icon, ImageView::class.java]

        init {
            view[R.id.ll_main_container, LinearLayout::class.java].setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val content = contentList[adapterPosition]
            if (content == null) {
                goToBackPath()
            } else {
                if (content.isDirectory) {
                    contentListener(content)
                } else {
                    onPathSelectedListener.onPathSelected(content.path)
                }
            }
        }
    }

    override fun getItemCount() = contentList.size
    override fun getItemId(position: Int) = position.toLong()
    override fun getItemViewType(position: Int) = position
}
