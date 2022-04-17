package com.hhvvg.anydebug.hook

import android.content.res.Resources
import android.content.res.XModuleResources
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.hook.hookimpl.ActivityInspectHook
import com.hhvvg.anydebug.hook.hookimpl.GlobalControlReceiverHook
import com.hhvvg.anydebug.hook.hookimpl.PupupWindowHook
import com.hhvvg.anydebug.hook.hookimpl.TextViewHook
import com.hhvvg.anydebug.hook.hookimpl.ViewBoundsHook
import com.hhvvg.anydebug.hook.hookimpl.ViewClickHook
import com.hhvvg.anydebug.hook.hookimpl.ViewRulesLoaderHook
import com.hhvvg.anydebug.hook.hookimpl.ViewVisibilityHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Class for application hook.
 *
 * @author hhvvg
 */
class AnyHookFramework : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val hooks: ArrayList<IHook> = arrayListOf(
        ViewClickHook(),
        PupupWindowHook(),
        GlobalControlReceiverHook(),
        ViewRulesLoaderHook(),
        ViewVisibilityHook(),
        TextViewHook(),
        ViewBoundsHook(),
        ActivityInspectHook(),
    )

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {
        // Don't hook itself
        val packageName = p0.packageName
        if (packageName == BuildConfig.PACKAGE_NAME) {
            return
        }
        hooks.forEach {
            it.onHook(param = p0)
        }
    }

    override fun initZygote(p0: IXposedHookZygoteInit.StartupParam) {
        modulePath = p0.modulePath
        moduleRes = getModuleRes(modulePath)
    }

    companion object {
        @JvmStatic
        lateinit var moduleRes: Resources

        @JvmStatic
        lateinit var modulePath: String

        @JvmStatic
        fun getModuleRes(path: String): Resources {
            return XModuleResources.createInstance(path, null)
        }
    }
}
