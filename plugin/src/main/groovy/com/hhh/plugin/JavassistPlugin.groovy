package com.hhh.plugin

import com.hhh.transform.AOPTransform
import com.hhh.transform.AOPType
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavassistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("Hello JavassistPlugin!")
        project.android.registerTransform(new AOPTransform(project, AOPType.JAVASSIST))
    }
}
