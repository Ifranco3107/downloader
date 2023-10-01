package com.macropay.utils.phone

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.macropay.utils.Settings
import com.macropay.utils.Utils
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.broadcast.Sender.sendSIMStatus
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.logs.Log.msg
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko


//import android.os. //.SystemProperties#set()
//import android.os.SystemProperties;
object DeviceCfg {
    var TAG = "DeviceCfg"// ErrorMgr.guardar(TAG,"getSerialNumber", e.getMessage() );

    //    Log.msg("serialNumber", "getSerialNumber: " + serialNumber);
    @get:SuppressLint("MissingPermission")
    val serialNumber: String
        get() {
            var serialNumber = "inst"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    serialNumber = Build.getSerial()
                } catch (e: SecurityException) {
                    // ErrorMgr.guardar(TAG,"serialNumber", e.message );
                }
            }
            return serialNumber
        }

    // ICCID (Integrated Circuit Card Identifier) is a globally unique classifier that can be used to identify SIM hardware (most prominently physical SIM cards)
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getIccid(context: Context?, slot: Int): String? {
        var iccId : String? = null
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            if (subscriptionList == null||subscriptionList.size==0) {
                Log.msg(TAG, "[getIccid] No existe SIMs")
                return iccId
            }

            var nSlot = 0
            if(subscriptionList.size ==1)
                nSlot= 0
            else
                nSlot = slot

            iccId =subscriptionList[nSlot].iccId
            Log.msg(TAG, "[getIccid] nSlot: "+nSlot + " iccId: "+iccId)


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"getIccid",ex.message)
        }
        return iccId
    }

    // NOTA:
    //      NO USAR Log.msg, porque se cicla...
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getImei(context: Context): String {
        var imei = "" //tMgr.getDeviceId();
        try {
            val tMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            imei = tMgr.imei
            // imei = tMgr.getDeviceId() //Toma el IMEI Default
            if (imei == null)
                imei = serialNumber //Build.getSerial()
        } catch (e: Exception) {
           imei = if (e.message!!.contains("does not meet the requirements")
                                || e.message!!.contains("does not have")
                                || e.message!!.contains("must not be null"))
               serialNumber   // Build.getSerial()
           else {
                //    ErrorMgr.guardar(TAG,"getImei*", e.getMessage());
                "ERROR"
            }
        }
        return imei
    }
  //  @RequiresApi(Build.VERSION_CODES.Q)
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getImeiBySlot(context: Context?,slot: Int): String {
        var imei = serialNumber
        try {
            val tMgr = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            Log.i(TAG, "[getImeiBySlot] Single or Dula Sim "+tMgr.getPhoneCount());
            Log.i(TAG, "[getImeiBySlot] Defualt device ID "+tMgr.getDeviceId());
            Log.i(TAG, "[getImeiBySlot] Single 0 "+tMgr.getDeviceId(0));
            Log.i(TAG, "[getImeiBySlot] Single 1 "+tMgr.getDeviceId(1));
            tMgr.phoneCount
            imei =tMgr.getDeviceId(slot);
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCountSIM", e.message)
        }
        return imei
    }


    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getSimCount(context: Context?): Int {
        var countSim : Int = 1
        try {
            val tMgr = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            countSim= tMgr.phoneCount
            Log.i(TAG, "[getSimCount] Single or Dual Sim: "+countSim);
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getSimCount", e.message)
        }
        return countSim
    }
