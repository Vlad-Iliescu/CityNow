package ro.citynow;

public final class DB {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "citynow.db";

    public static final class CATEGORIE {
        public static final String TABLE_NAME = "categorie";

        public static final String ID = "_id";
        public static final String CATEGORIE_ID = "cat_id";
        public static final String ACTIV = "activ";
        public static final String NAME = "nume";
        public static final String POZA = "poza";
        public static final String TEXT_COLOR = "text_color";
        public static final String SUBCAT = "subcat";
    }

    public static final class SUBCATEGORIE {
        public static final String TABLE_NAME = "subcategorie";

        public static final String ID = "_id";
        public static final String CATEGORIE_ID = "cat_id";
        public static final String SUBCATEGORIE_ID = "subcat_id";
        public static final String ACTIV = "activ";
        public static final String NAME = "nume";
        public static final String POZA = "poza";
        public static final String TEXT_COLOR = "text_color";
        public static final String CONTENTS = "contents";
    }
}
