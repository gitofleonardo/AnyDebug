package com.hhvvg.anydebug.hook

import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.hook.hookers.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Class for package hook.
 *
 * @author hhvvg
 */
class AnyHookPackage : IXposedHookLoadPackage {
    private val hookers: ArrayList<IHooker> = arrayListOf(
        GlobalSettingsLoaderHooker(),
        ViewInitHooker(),
        ViewClickHooker(),
        PupupWindowHooker(),
        GlobalControlHooker(),
    )

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {
        // Don't hook itself
        val packageName = p0.packageName
        if (packageName == BuildConfig.PACKAGE_NAME) {
            return
        }
        hookers.forEach {
            it.onHook(param = p0)
        }
    }
}
