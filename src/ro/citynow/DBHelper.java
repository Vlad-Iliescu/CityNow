package ro.citynow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper {
    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context) {
            super(context, DB.DATABASE_NAME, null, DB.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + DB.CATEGORIE.TABLE_NAME + "( " +
                            DB.CATEGORIE.ID + " INTEGER PRIMARY KEY, " +
                            DB.CATEGORIE.CATEGORIE_ID + " INTEGER, " +
                            DB.CATEGORIE.ACTIV + " INTEGER DEFAULT 1, " +
                            DB.CATEGORIE.NAME + " TEXT, " +
                            DB.CATEGORIE.TEXT_COLOR + " TEXT, " +
                            DB.CATEGORIE.SUBCAT + " INTEGER, " +
                            DB.CATEGORIE.POZA +" BLOB );"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + DB.SUBCATEGORIE.TABLE_NAME + "( " +
                            DB.SUBCATEGORIE.ID + " INTEGER PRIMARY KEY, " +
                            DB.SUBCATEGORIE.CATEGORIE_ID + " INTEGER, " +
                            DB.SUBCATEGORIE.SUBCATEGORIE_ID + " INTEGER, " +
                            DB.SUBCATEGORIE.ACTIV + " INTEGER DEFAULT 1, " +
                            DB.SUBCATEGORIE.NAME + " TEXT, " +
                            DB.SUBCATEGORIE.TEXT_COLOR + " TEXT, " +
                            DB.SUBCATEGORIE.CONTENTS + " INTEGER, " +
                            DB.SUBCATEGORIE.POZA +" BLOB );"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB.CATEGORIE.TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB.SUBCATEGORIE.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    private DBOpenHelper dbHelper;
    private SQLiteDatabase db;

    public DBHelper(Context context) {
        this.dbHelper = new DBOpenHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public Integer getCategorieId(Integer categotie_id) {
        String selection = null;
        String[] selectionArgs = null;
        if (categotie_id != null) {
            selection = DB.CATEGORIE.CATEGORIE_ID + " = ? ";
            selectionArgs = new String[]{String.valueOf(categotie_id)};
        }
        Cursor query =  db.query(DB.CATEGORIE.TABLE_NAME, new String[]{ DB.CATEGORIE.ID }, selection,
                selectionArgs, null, null, null);
        if (query != null) {
            if (query.moveToFirst()) {
                return query.getInt(query.getColumnIndex(DB.CATEGORIE.ID));
            }
        }
        return null;
    }

    public Cursor getPozaCategorie(Long Id) {
        String selection = null;
        String[] selectionArgs = null;
        if (Id != null) {
            selection = DB.CATEGORIE.ID + " = ? ";
            selectionArgs = new String[]{String.valueOf(Id)};
        }
        return db.query(DB.CATEGORIE.TABLE_NAME, new String[]{ DB.CATEGORIE.POZA }, selection,
                selectionArgs, null, null, null);
    }

    public Cursor getCategorie(Integer id) {
        if (id == null) {
            return null;
        }

        String selection = DB.CATEGORIE.ID + " = ? ";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        return db.query(DB.CATEGORIE.TABLE_NAME, new String[]{ DB.CATEGORIE.CATEGORIE_ID, DB.CATEGORIE.SUBCAT },
                selection, selectionArgs, null, null, null);
    }

    public Cursor getCategorie(long id) {
        String selection = DB.CATEGORIE.ID + " = ? ";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        return db.query(DB.CATEGORIE.TABLE_NAME, new String[]{ DB.CATEGORIE.CATEGORIE_ID, DB.CATEGORIE.SUBCAT },
                selection, selectionArgs, null, null, null);
    }

    public Cursor getCategorii() {
        String selection = DB.CATEGORIE.ACTIV + " = ? ";
        String[] selectionArgs =  new String[]{ "1" };
        return db.query(DB.CATEGORIE.TABLE_NAME,
                new String[]{ DB.CATEGORIE.ID, DB.CATEGORIE.NAME, DB.CATEGORIE.TEXT_COLOR, DB.CATEGORIE.POZA,
                              DB.CATEGORIE.CATEGORIE_ID },
                selection, selectionArgs, null, null, DB.CATEGORIE.NAME+" ASC");
    }

    public Cursor getSubcategorii(Integer categorieId) {
        String selection = DB.SUBCATEGORIE.ACTIV + " = ? AND " + DB.SUBCATEGORIE.CATEGORIE_ID + " = ? ";
        String[] selectionArgs =  new String[]{ "1", String.valueOf(categorieId)};
        return db.query(DB.SUBCATEGORIE.TABLE_NAME,
                new String[]{ DB.SUBCATEGORIE.ID, DB.SUBCATEGORIE.SUBCATEGORIE_ID, DB.SUBCATEGORIE.NAME,
                              DB.SUBCATEGORIE.TEXT_COLOR },
                selection, selectionArgs, null, null, DB.SUBCATEGORIE.NAME+" ASC");
    }

    public Integer getSubcategorieId(Integer subcategorieId) {
        String selection = null;
        String[] selectionArgs = null;
        if (subcategorieId != null) {
            selection = DB.SUBCATEGORIE.SUBCATEGORIE_ID + " = ? ";
            selectionArgs = new String[]{String.valueOf(subcategorieId)};
        }
        Cursor query =  db.query(DB.SUBCATEGORIE.TABLE_NAME, new String[]{ DB.SUBCATEGORIE.ID }, selection,
                selectionArgs, null, null, null);
        if (query != null) {
            if (query.moveToFirst()) {
                return query.getInt(query.getColumnIndex(DB.SUBCATEGORIE.ID));
            }
        }
        return null;
    }

    public Cursor getSubcategorie(long id) {
        String selection = DB.SUBCATEGORIE.ID + " = ? ";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        return db.query(DB.SUBCATEGORIE.TABLE_NAME,
                new String[]{ DB.CATEGORIE.CATEGORIE_ID, DB.SUBCATEGORIE.SUBCATEGORIE_ID },
                selection, selectionArgs, null, null, null);
    }

    public Cursor getPozaSubcategorie(Long id) {
        String selection = null;
        String[] selectionArgs = null;
        if (id != null) {
            selection = DB.SUBCATEGORIE.ID + " = ? ";
            selectionArgs = new String[]{String.valueOf(id)};
        }
        return db.query(DB.SUBCATEGORIE.TABLE_NAME, new String[]{ DB.SUBCATEGORIE.POZA }, selection,
                selectionArgs, null, null, null);
    }

    public long saveCategorie(Integer categorie_id, String nume, String poza, String text_color,
                              Integer activ, Integer subcat) {
        ContentValues values = new ContentValues();
        values.put(DB.CATEGORIE.NAME, nume);
        values.put(DB.CATEGORIE.CATEGORIE_ID, categorie_id);
        values.put(DB.CATEGORIE.POZA, poza);
        values.put(DB.CATEGORIE.ACTIV, activ);
        values.put(DB.CATEGORIE.TEXT_COLOR, text_color);
        values.put(DB.CATEGORIE.SUBCAT, subcat);

        return db.insert(DB.CATEGORIE.TABLE_NAME, null, values);
    }

    public long updateCategorieByCategorie(Integer categorie_id, String nume, String poza, String text_color,
                                           Integer activ, Integer subcat) {
        ContentValues values = new ContentValues();
        values.put(DB.CATEGORIE.NAME, nume);
        values.put(DB.CATEGORIE.POZA, poza);
        values.put(DB.CATEGORIE.ACTIV, activ);
        values.put(DB.CATEGORIE.TEXT_COLOR, text_color);
        values.put(DB.CATEGORIE.SUBCAT, subcat);

        return db.update(DB.CATEGORIE.TABLE_NAME, values, DB.CATEGORIE.CATEGORIE_ID + " = ? ",
                new String[]{String.valueOf(categorie_id)});
    }

    public long updateCategorieById(Integer id, Integer categorie_id, String nume, String poza,
                                    String text_color, Integer activ, Integer subcat) {
        ContentValues values = new ContentValues();
        values.put(DB.CATEGORIE.CATEGORIE_ID, categorie_id);
        values.put(DB.CATEGORIE.POZA, poza);
        values.put(DB.CATEGORIE.NAME, nume);
        values.put(DB.CATEGORIE.ACTIV, activ);
        values.put(DB.CATEGORIE.TEXT_COLOR, text_color);
        values.put(DB.CATEGORIE.SUBCAT, subcat);

        return db.update(DB.CATEGORIE.TABLE_NAME, values, DB.CATEGORIE.ID + " = ? ",
                new String[]{ String.valueOf(id) });
    }

    public long saveSubcategorie(Integer subcategorie_id, Integer categorie_id, String nume, String poza,
                                 String text_color, Integer activ, Integer contents) {
        ContentValues values = new ContentValues();
        values.put(DB.SUBCATEGORIE.SUBCATEGORIE_ID, subcategorie_id);
        values.put(DB.SUBCATEGORIE.CATEGORIE_ID, categorie_id);
        values.put(DB.SUBCATEGORIE.NAME, nume);
        values.put(DB.SUBCATEGORIE.POZA, poza);
        values.put(DB.SUBCATEGORIE.ACTIV, activ);
        values.put(DB.SUBCATEGORIE.TEXT_COLOR, text_color);
        values.put(DB.SUBCATEGORIE.CONTENTS, contents);

        return db.insert(DB.SUBCATEGORIE.TABLE_NAME, null, values);
    }

    public long updateSubcategorieById(Integer id, Integer subcategorie_id, Integer categorie_id, String nume,
                                       String poza, String text_color, Integer activ, Integer contents) {
        ContentValues values = new ContentValues();
        values.put(DB.SUBCATEGORIE.SUBCATEGORIE_ID, subcategorie_id);
        values.put(DB.SUBCATEGORIE.CATEGORIE_ID, categorie_id);
        values.put(DB.SUBCATEGORIE.NAME, nume);
        values.put(DB.SUBCATEGORIE.POZA, poza);
        values.put(DB.SUBCATEGORIE.ACTIV, activ);
        values.put(DB.SUBCATEGORIE.TEXT_COLOR, text_color);
        values.put(DB.SUBCATEGORIE.CONTENTS, contents);

        return db.update(DB.SUBCATEGORIE.TABLE_NAME, values, DB.SUBCATEGORIE.ID + " = ? ",
                new String[]{ String.valueOf(id) });
    }
}
