package com.macropay.downloader.ui.backdoor

import android.widget.TextView.OnEditorActionListener
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import com.macropay.downloader.databinding.FragmentValidarBinding
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

class ValidarFragment : DialogFragment(), OnEditorActionListener {
    private var mParam1: String? = null
    private var binding: FragmentValidarBinding? = null
    private val TAG = "ValidarFragment"
    //private EditText mEditText;
    // 1. Defines the listener interface with a method passing back data result.
    interface EditNameDialogListener {
        fun onFinishEditDialog(inputText: String?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View? = null
        binding = FragmentValidarBinding.inflate(inflater, container, false)
        view = binding!!.root
        val title = requireArguments().getString("title", "Enter Name")
        this.requireActivity().title = title
        // Show soft keyboard automatically and request focus to field
        binding!!.lblYourName.text = title
        binding!!.txtYourName.requestFocus()
        binding!!.txtYourName.setOnEditorActionListener(this)
        this.requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return view
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        try{
            Log.msg(TAG,"[onEditorAction] :" + actionId + " , [" + v.text + "]")
            if(event == null){
                Log.msg(TAG,"[onEditorAction] event == null  --->")
          //      return false
            }

            if (EditorInfo.IME_ACTION_DONE == actionId) {
                // Return input text back to activity through the implemented listener
                val listener = activity as EditNameDialogListener?
                listener!!.onFinishEditDialog(binding!!.txtYourName.text.toString())
                // Close the dialog and return back to the parent activity
                dismiss()
                return true
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onEditorAction",ex.message)
        }
        return false
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        @JvmStatic
        fun newInstance(title: String?): ValidarFragment {
            val fragment = ValidarFragment()
            val args = Bundle()
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }
    }
}