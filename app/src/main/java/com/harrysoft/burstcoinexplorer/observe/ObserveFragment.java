package com.harrysoft.burstcoinexplorer.observe;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.harrysoft.burstcoinexplorer.R;
import com.harrysoft.burstcoinexplorer.burst.api.BurstInfoService;
import com.harrysoft.burstcoinexplorer.burst.api.BurstNetworkService;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ObserveFragment extends Fragment {

    @Inject
    BurstNetworkService burstNetworkService;
    @Inject
    BurstInfoService burstInfoService;

    private ObservePagerAdapter observePagerAdapter;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_observe, container, false);

        ViewPager viewPager = view.findViewById(R.id.observe_viewpager);
        observePagerAdapter = setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.observe_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        getNetworkStatus();

        return view;
    }

    private void getNetworkStatus(@SuppressWarnings("SameParameterValue") @Nullable ObserveSubFragment sender) {
        burstNetworkService.fetchNetworkStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkStatus -> observePagerAdapter.onNetworkStatus(networkStatus, sender), throwable -> onError(throwable, sender));
        burstInfoService.getForks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observePagerAdapter::onForkInfos, throwable -> onError(throwable, sender));
    }

    private void getNetworkStatus() {
        getNetworkStatus(null);
    }

    private void onError(Throwable throwable, @Nullable ObserveSubFragment sender) {
        Toast.makeText(getContext(), R.string.loading_error, Toast.LENGTH_LONG).show();
        if (sender != null) {
            sender.onRefreshError(throwable);
        }
    }

    private ObservePagerAdapter setupViewPager(ViewPager viewPager) {
        ObservePagerAdapter observePagerAdapter = new ObservePagerAdapter(getChildFragmentManager());

        ObserveStatusFragment statusFragment = new ObserveStatusFragment();
        ObserveVersionsFragment versionsFragment = new ObserveVersionsFragment();
        ObserveForksFragment forksFragment = new ObserveForksFragment();
        ObservePeersMapFragment mapFragment = new ObservePeersMapFragment();
        ObserveBrokenPeersFragment brokenPeersFragment = new ObserveBrokenPeersFragment();

        statusFragment.setUp(this::getNetworkStatus);
        versionsFragment.setUp(this::getNetworkStatus);
        forksFragment.setUp(this::getNetworkStatus);
        mapFragment.setUp(this::getNetworkStatus);
        brokenPeersFragment.setUp(this::getNetworkStatus);

        observePagerAdapter.addFragment(statusFragment, getString(R.string.observe_peer_status));
        observePagerAdapter.addFragment(versionsFragment, getString(R.string.observe_peer_versions));
        observePagerAdapter.addFragment(forksFragment, getString(R.string.observe_forks));
        observePagerAdapter.addFragment(mapFragment, getString(R.string.observe_map));
        observePagerAdapter.addFragment(brokenPeersFragment, getString(R.string.observe_broken_peers));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(observePagerAdapter);
        return observePagerAdapter;
    }
}
