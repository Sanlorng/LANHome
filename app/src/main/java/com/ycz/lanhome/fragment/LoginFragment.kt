package com.ycz.lanhome.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.ycz.lanhome.*
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.model.ResultCode
import com.ycz.lanhome.model.User
import com.ycz.lanhome.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    var user = User()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is Shell) {
            (activity!! as Shell).showToolbar()
        }
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.users.observe(this, Observer {

        })
        viewModel.tabSelectItem.observe(this, Observer {
            tabSelectLoginOrRegister.selectTab(tabSelectLoginOrRegister.getTabAt(it))
            if (it == 0)
                userLogin.text = getString(R.string.login)
            if (it == 1)
                userLogin.text = getString(R.string.register)
        })
        viewModel.loginStates.observe(this, Observer {
            when (it.code) {
                ResultCode.LOADED.code -> {

                }

                ResultCode.LOADING.code -> {

                }

                ResultCode.FAILED.code -> {
                    toast(getString(R.string.login_failed))
                }
                ResultCode.NOT_FOUND.code -> {
                    toast("该用户不存在")
                }

                402 -> {
                    toast("密码不正确")
                }
                ResultCode.SUCCESS.code -> {

                    toast(getString(R.string.login_success))
                    AppConfig.userId = it.data?:-1
//                    activity?.finish()

                }
            }
        })
        viewModel.loginUser.observe(this, Observer {
            AppConfig.user = it
            activity?.finish()
        })
        tabSelectLoginOrRegister.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.tabSelectItem.postValue(tab?.position ?: 0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
        })

        userLogin.setOnClickListener {
            loginOrRegister()
        }

        layoutUserPassword.editText?.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                loginOrRegister()
            }
            return@setOnEditorActionListener true
        }

        layoutUserNameOrPhone.editText?.doOnTextChanged { text, start, count, after ->
            layoutUserNameOrPhone.error = null
            if (text.isNullOrEmpty()) {
                layoutUserNameOrPhone.error = getString(R.string.username_or_phone_do_not_exist)
                layoutUserNameOrPhone.isErrorEnabled = true
                userLogin.isEnabled = false
            } else if (layoutUserPassword.editText?.text?.trim().isNullOrEmpty().not()) {
                layoutUserNameOrPhone.error = ""
                userLogin.isEnabled = true
            }
        }
        layoutUserPassword.editText?.doOnTextChanged { text, start, count, after ->
            layoutUserNameOrPhone.error = null
            if (text?.length ?: 0 < 6) {
                layoutUserPassword.error = getString(R.string.password_is_too_short)
                userLogin.isEnabled = false
            } else if (layoutUserNameOrPhone.editText?.text?.trim().isNullOrEmpty().not()) {
                layoutUserPassword.error = ""
                userLogin.isEnabled = true
            }

        }
    }

    private fun loginOrRegister() {
        val name = layoutUserNameOrPhone.editText?.editableText?.trim() ?: ""
        val pass = layoutUserPassword.editText?.editableText?.trim() ?: ""
        if (name.isBlank()) {
            layoutUserNameOrPhone.error = getString(R.string.please_type_username_or_phone)
        } else if (pass.isBlank()) {
            layoutUserPassword.error = getString(R.string.please_type_password)
        } else {
            user = User(userName = name.toString(), password = pass.toString())
            if (viewModel.tabSelectItem.value == 0)
                viewModel.login(user)
            else if (viewModel.tabSelectItem.value == 1)
                viewModel.register(user)
        }
    }

}
