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

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.inventory.data.ItemRoomDatabase
import java.security.KeyStore
import javax.crypto.KeyGenerator

class InventoryApplication : Application(){
    val database: ItemRoomDatabase by lazy {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val secretKeyEntry = ks.getEntry("DbEncryptingKey", null) as? KeyStore.SecretKeyEntry
        var secretKey = secretKeyEntry?.secretKey

        if (secretKey == null) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenerateSpec = KeyGenParameterSpec.Builder(
                "DbEncryptingKey",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenerateSpec)
            secretKey = keyGenerator.generateKey()
        }

        ItemRoomDatabase.getDatabase(this, secretKey.toString())
    }
}

