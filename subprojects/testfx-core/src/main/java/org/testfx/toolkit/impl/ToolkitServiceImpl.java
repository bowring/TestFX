/*
 * Copyright 2013-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the Licence for the specific language governing permissions
 * and limitations under the Licence.
 */
package org.testfx.toolkit.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.testfx.toolkit.ApplicationLauncher;
import org.testfx.toolkit.ApplicationService;
import org.testfx.toolkit.ToolkitService;
import org.testfx.toolkit.StageFuture;

import static org.loadui.testfx.utils.WaitForAsyncUtils.async;
import static org.loadui.testfx.utils.WaitForAsyncUtils.asyncFx;

public class ToolkitServiceImpl implements ToolkitService {

    //---------------------------------------------------------------------------------------------
    // PRIVATE FIELDS.
    //---------------------------------------------------------------------------------------------

    private ApplicationLauncher applicationLauncher;

    private ApplicationService applicationService;

    //---------------------------------------------------------------------------------------------
    // CONSTRUCTORS.
    //---------------------------------------------------------------------------------------------

    public ToolkitServiceImpl(ApplicationLauncher applicationLauncher,
                              ApplicationService applicationService) {
        this.applicationLauncher = applicationLauncher;
        this.applicationService = applicationService;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS.
    //---------------------------------------------------------------------------------------------

    @Override
    public Future<Stage> setupPrimaryStage(StageFuture primaryStageFuture,
                                           Class<? extends Application> applicationClass,
                                           String... applicationArgs) {
        if (!primaryStageFuture.isDone()) {
            async(() -> {
                try {
                    applicationLauncher.launch(applicationClass, applicationArgs);
                }
                catch (Throwable exception) {
                    primaryStageFuture.setException(exception);
                }
            });
        }
        return primaryStageFuture;
    }

    @Override
    public Future<Void> setup(Runnable runnable) {
        return asyncFx(runnable);
    }

    @Override
    public <T> Future<T> setup(Callable<T> callable) {
        return asyncFx(callable);
    }

    @Override
    public Future<Stage> setupStage(Stage stage,
                                    Consumer<Stage> stageConsumer) {
        return asyncFx(() -> {
            stageConsumer.accept(stage);
            return stage;
        });
    }

    @Override
    public Future<Scene> setupScene(Stage stage,
                                    Supplier<? extends Scene> sceneSupplier) {
        return asyncFx(() -> {
            Scene scene = sceneSupplier.get();
            stage.setScene(scene);
            return scene;
        });
    }

    @Override
    public Future<Parent> setupSceneRoot(Stage stage,
                                         Supplier<? extends Parent> sceneRootSupplier) {
        return asyncFx(() -> {
            Parent rootNode = sceneRootSupplier.get();
            stage.setScene(new Scene(rootNode));
            return rootNode;
        });
    }

    @Override
    public Future<Application> setupApplication(Stage stage,
                                                Class<? extends Application> applicationClass,
                                                String... applicationArgs) {
        return async(() -> {
            Application application = applicationService.create(
                applicationClass, applicationArgs
            ).get();
            applicationService.init(application).get();
            applicationService.start(application, stage).get();
            return application;
        });
    }

    @Override
    public Future<Void> cleanupApplication(Application application) {
        return applicationService.stop(application);
    }

}