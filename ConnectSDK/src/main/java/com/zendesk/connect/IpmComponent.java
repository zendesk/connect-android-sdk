package com.zendesk.connect;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {IpmComponent.IpmModule.class})
interface IpmComponent {

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder view(IpmMvp.View view);

        IpmComponent build();
    }

    void inject(IpmActivity ipmActivity);

    @Module
    abstract class IpmModule {

        /**
         * Provides an implementation of {@link IpmMvp.Model}
         *
         * @return an implementation of {@link IpmMvp.Model}
         */
        @Provides
        static IpmMvp.Model provideModel(IpmModel ipmModel) {
            return ipmModel;
        }

        /**
         * Provides an implementation of {@link IpmMvp.Presenter}
         *
         * @return an implementation of {@link IpmMvp.Presenter}
         */
        @Provides
        static IpmMvp.Presenter providePresenter(IpmPresenter ipmPresenter) {
            return ipmPresenter;
        }
    }
}
