package com.sciri.mlsearch.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sciri.mlsearch.R
import com.sciri.mlsearch.databinding.FragmentSignUpBinding
import com.sciri.mlsearch.isValidEmail


class SignUpFragment : Fragment() {

    interface SignUpFragmentActions {
        fun onSignUpFieldsValidated(
            email: String, password: String, passwordConfirmation: String
        )
    }

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var signUpFragmentActions: SignUpFragmentActions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater)
        setUpSignUpButton()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signUpFragmentActions = try {
            context as SignUpFragmentActions
        } catch (e: Exception) {
            throw java.lang.ClassCastException("$context must implement LoginFragmentActions")
        }
    }

    private fun setUpSignUpButton() {
        binding.signUpButton.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        binding.emailInput.error = ""
        binding.passwordInput.error = ""
        binding.confirmPasswordInput.error = ""

        val email = binding.emailEdit.text.toString()
        if (!isValidEmail(email)) {
            binding.emailInput.error = getString(R.string.email_is_not_valid)
            return
        }

        val password = binding.passwordEdit.text.toString()
        if (password.isEmpty()) {
            binding.passwordInput.error = getString(R.string.password_must_not_be_empty)
            return
        }

        val confirmPassword = binding.confirmPasswordEdit.text.toString()
        if (password != confirmPassword) {
            binding.confirmPasswordInput.error = getString(R.string.passwords_do_not_match)
            return
        }

        signUpFragmentActions.onSignUpFieldsValidated(email, password, password)
    }
}