/*
            val tMgr = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(tMgr!!.javaClass.name)

             //   val method: Method = telephonyClass.getMethod("getDeviceId", WifiConfiguration::class.java, Int::class.javaPrimitiveType)
               // method.invoke(tMgr, config, true)


                val parameter = arrayOfNulls<Class<*>?>(1)
                parameter[0] = Int::class.javaPrimitiveType
                Log.msg(TAG,"[getImeiBySlot] parameter[0] "+parameter[0])


                val getFirstMethod: Method = telephonyClass.getMethod("getDeviceId", *parameter)
                Log.d("SimData", getFirstMethod.toString())
                val obParameter = arrayOfNulls<Any>(1)
                obParameter[0] = 0
                val ime1 = getFirstMethod.invoke(tMgr, obParameter)
                Log.d("[getImeiBySlot] IMEI ", "first :$ime1")
                obParameter[0] = 1
                Log.d("[getImeiBySlot] IMEI ", "Second :${getFirstMethod.invoke(tMgr, obParameter)}")
            } catch (e: Exception) {
               ErrorMgr.guardar(TAG,"",e.message)
            }*/
        /*val subscriptionManager = SubscriptionManager.from(context)
     val subscriptionList = subscriptionManager.activeSubscriptionInfoList
     if (subscriptionList != null) {
          val tMgr = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
          imei = tMgr.getImei(slot)
          Log.msg(TAG,"[getImeiBySlot]--------------")
          var idx = 0
          subscriptionList.forEach(){
             Log.msg(TAG,"[getImeiBySlot] simSlotIndex: -->" +it.simSlotIndex +" cardId: "+   it.cardId )
             try{
                  Log.msg(TAG,"[getImeiBySlot] imei: $idx.- "+ tMgr.getImei(idx) +" [for]"  )
              }catch (ex:Exception){

              }
          }
      }*/



    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun currentSlot(context: Context):Int {
      var currentSlot = 0
      if (Utils.SDK_INT <= Build.VERSION_CODES.P) {
          return currentSlot
      }
      var nDataSubscriptionId = -1
      try {
          nDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
          currentSlot = SubscriptionManager.getSlotIndex(nDataSubscriptionId)
      } catch (e: Exception) {
          currentSlot = 0
          Log.msg(TAG,"ERROR: [currentSlot] "+ e.message)
          //    ErrorMgr.guardar(TAG, "hasIMEI", e.message)
      }

      return currentSlot
  }
  /*          Log.msg(TAG,"[slotDefault] getSlotIndex: "  )

            Log.msg(TAG,"[slotDefault] nDataSubscriptionId: "+nDataSubscriptionId)


            Log.msg(TAG,"[slotDefault] getDefaultSmsSubscriptionId: " + SubscriptionManager.getDefaultSmsSubscriptionId() )
    //        Log.msg(TAG,"[slotDefault] getActiveDataSubscriptionId: " + SubscriptionManager.getActiveDataSubscriptionId() )  //Requiere R.
            Log.msg(TAG,"[slotDefault] getDefaultSubscriptionId: " + SubscriptionManager.getDefaultSubscriptionId() )
            Log.msg(TAG,"[slotDefault] getSlotIndex: " + SubscriptionManager.getSlotIndex(nDataSubscriptionId) )
            //--->
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList


            var index = 0;
            Log.msg(TAG,"[slotDefault] ")
            for (subscriptioninfo in subscriptionList) {
                msg(TAG, index.toString() +".- iccid: " +subscriptioninfo.iccId)



                index++
            }

    }*/
    @JvmStatic
    fun hasIMEI(context: Context): Boolean {
        var imei = ""
        var bResult = false
        try {
            val tMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (tMgr == null) {
                bResult = false
            } else {
                imei = tMgr.imei
                if (imei != null) bResult = !imei.isEmpty()
            }
        } catch (e: Exception) {
        //    ErrorMgr.guardar(TAG, "hasIMEI", e.message)
        }
        return bResult
    }

    //Returns the unique subscriber ID, for example, the IMSI for a GSM phone.
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getImsi(context: Context): String? {
        return try {
            val tMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager ?: return null
            tMgr.subscriberId
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getImsi", e.message)
            null
        }
    }

   // @RequiresApi(Build.VERSION_CODES.Q)
