package com.example.inventory

import androidx.lifecycle.*
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch

class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String, nameProvider: String, emailProvider: String, phoneProvider: String, source: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt(),
            nameProvider = nameProvider,
            emailProvider = emailProvider,
            phoneNumberProvider = phoneProvider,
            source = source
        )
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        nameProvider: String,
        emailProvider: String,
        phoneProvider: String,
        source: String
    ): Item {
        return Item(
            id = itemId,
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt(),
            nameProvider = nameProvider,
            emailProvider = emailProvider,
            phoneNumberProvider = phoneProvider,
            source = source
        )
    }

    fun sellItem(item: Item) {
        if (item.quantityInStock > 0) {
            // Decrease the quantity by 1
            val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
            updateItem(newItem)
        }
    }
    fun isStockAvailable(item: Item): Boolean {
        return (item.quantityInStock > 0)
    }

    fun addNewItem(itemName: String, itemPrice: String, itemCount: String, nameProvider: String, emailProvider: String, phoneProvider: String, source: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount, nameProvider, emailProvider, phoneProvider, source )
        insertItem(newItem)
    }

    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        nameProvider: String,
        emailProvider: String,
        phoneProvider: String,
        source: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount, nameProvider, emailProvider,phoneProvider, source)
        updateItem(updatedItem)
    }

    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String, nameProvider: String, emailProvider: String, phoneProvider: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank() || nameProvider.isBlank() || emailProvider.isBlank() || phoneProvider.isBlank()) {
            return false
        }
        return true
    }

    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }
}

class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}