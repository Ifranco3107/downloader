package com.macropay.downloader.ui.common.mensajes

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.macropay.downloader.ui.Alertas.AlertasActivity
import android.widget.Toast
import android.view.Gravity
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.macropay.downloader.R
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.lang.Exception

object ToastDPC : Activity() {
    private const val TAG = "ToastDPC"
    fun toastPolicyRestriction(context: Context?, titulo: String?, mensaje: String?) {
        //[metodo 2]
/*            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
          // View view = inflater.inflate( R.layout.toast, null );

            //[metodo 1]
            //LayoutInflater inflater = LayoutInflater.from(context);
            ToastBinding binding = ToastBinding.inflate(inflater);

            // ToastBinding binding = ToastBinding.inflate(inflater. getLayoutInflater());
            View view = binding.getRoot();

            binding.text.setText(Mensaje);
            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(view);
            toast.show();*/
    }

    fun showPolicyRestriction(context: Context, titulo: String?, mensaje: String) {
        var mensaje = mensaje
        Log.msg(TAG, "showPolicyRestriction")
        if (mensaje.isEmpty()) mensaje = "Acción no permitida!\nSi tienes alguna pregunta,\ncomunicate con tu administrador de TI."
        Log.msg(TAG, "msg: $mensaje")
        try {
            val alertIntent = Intent(context, AlertasActivity::class.java)
            alertIntent.putExtra("titulo", titulo)
            alertIntent.putExtra("mensaje", mensaje)
            alertIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(alertIntent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showPolicyRestriction", ex.message)
        }
    }

    @JvmStatic
    fun showToast(context: Context?, Mensaje: String?) {
        try {

/*           var  handler = Handler(Looper.getMainLooper())
            Looper.prepare()

            handler.post {*/
                val toast = Toast.makeText(context, Mensaje, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
 /*           }

            Looper.loop()*/
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showToast: Mensaje: \$Mensaje", ex.message)
        }
    }

    fun showSnackBar(context: Context?, view: View, mensaje: String, tipoMsg: Int) {
        var ColorBack = 0
        ColorBack = when (tipoMsg) {
            1 -> {
                context!!.resources.getColor(R.color.color_success)
            }
            2 -> {
                context!!.resources.getColor(R.color.color_info)
            }
            3 -> {
                context!!.resources.getColor(R.color.color_alert)
            }
            else -> {
                context!!.resources.getColor(R.color.color_info)
            }
        }
        try {
            val snackbar = Snackbar.make(view, mensaje, 3000)
            val snackBarView = snackbar.view
            snackBarView.setBackgroundColor(ColorBack)
            //snackBarView.findViewById(android.support.design.R.id.snackbar_text).setTextColor(textColor)
            snackbar.show()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "", ex.message)
        }
    }
}