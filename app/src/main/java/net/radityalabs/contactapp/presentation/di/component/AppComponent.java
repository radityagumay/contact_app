package net.radityalabs.contactapp.presentation.di.component;

import net.radityalabs.contactapp.ContactApp;
import net.radityalabs.contactapp.data.network.RetrofitHelper;
import net.radityalabs.contactapp.presentation.di.module.AppModule;
import net.radityalabs.contactapp.presentation.di.module.HttpModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by radityagumay on 4/11/17.
 */

@Singleton
@Component(modules = {AppModule.class, HttpModule.class})
public interface AppComponent {

    ContactApp getContext();

    RetrofitHelper retrofitHelper();
}
