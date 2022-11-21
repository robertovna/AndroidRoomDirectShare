/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.Item
import com.example.inventory.data.getFormattedPrice
import com.example.inventory.databinding.FragmentItemDetailBinding
import com.example.inventory.encrypt.EncryptFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * [ItemDetailFragment] displays the details of the selected item.
 */
class ItemDetailFragment : Fragment() {
    companion object {
        const val FILE_SAVE = 1515
    }

    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }
    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun bind(item: Item) {
        var settings = viewModel.settings.getSettings()
        binding.apply {
            itemName.text = item.itemName
            itemPrice.text = item.getFormattedPrice()
            itemCount.text = item.quantityInStock.toString()
            nameProvider.text = if (settings.hideSensitiveData) "***" else item.nameProvider
            emailProvider.text = if (settings.hideSensitiveData) "***" else item.emailProvider
            phoneProvider.text = if (settings.hideSensitiveData) "***" else item.phoneNumberProvider
            source.text = item.source
            sellItem.isEnabled = viewModel.isStockAvailable(item)
            sellItem.setOnClickListener { viewModel.sellItem(item) }
            deleteItem.setOnClickListener { showConfirmationDialog() }
            editItem.setOnClickListener { editItem() }
            share.setOnClickListener { shareItem() }
            shareFile.setOnClickListener { shareItemFile() }
            saveIntoFile.setOnClickListener { saveItemIntoFile() }
            if (viewModel.settings.getSettings().disableShareData) {
                share.isEnabled = false
                shareFile.isEnabled = false
            }
        }
    }

    private fun saveItemIntoFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "${item.id}.json")
        }
        startActivityForResult(intent, FILE_SAVE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_SAVE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                EncryptFile.encryptItemIntoFile(requireContext(), uri, item)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.itemId
        viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
            item = selectedItem
            bind(item)
        }
    }

    private fun editItem() {
        val action = ItemDetailFragmentDirections.actionItemDetailFragmentToAddItemFragment(
            getString(R.string.edit_fragment_title),
            item.id,
            item.source
        )
        this.findNavController().navigate(action)
    }

    private fun shareItem() {
        var settings = viewModel.settings.getSettings()
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        var text = "Name: " + item.itemName + "\n"
        text += "Price: " + item.itemPrice.toString() + "\n"
        text += "Quantity in stock: " + item.quantityInStock.toString() + "\n"
        text += "Provider Name: " + if (settings.hideSensitiveData) "***" else item.nameProvider + "\n"
        text += "Provider Email: " + if (settings.hideSensitiveData) "***" else item.emailProvider + "\n"
        text += "Provider Phone: " + if (settings.hideSensitiveData) "***" else item.phoneNumberProvider + "\n"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)

        startActivity(Intent.createChooser(sharingIntent, null))
    }

    private fun shareItemFile() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/json"
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val file = EncryptFile.getEncryptedItemPathInCache(requireContext(), item)
        val fileURI = FileProvider.getUriForFile(requireContext(), "com.example.inventory.provider", file)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileURI)
        sharingIntent.setPackage("com.google.android.gm")
        startActivity(Intent.createChooser(sharingIntent, null))
    }

    /**
     * Displays an alert dialog to get the user's confirmation before deleting the item.
     */
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteItem()
            }
            .show()
    }

    /**
     * Deletes the current item and navigates to the list fragment.
     */
    private fun deleteItem() {
        viewModel.deleteItem(item)
        findNavController().navigateUp()
    }

    /**
     * Called when fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
