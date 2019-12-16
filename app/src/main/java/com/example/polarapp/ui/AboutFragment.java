package com.example.polarapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.example.polarapp.R;

public class AboutFragment extends MaterialAboutFragment {

    @Override
    protected MaterialAboutList getMaterialAboutList(final Context activityContext) {
        MaterialAboutCard versionCard = new MaterialAboutCard.Builder()
                .addItem(new MaterialAboutTitleItem.Builder()
                        .text(R.string.app_name)
                        .desc("\u00A9 2019")
                        .icon(R.mipmap.ic_polaris_logo)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Version")
                        .subText("1.0.0")
                        .icon(R.drawable.ic_about_version)
                        .build())
                .build();

        MaterialAboutCard authorCard = new MaterialAboutCard.Builder()
                .title("Authors")
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Ivan Gonzalez")
                        .subText("Spain")
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/ivangonzalezacuna"));
                                startActivity(i);
                            }
                        })
                        .icon(R.drawable.ic_about_author)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Basil Jung")
                        .subText("Switzerland")
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/basiljung"));
                                startActivity(i);
                            }
                        })
                        .icon(R.drawable.ic_about_author)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Ilari Kemppainen")
                        .subText("Finland")
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/Kemppis"));
                                startActivity(i);
                            }
                        })
                        .icon(R.drawable.ic_about_author)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("GitHub Project Repository")
                        .icon(R.drawable.ic_about_github)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/basiljung/polarProject"));
                                startActivity(i);
                            }
                        })
                        .build())
                .build();

        return new MaterialAboutList.Builder()
                .addCard(versionCard)
                .addCard(authorCard)
                .build();
    }

    @Override
    protected int getTheme() {
        return R.style.AppTheme_MaterialAboutActivity_Fragment;
    }
}