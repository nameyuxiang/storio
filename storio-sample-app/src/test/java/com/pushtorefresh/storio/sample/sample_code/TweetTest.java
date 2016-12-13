package com.pushtorefresh.storio.sample.sample_code;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.contentresolver.BuildConfig;
import com.pushtorefresh.storio.sample.SampleApp;
import com.pushtorefresh.storio.sample.db.entities.Tweet;
import com.pushtorefresh.storio.sample.db.entities.TweetStorIOSQLitePutResolver;
import com.pushtorefresh.storio.sample.db.entities.TweetWithUser;
import com.pushtorefresh.storio.sample.db.entities.User;
import com.pushtorefresh.storio.sample.db.tables.TweetsTable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TweetTest {

    @Test
    public void tweets() {
        SampleApp sampleApp = (SampleApp) RuntimeEnvironment.application;
        StorIOSQLite storIOSQLite = sampleApp.appComponent().storIOSQLite();

        storIOSQLite
                .put()
                .object(Tweet.newTweet(1L, "artem_zin", "test tweet 1"))
                .prepare()
                .executeAsBlocking();

        storIOSQLite
                .put()
                .object(Tweet.newTweet(1L, "artem_zin", null))
                .withPutResolver(new TweetStorIOSQLitePutResolver() {
                    @Override
                    @NonNull
                    public ContentValues mapToContentValues(@NonNull Tweet object) {
                        final ContentValues contentValues = super.mapToContentValues(object);
                        contentValues.remove(TweetsTable.COLUMN_CONTENT);
                        contentValues.remove(TweetsTable.COLUMN_CONTENT);   // duplicate call
                        return contentValues;
                    }
                })
                .prepare()
                .executeAsBlocking();

        Tweet fromDb = storIOSQLite
                .get()
                .object(Tweet.class)
                .withQuery(Query.builder()
                        .table(TweetsTable.TABLE)
                        .where(TweetsTable.COLUMN_ID + "=?")
                        .whereArgs(1L)
                        .build())
                .prepare()
                .executeAsBlocking();

        assertThat(fromDb).isNotNull();
        assertThat(fromDb.content()).isEqualTo("test tweet 1");
    }
}