package me.sungbin.codemover

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sungbin.androidutils.extensions.get
import com.sungbin.androidutils.util.PermissionUtil
import com.sungbin.androidutils.util.StorageUtil

class MainActivity : AppCompatActivity() {

    private lateinit var alert: AlertDialog
    private lateinit var adapter: DialogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionUtil.request(this,
            getString(R.string.main_need_permission),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE))

        val database = Firebase.database
        database.reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val value = snapshot.getValue(String::class.java).toString()

                val view = layoutInflater.inflate(R.layout.layout_dialog, null)
                val recyclerView = view[R.id.rv_view, RecyclerView::class.java]
                adapter = DialogAdapter(recyclerView)
                adapter.init()
                adapter.setOnFolderSelectedListener {
                    StorageUtil.save(it, value)
                    alert.cancel()
                }
                recyclerView.adapter = adapter
                val dialog = AlertDialog.Builder(this@MainActivity)
                dialog.setTitle(getString(R.string.dialog_choose_file))
                dialog.setView(view)
                alert = dialog.create()
                alert.show()

                database.reference.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}

        })

    }

}