package com.ray.router.register.launch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.ray.router.register.utils.Logger
import com.ray.router.register.utils.ScanSetting
import com.ray.router.register.core.RegisterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Simple version of AutoRegister plugin for ARouter
 * @author billy.qi email: qiyilike@163.com
 * @since 17/12/06 15:35
 */
public class PluginLaunch implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        //only application module needs this plugin to generate register code
        Logger.make(project)
        if (isApp) {
//            Logger.make(project)
            Logger.i('Project enable router-register plugin')

            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new RegisterTransform(project)

            //init arouter-auto-register settings
            ArrayList<ScanSetting> list = new ArrayList<>(1)
            list.add(new ScanSetting('Provider'))
            RegisterTransform.registerList = list
            //register this plugin
            android.registerTransform(transformImpl)
        }else{
            Logger.i('Project disable router-register plugin')
        }
    }

}
