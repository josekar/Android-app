package com.example.stockapp;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

final class DateSection extends Section {

    Date date = new Date();
    SimpleDateFormat simpleDate =  new SimpleDateFormat("MMMM d, yyyy");
    String sectionTitle = simpleDate.format(date);
    List<String> itemList = Arrays.asList();

    public DateSection() {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_item)
                .headerResourceId(R.layout.date_header)
                .build());
    }


    @Override
    public int getContentItemsTotal() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new StockItemViewHolder(view);
    }


    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        StockItemViewHolder itemHolder = (StockItemViewHolder) holder;
        String itemName = itemList.get(position);

        // bind your view here
//        itemHolder.tvItem.setText(itemName);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.tvTitle.setText(sectionTitle);
    }


}
