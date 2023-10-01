package com.macropay.downloader.ui.Alertas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.macropay.downloader.databinding.ActivityAlertasBinding


class AlertasActivity : AppCompatActivity() {
    var bind: ActivityAlertasBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAlertasBinding.inflate(layoutInflater)
        val view: View = bind!!.root
        setContentView(view)

        val mensaje = intent.extras
        val titulo = mensaje!!.getString("titulo")
        val msg = mensaje.getString("mensaje")
       // val isTimer = mensaje.getBoolean("isTimer",false)
        bind!!.txtTitulo.text = titulo
        bind!!.txtMensaje.text = msg

        val handler = Handler(Looper.getMainLooper())
        //bind!!.btnAAceptar.visibility = View.GONE
       // if(isTimer)
            handler.postDelayed({  this.finish()}, 15_000)
       // else
       //     bind!!.btnAAceptar.visibility = View.VISIBLE
        bind!!.txtTitulo.text = titulo
    }

    fun salirAlert(v:View){
        this.finish();
    }

}