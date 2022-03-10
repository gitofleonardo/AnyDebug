package com.hhvvg.anydebug.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hook interface for implementing customized hook process.
 */
interface IHooker {

    /**
     * Method with hook process.
     * @param param Package params.
     */
    fun onHook(param: XC_LoadPackage.LoadPackageParam)
}