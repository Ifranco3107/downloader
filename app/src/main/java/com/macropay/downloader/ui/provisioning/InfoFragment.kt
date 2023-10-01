package com.macropay.downloader.ui.provisioning

import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.databinding.FragmentInfoBinding
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import com.samsung.android.knox.EnterpriseDeviceManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

