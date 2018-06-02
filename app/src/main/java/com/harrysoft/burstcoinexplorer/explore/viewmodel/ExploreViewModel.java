package com.harrysoft.burstcoinexplorer.explore.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.v4.widget.SwipeRefreshLayout;

import com.harrysoft.burstcoinexplorer.R;
import com.harrysoft.burstcoinexplorer.burst.entity.Block;
import com.harrysoft.burstcoinexplorer.burst.entity.BurstPrice;
import com.harrysoft.burstcoinexplorer.burst.service.BurstBlockchainService;
import com.harrysoft.burstcoinexplorer.burst.service.BurstPriceService;
import com.harrysoft.burstcoinexplorer.main.repository.PreferenceRepository;
import com.harrysoft.burstcoinexplorer.util.CurrencyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ExploreViewModel extends AndroidViewModel implements SwipeRefreshLayout.OnRefreshListener {

    private final BurstBlockchainService burstBlockchainService;
    private final BurstPriceService burstPriceService;
    private final PreferenceRepository preferenceRepository;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>();
    private final MutableLiveData<List<Block>> recentBlocks = new MutableLiveData<>();
    private final MutableLiveData<String> priceFiat = new MutableLiveData<>();
    private final MutableLiveData<String> priceBtc = new MutableLiveData<>();
    private final MutableLiveData<String> marketCapital = new MutableLiveData<>();
    private final MutableLiveData<String> blockHeight = new MutableLiveData<>();
    private final MutableLiveData<String> recentBlocksLabel = new MutableLiveData<>();

    private String lastCurrencyCode = "";

    ExploreViewModel(Application application, BurstBlockchainService burstBlockchainService, BurstPriceService burstPriceService, PreferenceRepository preferenceRepository) {
        super(application);
        this.burstBlockchainService = burstBlockchainService;
        this.burstPriceService = burstPriceService;
        this.preferenceRepository = preferenceRepository;

        priceFiat.postValue(application.getString(R.string.price_fiat, application.getString(R.string.loading)));

        refreshing.postValue(true);
        onRefresh();
    }

    private void getData() {
        compositeDisposable.add(burstBlockchainService.fetchRecentBlocks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onRecentBlocks, t -> onRecentBlocksError()));
    }

    private void getPrice() {
        compositeDisposable.add(burstPriceService.fetchPrice(preferenceRepository.getSelectedCurrency())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onPrice, t -> onRecentBlocksError()));

        compositeDisposable.add(burstPriceService.fetchPrice("BTC")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onPrice, t -> onPriceError()));
    }

    private void onRecentBlocksError() {
        refreshing.postValue(false);
        blockHeight.postValue(getApplication().getString(R.string.loading_error));
        recentBlocksLabel.postValue(getApplication().getString(R.string.recent_blocks_error));
    }

    private void onPriceError() {
        priceFiat.postValue(getApplication().getString(R.string.loading_error));
        priceBtc.postValue(getApplication().getString(R.string.loading_error));
        marketCapital.postValue(getApplication().getString(R.string.loading_error));
    }

    private void onPrice(BurstPrice burstPrice) {
        if (burstPrice.currencyCode.equals("BTC")) {
            priceBtc.postValue(getApplication().getString(R.string.basic_data, CurrencyUtils.formatCurrencyAmount(burstPrice.currencyCode, burstPrice.price)));
        } else {
            priceFiat.postValue(getApplication().getString(R.string.price_fiat, CurrencyUtils.formatCurrencyAmount(burstPrice.currencyCode, burstPrice.price)));
            marketCapital.postValue(getApplication().getString(R.string.basic_data, CurrencyUtils.formatCurrencyAmount(burstPrice.currencyCode, burstPrice.marketCapital)));
        }
    }

    private void onRecentBlocks(Block[] blocks) {
        refreshing.postValue(false);
        recentBlocks.postValue(Arrays.asList(blocks));
        recentBlocksLabel.postValue(getApplication().getString(R.string.recent_blocks));
        blockHeight.postValue(String.format(Locale.getDefault(), "%d", blocks[0].blockNumber));
    }

    public void checkForCurrencyChange() {
        if (!Objects.equals(preferenceRepository.getSelectedCurrency(), lastCurrencyCode)) {
            lastCurrencyCode = preferenceRepository.getSelectedCurrency();
            getPrice();
        }
    }

    @Override
    public void onRefresh() {
        getData();
        getPrice();
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
    }

    public LiveData<Boolean> getRefreshing() { return refreshing; }
    public LiveData<List<Block>> getRecentBlocks() { return recentBlocks; }
    public LiveData<String> getPriceFiat() { return priceFiat; }
    public LiveData<String> getPriceBtc() { return priceBtc; }
    public LiveData<String> getMarketCapital() { return marketCapital; }
    public LiveData<String> getBlockHeight() { return blockHeight; }
    public LiveData<String> getRecentBlocksLabel() { return recentBlocksLabel; }
}
