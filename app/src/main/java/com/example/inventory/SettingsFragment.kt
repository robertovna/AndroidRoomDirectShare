package com.example.inventory

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.databinding.FragmentItemDetailBinding
import com.example.inventory.databinding.FragmentSettingsBinding
import com.example.inventory.databinding.ItemListFragmentBinding
import com.example.inventory.encrypt.EncryptedSettings
import com.example.inventory.encrypt.SettingsPreference


class SettingsFragment : Fragment() {
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }
    private lateinit var settings: SettingsPreference

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun bind(settings: SettingsPreference) {
        binding.apply {
            defaultNameProvider.setText(settings.defaultProviderName, TextView.BufferType.SPANNABLE)
            defaultEmailProvider.setText(settings.defaultProviderEmail, TextView.BufferType.SPANNABLE)
            defaultPhoneProvider.setText(settings.defaultProviderPhoneNumber, TextView.BufferType.SPANNABLE)
            isSetDefaultValuesCheck.isChecked = settings.isSetDefaultValues
            hideSensitiveDataCheck.isChecked = settings.hideSensitiveData
            disableShareDataCheck.isChecked = settings.disableShareData

            saveAction.setOnClickListener { saveSettings() }
        }
    }

    private fun isEntryValid(): Boolean {
        val nameProvider = binding.defaultNameProvider.text.toString()
        val emailProvider = binding.defaultEmailProvider.text.toString()
        val phoneProvider = binding.defaultPhoneProvider.text.toString()

        var isValid = true
        if (nameProvider.isNullOrBlank()) {
            binding.defaultNameProvider.error = "Empty name provider"
            isValid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailProvider).matches()) {
            binding.defaultEmailProvider.error = "Incorrect email"
            isValid = false
        }
        if (!Patterns.PHONE.matcher(phoneProvider).matches()) {
            binding.defaultPhoneProvider.error = "Incorrect phone"
            isValid = false
        }

        return isValid
    }

    private fun saveSettings() {
        if (isEntryValid()) {
            settings = SettingsPreference(
                binding.defaultNameProvider.text.toString(),
                binding.defaultEmailProvider.text.toString(),
                binding.defaultPhoneProvider.text.toString(),
                binding.isSetDefaultValuesCheck.isChecked,
                binding.hideSensitiveDataCheck.isChecked,
                binding.disableShareDataCheck.isChecked,
            )
            viewModel.settings.setSettings(settings)

            val action = SettingsFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = viewModel.settings.getSettings()
        bind(settings)
    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}