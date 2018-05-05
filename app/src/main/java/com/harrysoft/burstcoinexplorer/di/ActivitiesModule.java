package com.harrysoft.burstcoinexplorer.di;

import com.harrysoft.burstcoinexplorer.explore.browse.ViewAccountDetailsActivity;
import com.harrysoft.burstcoinexplorer.explore.browse.ViewAccountTransactionsActivity;
import com.harrysoft.burstcoinexplorer.explore.browse.ViewBlockDetailsActivity;
import com.harrysoft.burstcoinexplorer.explore.browse.ViewBlockExtraDetailsActivity;
import com.harrysoft.burstcoinexplorer.explore.browse.ViewTransactionDetailsActivity;
import com.harrysoft.burstcoinexplorer.main.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivitiesModule {
    @ActivityScope
    @ContributesAndroidInjector (modules = MainModule.class)
    abstract MainActivity bindMainActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ViewAccountDetailsActivity bindViewAccountDetailsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ViewAccountTransactionsActivity bindViewAccountTransactionsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ViewBlockDetailsActivity bindViewBlockDetailsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ViewBlockExtraDetailsActivity bindViewBlockExtraDetailsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ViewTransactionDetailsActivity bindViewTransactionDetailsActivity();
}