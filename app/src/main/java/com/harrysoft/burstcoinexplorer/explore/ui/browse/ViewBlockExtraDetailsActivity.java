package com.harrysoft.burstcoinexplorer.explore.ui.browse;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.harrysoft.burstcoinexplorer.R;
import com.harrysoft.burstcoinexplorer.explore.entity.TransactionDisplayType;
import com.harrysoft.burstcoinexplorer.explore.viewmodel.browse.ViewBlockExtraDetailsViewModel;
import com.harrysoft.burstcoinexplorer.explore.viewmodel.browse.ViewBlockExtraDetailsViewModelFactory;
import com.harrysoft.burstcoinexplorer.explore.viewmodel.browse.ViewTransactionsViewModelFactory;

import java.math.BigInteger;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ViewBlockExtraDetailsActivity extends ViewTransactionsActivity {

    @Inject
    ViewTransactionsViewModelFactory viewTransactionsViewModelFactory;
    @Inject
    ViewBlockExtraDetailsViewModelFactory viewBlockExtraDetailsViewModelFactory;

    private ViewBlockExtraDetailsViewModel viewBlockExtraDetailsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_block_extra_details);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(getString(R.string.extra_block_parcel))) {
            viewBlockExtraDetailsViewModelFactory.setBlock(getIntent().getParcelableExtra(getString(R.string.extra_block_parcel)));
        } else {
            Toast.makeText(this, R.string.loading_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewBlockExtraDetailsViewModel = ViewModelProviders.of(this, viewBlockExtraDetailsViewModelFactory).get(ViewBlockExtraDetailsViewModel.class);

        TextView blockNumberText = findViewById(R.id.view_block_extra_details_block_number_value);
        TextView blockRewardText = findViewById(R.id.view_block_extra_details_block_reward_value);
        TextView transactionsLabel = findViewById(R.id.view_block_extra_details_transactions_label);

        viewBlockExtraDetailsViewModel.getTransactionIDs().observe(this, transactionIDs -> setupViewTransactionsActivity(findViewById(R.id.view_block_extra_details_transactions_list), viewTransactionsViewModelFactory, TransactionDisplayType.FROM, transactionIDs));
        viewBlockExtraDetailsViewModel.getTransactionsLabel().observe(this, transactionsLabel::setText);
        viewBlockExtraDetailsViewModel.getBlockNumberText().observe(this, blockNumberText::setText);
        viewBlockExtraDetailsViewModel.getBlockRewardText().observe(this, blockRewardText::setText);
    }

    @Override
    protected void setTransactionsLabelText(int text) {
        viewBlockExtraDetailsViewModel.setTransactionsLabel(text);
    }
}
