/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.license_agreement

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel


class LicenseAgreementScreenViewModel(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        const val ACCEPTED = "ACCEPTED"
    }

    fun getLicenseAgreementStatus(): Boolean{
        return sharedPreferences.getBoolean(ACCEPTED, false)
    }

    fun setLicenseAgreementStatus(){
        sharedPreferences.edit()?.putBoolean(ACCEPTED, true)?.apply()
    }
}