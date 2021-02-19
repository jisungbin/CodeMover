package me.sungbin.codemover

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import me.sungbin.androidutils.extensions.get
import me.sungbin.androidutils.extensions.toast
import me.sungbin.androidutils.util.PermissionUtil
import me.sungbin.androidutils.util.StorageUtil

class MainActivity : AppCompatActivity() {

    private lateinit var alert: AlertDialog
    private lateinit var adapter: DialogAdapter
    private val livedata: MutableLiveData<String> = MutableLiveData()
    private var lastCodeReceiveTime = 0L

    @SuppressLint("InflateParams")
    private fun init() {
        livedata.observe(this) { code ->
            val nowCodeReceiveTime = System.currentTimeMillis()
            if (lastCodeReceiveTime + 3000 >= nowCodeReceiveTime) return@observe
            lastCodeReceiveTime = nowCodeReceiveTime
            adapter.setOnFolderSelectedListener { path ->
                StorageUtil.save(path, code)
                alert.cancel()
                toast(getString(R.string.main_apply_code))
            }
            alert.show()
        }
        val view = layoutInflater.inflate(R.layout.layout_dialog, null)
        val recyclerView = view[R.id.rv_view, RecyclerView::class.java]
        adapter = DialogAdapter(recyclerView).apply { init() }
        recyclerView.adapter = adapter
        alert = AlertDialog.Builder(this@MainActivity).apply {
            setView(view)
            setPositiveButton(getString(R.string.close), null)
            setCancelable(false)
        }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PermissionUtil.request(
            this,
            getString(R.string.main_need_permission),
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        val reference = Firebase.database.reference
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val code = snapshot.getValue(String::class.java).toString()
                livedata.postValue(code)
                reference.removeValue()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