/*    @JvmStatic*/
    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun getCarrierId_Q(context: Context, slot: Int): String {
        var carrierID = ""
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            if (subscriptionManager == null) {
                msg(TAG, "no hay datos..")
                return ""
            }
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            for (subscriptioninfo in subscriptionList) {
              //  msg(TAG, "SDK >= Q")

                //msg(TAG, subscriptioninfo.mccString)
               // msg(TAG, subscriptioninfo.mncString)
               // carrierID = subscriptioninfo.carrierId

                carrierID = subscriptioninfo.mccString +subscriptioninfo.mncString
                msg(TAG, "** carrierID: $carrierID")
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCarrierId", e.message)
        }
        return carrierID
    }

   /* @JvmStatic*/
    @SuppressLint("MissingPermission")
    fun getCarrierId_P(context: Context, slot: Int): String {
        var carrierID = ""
        try {

            val subscriptionManager = SubscriptionManager.from(context)
            run {

                //(MCC + MNC)
                val telephone = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val simOperatorCode = telephone.simOperator
                carrierID = simOperatorCode //.toInt()
            }
            msg(TAG, "** carrierID: $carrierID")

        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCarrierId", e.message)
        }
        return carrierID
    }


    //Regresa: "Telcel"
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getCarrierName(context: Context?, slot: Int): String? {
        var carrierName = "desconocido"
        //Log.msg("getPhoneNumber","subscriptionManager slot:"+slot);
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            for (subscriptioninfo in subscriptionList) {
                carrierName = subscriptioninfo.carrierName as String
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCarrierName", e.message)
        }
        return carrierName
    }

    //Regresa: "Telcel" o "Solo Emergecias"
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getDisplayText(context: Context?, slot: Int): String? {
        var displayText = "indefinido"
        //Log.msg("getPhoneNumber","subscriptionManager slot:"+slot);
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            for (subscriptioninfo in subscriptionList) {
                displayText = subscriptioninfo.displayName as String
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getDisplayText", e.message)
        }
        return displayText
    }

    //Regresa: MCC - Mobile Country Code
    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun getMCC_Q(context: Context, slot: Int): Int {
        var mccString = -1
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            var idx = currentSlot(context)
            Log.msg(TAG, "[getMCC_Q] idx; $idx slot: $slot")
            for (subscriptioninfo in subscriptionList) {
                    mccString = subscriptioninfo.mccString!!.toInt()
                //  mccString= 0;
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getMCC_Q", e.message)
        }
        return mccString
    }
    @SuppressLint("MissingPermission")
    fun getMCC_P(context: Context, slot: Int): Int {
        var mccString = -1
        try {
            //(MCC + MNC)
            val telephone = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simOperatorCode = telephone.simOperator
            mccString = simOperatorCode.substring(0, 3).toInt()
            msg(TAG, "simOperatorCode: $simOperatorCode")

        //  mccString= 0;

        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getMCC_P", e.message)
        }
        return mccString
    }
    //Regresas: 020 - MNC - Mobile Network Code
    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    fun getMNC_Q(context: Context, slot: Int): Int {
        var mncString = -1
        //Log.msg("getPhoneNumber","subscriptionManager slot:"+slot);
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            var idx = currentSlot(context)
            Log.msg(TAG, "[getMNC_Q] idx; $idx slot: $slot")
            for (subscriptioninfo in subscriptionList) {

                mncString = subscriptioninfo.mncString!!.toInt()

            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getMNC_Q", e.message)
        }
        return mncString
    }
    fun getMNC_P(context: Context, slot: Int): Int {
        var mncString = -1
        //Log.msg("getPhoneNumber","subscriptionManager slot:"+slot);
        try {

            //(MCC + MNC)
            val telephone = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simOperatorCode = telephone.simOperator
            mncString = simOperatorCode.substring(3).toInt()
            msg(TAG, "simOperatorCode: $simOperatorCode")

            ///String simOperatorName = telephone.getSimOperatorName();
            // Log.msg(TAG,"simOperatorName: "+simOperatorName);
            //  mncString=0;

        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getMNC_P", e.message)
        }
        return mncString
    }
    //Regresa: mx
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getCountryId(context: Context?, slot: Int): String? {
        var countryId = "indefinido"
        //Log.msg("getPhoneNumber","subscriptionManager slot:"+slot);
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            for (subscriptioninfo in subscriptionList) {
                countryId = subscriptioninfo.countryIso as String
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCountryId", e.message)
        }
        return countryId
    }

    //Regresa el Numero de Sims.
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun getCountSIM(context: Context?): Int {
        //  Log.msg(TAG,"getCountSIM ");
        var count = 0
        try {
            val subscriptionManager = SubscriptionManager.from(context)
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            if (subscriptionList != null) count = subscriptionList.size
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "getCountSIM", e.message)
        }
        return count
    }



    @JvmStatic
    fun revisarSIMChanged(context: Context?): Boolean {
        var bResult = false
        try {
            var currentNumSerie = getIccid(context, 0)
            val anteriorNumSerie: String = Settings.getSetting(Cons.KEY_CURRENT_SIM_NUMBER, "")
            msg(TAG, "Actual: $currentNumSerie anterior: $anteriorNumSerie")
            if (currentNumSerie == null) currentNumSerie = ""
            if (currentNumSerie != anteriorNumSerie) {
                msg(TAG, "Bloquear - Se removio el SIM - actual: [$currentNumSerie]")
               Sender.sendBloqueo(true, context!!, Kiosko.eTipo.PorCambioSIM)
                if (currentNumSerie.isEmpty())
                    sendSIMStatus(Cons.TEXT_SIM_REMOVIDA) else sendSIMStatus(Cons.TEXT_SIM_INSERTADA)
                bResult = true
            } else msg(TAG, "Sin cambio en SIM")
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "revisarSIMChanged", e.message)
        }
        return bResult
    }

    fun getSlotCount(context: Context): Int {
        var count = 1
        try {
            val mSubscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            if (mSubscriptionManager != null) {
                count = mSubscriptionManager.activeSubscriptionInfoCountMax
                return count
            }
        } catch (e:Exception) {
            ErrorMgr.guardar(TAG, "getSimCount", e.message)
        }
        return count
    }
}