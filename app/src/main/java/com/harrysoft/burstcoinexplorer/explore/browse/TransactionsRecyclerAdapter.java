package com.harrysoft.burstcoinexplorer.explore.browse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.harrysoft.burstcoinexplorer.burst.explorer.BurstExplorer;
import com.harrysoft.burstcoinexplorer.R;
import com.harrysoft.burstcoinexplorer.burst.api.BurstBlockchainService;
import com.harrysoft.burstcoinexplorer.burst.entity.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class TransactionsRecyclerAdapter extends RecyclerView.Adapter<TransactionsRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final BurstBlockchainService burstBlockchainService;
    private final BurstExplorer burstExplorer;

    private final ArrayList<BigInteger> transactionIDs;
    private final ArrayMap<BigInteger, Transaction> transactions = new ArrayMap<>();

    private int displayedItems;
    private boolean loadingMoreItems = false;
    private final int totalItems;

    private final static int TRANSACTION_VIEW_TYPE = 1;
    private final static int LOAD_MORE_VIEW_TYPE = 2;

    TransactionsRecyclerAdapter(Context context, BurstBlockchainService apiService, BurstExplorer burstExplorer, ArrayList<BigInteger> transactionIDs) {
        this.context = context;
        this.burstBlockchainService = apiService;
        this.burstExplorer = burstExplorer;
        this.transactionIDs = transactionIDs;
        totalItems = transactionIDs.size();
        displayedItems = 0;
        loadMore();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == displayedItems) { // if it is longer than the list of displayed items (displayedItems counts from 1, position counts from 0)
            return LOAD_MORE_VIEW_TYPE;
        } else {
            return TRANSACTION_VIEW_TYPE;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case TRANSACTION_VIEW_TYPE:
                return new ViewHolder(context, burstExplorer, LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false), viewType);

            case LOAD_MORE_VIEW_TYPE:
                ViewHolder viewHolder = new ViewHolder(context, burstExplorer, LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_view_transactions_load_more, parent, false), viewType);
                viewHolder.setOnClickListener(view -> {
                    if (!loadingMoreItems) {
                        loadMore();
                    }
                });
                return viewHolder;

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < displayedItems) {
            BigInteger transactionID = transactionIDs.get(position);
            if (transactionID != null && transactions.get(transactionID) != null) {
                holder.setupView(transactions.get(transactionID));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (displayedItems == totalItems) {
            return displayedItems;
        } else if (displayedItems == 0) {
            return 0;
        } else {
            return displayedItems + 1;
        }
    }

    private void loadMore() {
        loadingMoreItems = true;

        int tempDisplayedItems = displayedItems + 25;
        if (tempDisplayedItems > totalItems) {
            tempDisplayedItems = totalItems;
        }

        int transactionsToAdd = tempDisplayedItems - displayedItems;

        ArrayMap<BigInteger, Transaction> newTransactions = new ArrayMap<>();

        for (int i = 1; i <= transactionsToAdd; i++) {
            BigInteger transactionID = transactionIDs.get(displayedItems + i - 1); // get counts from 0, i counts from 1
            burstBlockchainService.fetchTransaction(transactionID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(transaction -> {
                        newTransactions.put(transactionID, transaction);
                        if (newTransactions.size() == transactionsToAdd) {
                            finaliseLoadMore(newTransactions, transactionsToAdd);
                        }
                    }, error -> Toast.makeText(context, "Error loading transaction #" + transactionID.toString(), Toast.LENGTH_LONG).show());
        }
    }

    private void finaliseLoadMore(Map<BigInteger, Transaction> newTransactions, int newDisplayedItems) {
        displayedItems += newDisplayedItems;
        transactions.putAll(newTransactions);
        notifyDataSetChanged();
        loadingMoreItems = false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final BurstExplorer burstExplorer;

        private final RelativeLayout listItem;
        private final TextView text1;
        private final TextView text2;
        private final TextView type;
        private final TextView data;
        private final Button loadMore;
        private final int viewType;

        ViewHolder(Context context, BurstExplorer burstExplorer, View v, int viewType) {
            super(v);
            this.context = context;
            this.burstExplorer = burstExplorer;
            this.viewType = viewType;
            switch (viewType) {
                case TRANSACTION_VIEW_TYPE:
                    listItem = v.findViewById(R.id.list_item);
                    text1 = v.findViewById(R.id.list_item_text1);
                    text2 = v.findViewById(R.id.list_item_text2);
                    type = v.findViewById(R.id.list_item_type);
                    data = v.findViewById(R.id.list_item_data);
                    loadMore = null;
                    break;

                case LOAD_MORE_VIEW_TYPE:
                    listItem = null;
                    text1 = null;
                    text2 = null;
                    type = null;
                    data = null;
                    loadMore = v.findViewById(R.id.view_transactions_load_more);
                    break;

                default:
                    listItem = null;
                    text1 = null;
                    text2 = null;
                    type = null;
                    data = null;
                    loadMore = null;
            }
        }

        void setupView(Transaction transaction) {
            if (viewType == TRANSACTION_VIEW_TYPE) {
                text1.setText(context.getString(R.string.transaction_id_with_data, transaction.transactionID.toString()));
                text2.setText(transaction.amount.toString());
                text2.setText(context.getString(R.string.transaction_view_info, transaction.amount.toString(), transaction.sender.getFullAddress(), transaction.recipient.getFullAddress()));
                type.setText(context.getString(R.string.extra_block_extra));
                data.setText(context.getString(R.string.basic_data, transaction.transactionID.toString()));
                listItem.setOnClickListener(view -> burstExplorer.viewTransactionDetailsByTransaction(transaction));
            }
        }

        void setOnClickListener(View.OnClickListener onClickListener) {
            if (viewType == LOAD_MORE_VIEW_TYPE) {
                loadMore.setOnClickListener(view -> {
                    onClickListener.onClick(view);
                    Toast.makeText(context, R.string.loading, Toast.LENGTH_LONG).show();
                });
            }
        }
    }
}