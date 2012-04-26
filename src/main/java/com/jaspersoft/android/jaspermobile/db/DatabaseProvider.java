/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.tables.Favorites;
import roboguice.util.Ln;


/**
 * Provides access to the application database.
 *
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class DatabaseProvider {

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_NAME = "jasper_mobile_db";


        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create DB
            db.execSQL(ServerProfiles.TABLE_CREATE_SQL);
            db.execSQL(Favorites.TABLE_CREATE_SQL);

            // Add initial values
            ContentValues values = new ContentValues();

            values.put(ServerProfiles.KEY_ALIAS, "Mobile Demo");
            values.put(ServerProfiles.KEY_SERVER_URL, "http://mobiledemo.jaspersoft.com/jasperserver-pro");
            values.put(ServerProfiles.KEY_ORGANIZATION, "");
            values.put(ServerProfiles.KEY_USERNAME, "phoneuser");
            values.put(ServerProfiles.KEY_PASSWORD, "phoneuser");

            db.insert(ServerProfiles.TABLE_NAME, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Ln.w("Upgrading database from version %d to %d, which will destroy all old data", oldVersion, newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + ServerProfiles.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Favorites.TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    public DatabaseProvider(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public Cursor fetchServerProfile(long rowId) throws SQLException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {
                ServerProfiles.KEY_ALIAS,
                ServerProfiles.KEY_SERVER_URL,
                ServerProfiles.KEY_ORGANIZATION,
                ServerProfiles.KEY_USERNAME,
                ServerProfiles.KEY_PASSWORD
        };
        Cursor cursor = db.query(ServerProfiles.TABLE_NAME, columns, ServerProfiles._ID + "=" + rowId , null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;

    }

    public Cursor fetchFavoriteItem(long rowId) throws SQLException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {
                Favorites.KEY_NAME,
                Favorites.KEY_TITLE,
                Favorites.KEY_DESCRIPTION,
                Favorites.KEY_URI,
                Favorites.KEY_WSTYPE,
                Favorites.KEY_SERVER_PROFILE_ID,
                Favorites.KEY_USERNAME,
                Favorites.KEY_ORGANIZATION
        };
        Cursor cursor = db.query(Favorites.TABLE_NAME, columns, Favorites._ID + "=" + rowId , null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;

    }

    public Cursor fetchAllServerProfiles() throws SQLException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {
                ServerProfiles._ID,
                ServerProfiles.KEY_ALIAS,
                ServerProfiles.KEY_SERVER_URL,
                ServerProfiles.KEY_ORGANIZATION,
                ServerProfiles.KEY_USERNAME,
                ServerProfiles.KEY_PASSWORD
        };
        return db.query(ServerProfiles.TABLE_NAME, columns, null , null, null, null, null);
    }

    public Cursor fetchFavoriteItemsByParams(long serverProfileId, String username, String organization) throws SQLException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {
                Favorites._ID,
                Favorites.KEY_NAME,
                Favorites.KEY_TITLE,
                Favorites.KEY_DESCRIPTION,
                Favorites.KEY_URI,
                Favorites.KEY_WSTYPE
        };
        return db.query(Favorites.TABLE_NAME, columns,
                Favorites.KEY_SERVER_PROFILE_ID + "=" + serverProfileId
                        + " and " + Favorites.KEY_USERNAME + "= ? "
                        + " and " + Favorites.KEY_ORGANIZATION + "= ?",
                new String[] { username,organization }, null, null, Favorites.KEY_TITLE);
    }

    public long insertServerProfile(String alias, String serverUrl, String organization, String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(ServerProfiles.KEY_ALIAS, alias);
        initialValues.put(ServerProfiles.KEY_SERVER_URL, serverUrl);
        initialValues.put(ServerProfiles.KEY_ORGANIZATION, organization);
        initialValues.put(ServerProfiles.KEY_USERNAME, username);
        initialValues.put(ServerProfiles.KEY_PASSWORD, password);

        return db.insert(ServerProfiles.TABLE_NAME, null, initialValues);
    }

    public long insertFavoriteItem(String title, String name, String uri, String description, String wsType, Long serverProfileId, String username, String organization) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(Favorites.KEY_SERVER_PROFILE_ID, serverProfileId);
        initialValues.put(Favorites.KEY_USERNAME, username);
        initialValues.put(Favorites.KEY_ORGANIZATION, organization);  
        initialValues.put(Favorites.KEY_TITLE, title);
        initialValues.put(Favorites.KEY_URI, uri);
        initialValues.put(Favorites.KEY_NAME, name);
        initialValues.put(Favorites.KEY_DESCRIPTION, description);
        initialValues.put(Favorites.KEY_WSTYPE, wsType);

        // Seek in DB if Favorite already present
        Cursor foundFavorite = db.query(Favorites.TABLE_NAME, new String[] {Favorites._ID},
                          Favorites.KEY_SERVER_PROFILE_ID + "= ? and "
                        + Favorites.KEY_URI + "= ? and "
                        + Favorites.KEY_USERNAME + "= ? and "
                        + Favorites.KEY_ORGANIZATION + "= ? ",
                new String[] {serverProfileId.toString(),uri,username,organization}
                , null, null, null, "1");

        // Return present favorite id or create new and return new id
        if (foundFavorite.getCount() > 0) {
            foundFavorite.moveToFirst();
            return foundFavorite.getInt(foundFavorite.getColumnIndex(Favorites._ID));
        } else {
            return db.insert(Favorites.TABLE_NAME, null, initialValues);
        }
    }

    public int updateServerProfile(long rowId, String alias, String serverUrl, String organization, String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues updateValues = new ContentValues();
        updateValues.put(ServerProfiles.KEY_ALIAS, alias);
        updateValues.put(ServerProfiles.KEY_SERVER_URL, serverUrl);
        updateValues.put(ServerProfiles.KEY_ORGANIZATION, organization);
        updateValues.put(ServerProfiles.KEY_USERNAME, username);
        updateValues.put(ServerProfiles.KEY_PASSWORD, password);

        return  db.update(ServerProfiles.TABLE_NAME, updateValues, ServerProfiles._ID + "=" + rowId, null);
    }

    public int deleteServerProfile(long rowId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // remove dependent favorite records
        db.delete(Favorites.TABLE_NAME,Favorites.KEY_SERVER_PROFILE_ID + "=" + rowId, null);

        return db.delete(ServerProfiles.TABLE_NAME, ServerProfiles._ID + "=" + rowId, null);
    }

    public int deleteFavoriteItem(String uri, Long serverProfileId, String username, String organization) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(Favorites.TABLE_NAME,
                          Favorites.KEY_URI + "= ? and "
                        + Favorites.KEY_SERVER_PROFILE_ID +"= ? and "
                        + Favorites.KEY_USERNAME +"= ? and "
                        + Favorites.KEY_ORGANIZATION + "= ?",
                new String[] {uri,serverProfileId.toString(),username,organization} );
    }

}
